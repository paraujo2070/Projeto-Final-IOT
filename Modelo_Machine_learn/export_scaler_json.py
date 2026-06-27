#!/usr/bin/env python3
from __future__ import annotations

import json
from pathlib import Path

import joblib


def main() -> None:
    script_dir = Path(__file__).resolve().parent
    scaler = joblib.load(script_dir / "scaler_telemetria.pkl")
    payload = {
        "mean": [float(value) for value in scaler.mean_.tolist()],
        "scale": [float(value) for value in scaler.scale_.tolist()],
    }
    (script_dir / "scaler_telemetria.json").write_text(
        json.dumps(payload, indent=2),
        encoding="utf-8",
    )


if __name__ == "__main__":
    main()
