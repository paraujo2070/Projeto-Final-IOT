from __future__ import annotations

import json
import logging
from datetime import datetime, timezone

from paho.mqtt import publish

from .config import Settings
from .inference import InferenceEngine
from .storage import OutboxStorage


LOGGER = logging.getLogger(__name__)


class CloudForwarder:
    def __init__(self, settings: Settings, storage: OutboxStorage) -> None:
        self.settings = settings
        self.storage = storage
        self.inference = InferenceEngine(settings)

    def disconnect(self) -> None:
        return None

    def flush_pending(self, limit: int = 100) -> int:
        sent_count = 0
        for message in self.storage.get_pending_messages(limit=limit):
            try:
                payload = self._decorate_payload(message.payload)
                publish.single(
                    topic=message.cloud_topic,
                    payload=payload,
                    qos=message.qos,
                    hostname=self.settings.cloud_broker_host,
                    port=self.settings.cloud_broker_port,
                    auth=self._build_auth(),
                    client_id=self.settings.cloud_client_id,
                )
                self.storage.mark_sent(message.id)
                sent_count += 1
                LOGGER.info("Forwarded message id=%s to cloud topic=%s", message.id, message.cloud_topic)
            except Exception as exc:
                LOGGER.error("Failed to forward message id=%s: %s", message.id, exc)
                self.storage.mark_failed(message.id, str(exc))
                break
        return sent_count

    def publish_status(self) -> None:
        if self.settings.cloud_payload_format in {"ubidots", "ubidots_inference"}:
            return

        payload = json.dumps(
            {
                "gateway_id": self.settings.gateway_id,
                "timestamp": datetime.now(timezone.utc).isoformat(),
                "pending_messages": self.storage.pending_count(),
            }
        )
        try:
            publish.single(
                topic=self.settings.status_topic,
                payload=payload,
                qos=self.settings.publish_qos,
                hostname=self.settings.cloud_broker_host,
                port=self.settings.cloud_broker_port,
                auth=self._build_auth(),
                client_id=f"{self.settings.cloud_client_id}-status",
            )
        except Exception as exc:
            LOGGER.error("Failed to publish gateway status: %s", exc)

    def _decorate_payload(self, payload_text: str) -> str:
        if self.settings.cloud_payload_format == "ubidots_inference":
            return self._to_ubidots_inference_payload(payload_text)

        if self.settings.cloud_payload_format == "ubidots":
            return self._to_ubidots_payload(payload_text)

        data = json.loads(payload_text)
        data["gateway"] = {
            "gateway_id": self.settings.gateway_id,
            "forwarded_at": datetime.now(timezone.utc).isoformat(),
        }
        return json.dumps(data, ensure_ascii=False)

    def _to_ubidots_payload(self, payload_text: str) -> str:
        data = json.loads(payload_text)
        metadata = data.get("metadados", {}) if isinstance(data.get("metadados"), dict) else {}
        climate = data.get("clima", {}) if isinstance(data.get("clima"), dict) else {}
        pressure = data.get("pressao", {}) if isinstance(data.get("pressao"), dict) else {}
        inertial = data.get("inercial_vibracao", {}) if isinstance(data.get("inercial_vibracao"), dict) else {}
        context_info = data.get("contexto", {}) if isinstance(data.get("contexto"), dict) else {}

        timestamp_ms = self._parse_timestamp_ms(metadata.get("timestamp"))
        common_context = {
            "device_id": metadata.get("device_id", "unknown-device"),
            "gateway_id": self.settings.gateway_id,
            "source_timestamp": metadata.get("timestamp"),
            "status_sistema": context_info.get("status_sistema"),
            "label_coleta": context_info.get("label_coleta"),
            "janela_amostragem_segundos": metadata.get("janela_amostragem_segundos"),
        }

        payload = {
            "temperatura_c": self._ubidots_var(climate.get("temperatura_c"), timestamp_ms, common_context),
            "umidade_relativa_pct": self._ubidots_var(climate.get("umidade_relativa_pct"), timestamp_ms, common_context),
            "barometro_hpa_media": self._ubidots_var(pressure.get("barometro_hpa_media"), timestamp_ms, common_context),
            "barometro_hpa_variancia": self._ubidots_var(pressure.get("barometro_hpa_variancia"), timestamp_ms, common_context),
            "accel_x_variancia": self._ubidots_var(inertial.get("accel_x_variancia"), timestamp_ms, common_context),
            "accel_y_variancia": self._ubidots_var(inertial.get("accel_y_variancia"), timestamp_ms, common_context),
            "accel_z_variancia": self._ubidots_var(inertial.get("accel_z_variancia"), timestamp_ms, common_context),
            "gyro_x_variancia": self._ubidots_var(inertial.get("gyro_x_variancia"), timestamp_ms, common_context),
            "gyro_y_variancia": self._ubidots_var(inertial.get("gyro_y_variancia"), timestamp_ms, common_context),
            "gyro_z_variancia": self._ubidots_var(inertial.get("gyro_z_variancia"), timestamp_ms, common_context),
        }
        return json.dumps(payload, ensure_ascii=False)

    def _to_ubidots_inference_payload(self, payload_text: str) -> str:
        data = json.loads(payload_text)
        metadata = data.get("metadados", {}) if isinstance(data.get("metadados"), dict) else {}
        context_info = data.get("contexto", {}) if isinstance(data.get("contexto"), dict) else {}

        timestamp_ms = self._parse_timestamp_ms(metadata.get("timestamp"))
        result = self.inference.infer(payload_text)
        common_context = self._build_common_context(metadata, context_info)
        common_context["intrusion_source"] = result.intrusion_source
        common_context["mold_risk_label"] = result.mold_risk_label

        payload = {
            "temperatura_c": self._ubidots_var(result.temperature_c, timestamp_ms, common_context),
            "umidade_relativa_pct": self._ubidots_var(result.humidity_pct, timestamp_ms, common_context),
            "intrusao_detectada": self._ubidots_var(result.intrusion_detected, timestamp_ms, common_context),
            "intrusao_confianca": self._ubidots_var(result.intrusion_confidence, timestamp_ms, common_context),
            "risco_mofo_codigo": self._ubidots_var(result.mold_risk_code, timestamp_ms, common_context),
            "aceleracao_termica": self._ubidots_var(result.thermal_acceleration, timestamp_ms, common_context),
        }
        return json.dumps(payload, ensure_ascii=False)

    def _ubidots_var(self, value, timestamp_ms: int, context: dict) -> dict:
        numeric_value = self._safe_number(value)
        return {
            "value": numeric_value,
            "timestamp": timestamp_ms,
            "context": {k: v for k, v in context.items() if v is not None},
        }

    def _safe_number(self, value) -> float:
        try:
            return float(value)
        except (TypeError, ValueError):
            return 0.0

    def _parse_timestamp_ms(self, value) -> int:
        if not value:
            return int(datetime.now(timezone.utc).timestamp() * 1000)
        try:
            normalized = str(value).replace("Z", "+00:00")
            return int(datetime.fromisoformat(normalized).timestamp() * 1000)
        except ValueError:
            return int(datetime.now(timezone.utc).timestamp() * 1000)

    def _build_auth(self):
        if not self.settings.cloud_broker_username:
            return None
        return {
            "username": self.settings.cloud_broker_username,
            "password": self.settings.cloud_broker_password or "",
        }

    def _build_common_context(self, metadata: dict, context_info: dict) -> dict:
        return {
            "device_id": metadata.get("device_id", "unknown-device"),
            "gateway_id": self.settings.gateway_id,
            "source_timestamp": metadata.get("timestamp"),
            "status_sistema": context_info.get("status_sistema"),
            "label_coleta": context_info.get("label_coleta"),
            "janela_amostragem_segundos": metadata.get("janela_amostragem_segundos"),
        }
