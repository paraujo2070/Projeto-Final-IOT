from __future__ import annotations

import json
import logging
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from .config import Settings


LOGGER = logging.getLogger(__name__)

try:
    import numpy as np
except ImportError:  # pragma: no cover - optional dependency on target gateway
    np = None

try:
    import tflite_runtime.interpreter as tflite
except ImportError:  # pragma: no cover - optional dependency on target gateway
    tflite = None


@dataclass(frozen=True)
class InferenceResult:
    temperature_c: float
    humidity_pct: float
    intrusion_detected: int
    intrusion_confidence: float
    intrusion_source: str
    mold_risk_code: int
    mold_risk_label: str
    thermal_acceleration: int


class InferenceEngine:
    def __init__(self, settings: Settings) -> None:
        self.settings = settings
        self._tflite_interpreter = None
        self._tflite_input_details = None
        self._tflite_output_details = None
        self._scaler_mean: list[float] | None = None
        self._scaler_scale: list[float] | None = None
        self._load_tflite_model()

    def infer(self, payload_text: str) -> InferenceResult:
        data = json.loads(payload_text)

        climate = data.get("clima", {}) if isinstance(data.get("clima"), dict) else {}
        pressure = data.get("pressao", {}) if isinstance(data.get("pressao"), dict) else {}
        inertial = (
            data.get("inercial_vibracao", {})
            if isinstance(data.get("inercial_vibracao"), dict)
            else {}
        )

        temperature_c = self._safe_number(climate.get("temperatura_c"))
        humidity_pct = self._safe_number(climate.get("umidade_relativa_pct"))
        features = [
            self._safe_number(pressure.get("barometro_hpa_media")),
            self._safe_number(pressure.get("barometro_hpa_variancia")),
            self._safe_number(inertial.get("accel_x_variancia")),
            self._safe_number(inertial.get("accel_y_variancia")),
            self._safe_number(inertial.get("accel_z_variancia")),
            self._safe_number(inertial.get("gyro_x_variancia")),
            self._safe_number(inertial.get("gyro_y_variancia")),
            self._safe_number(inertial.get("gyro_z_variancia")),
        ]

        intrusion_detected, intrusion_confidence, intrusion_source = self._predict_intrusion(features)
        mold_risk_code, mold_risk_label, thermal_acceleration = self._predict_mold(
            temperature_c,
            humidity_pct,
        )

        return InferenceResult(
            temperature_c=temperature_c,
            humidity_pct=humidity_pct,
            intrusion_detected=intrusion_detected,
            intrusion_confidence=intrusion_confidence,
            intrusion_source=intrusion_source,
            mold_risk_code=mold_risk_code,
            mold_risk_label=mold_risk_label,
            thermal_acceleration=thermal_acceleration,
        )

    def _load_tflite_model(self) -> None:
        model_path = self._resolve_optional_path(self.settings.inference_model_path)
        scaler_path = self._resolve_optional_path(self.settings.inference_scaler_path)

        if not model_path or model_path.suffix != ".tflite":
            LOGGER.warning("TFLite model path not configured. Falling back to heuristic intrusion detection.")
            return
        if not model_path.exists():
            LOGGER.warning("TFLite model file not found at %s. Falling back to heuristic intrusion detection.", model_path)
            return
        if not scaler_path or not scaler_path.exists():
            LOGGER.warning("Scaler JSON file not found. Falling back to heuristic intrusion detection.")
            return
        if tflite is None or np is None:
            LOGGER.warning("tflite_runtime/numpy unavailable. Falling back to heuristic intrusion detection.")
            return

        try:
            scaler = json.loads(scaler_path.read_text(encoding="utf-8"))
            self._scaler_mean = [float(value) for value in scaler["mean"]]
            self._scaler_scale = [float(value) for value in scaler["scale"]]
            if len(self._scaler_mean) != 8 or len(self._scaler_scale) != 8:
                raise ValueError("scaler must contain 8 mean values and 8 scale values")

            self._tflite_interpreter = tflite.Interpreter(model_path=str(model_path))
            self._tflite_interpreter.allocate_tensors()
            self._tflite_input_details = self._tflite_interpreter.get_input_details()
            self._tflite_output_details = self._tflite_interpreter.get_output_details()
            LOGGER.info("Loaded TFLite intrusion model from %s", model_path)
        except Exception as exc:
            LOGGER.warning(
                "Failed to load TFLite intrusion model from %s: %s. Falling back to heuristic intrusion detection.",
                model_path,
                exc,
            )
            self._tflite_interpreter = None

    def _predict_intrusion(self, features: list[float]) -> tuple[int, float, str]:
        if self._tflite_interpreter is not None:
            try:
                probability = self._predict_intrusion_tflite(features)
                class_id = 1 if probability >= self.settings.intrusion_decision_threshold else 0
                return class_id, probability, "tflite"
            except Exception as exc:
                LOGGER.warning("TFLite inference failed: %s. Falling back to heuristic intrusion detection.", exc)

        return self._heuristic_intrusion(features)

    def _predict_intrusion_tflite(self, features: list[float]) -> float:
        assert self._tflite_interpreter is not None
        assert self._tflite_input_details is not None
        assert self._tflite_output_details is not None
        assert self._scaler_mean is not None
        assert self._scaler_scale is not None
        assert np is not None

        normalized = [
            (value - mean) / scale if scale != 0 else 0.0
            for value, mean, scale in zip(features, self._scaler_mean, self._scaler_scale)
        ]
        input_data = np.array([normalized], dtype=np.float32)
        self._tflite_interpreter.set_tensor(self._tflite_input_details[0]["index"], input_data)
        self._tflite_interpreter.invoke()
        output = self._tflite_interpreter.get_tensor(self._tflite_output_details[0]["index"])
        return self._safe_number(output[0][0])

    def _heuristic_intrusion(self, features: list[float]) -> tuple[int, float, str]:
        motion_variances = features[2:]
        max_variance = max(motion_variances, default=0.0)
        threshold = self.settings.intrusion_variance_threshold
        intrusion_detected = 1 if any(value > threshold for value in motion_variances) else 0
        confidence = min(1.0, max_variance / threshold) if threshold > 0 else float(intrusion_detected)
        return intrusion_detected, confidence, "heuristic"

    def _predict_mold(self, temperature_c: float, humidity_pct: float) -> tuple[int, str, int]:
        thermal_acceleration = 1 if 20.0 <= temperature_c <= 30.0 else 0

        if humidity_pct < 60.0:
            return 0, "seguro", thermal_acceleration
        if humidity_pct <= 70.0:
            return 1, "alerta", thermal_acceleration
        return 2, "critico", thermal_acceleration

    def _resolve_optional_path(self, value: str | None) -> Path | None:
        if not value:
            return None
        path = Path(value).expanduser()
        if not path.is_absolute():
            path = Path.cwd() / path
        return path

    def _safe_number(self, value: Any) -> float:
        try:
            return float(value)
        except (TypeError, ValueError):
            return 0.0
