#!/usr/bin/env python3
from __future__ import annotations

import json
import os
import random
from pathlib import Path

import numpy as np
import pandas as pd
import tensorflow as tf
from imblearn.over_sampling import SMOTE
from sklearn.metrics import classification_report
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler


FEATURES = [
    "pressao.barometro_hpa_media",
    "pressao.barometro_hpa_variancia",
    "inercial_vibracao.accel_x_variancia",
    "inercial_vibracao.accel_y_variancia",
    "inercial_vibracao.accel_z_variancia",
    "inercial_vibracao.gyro_x_variancia",
    "inercial_vibracao.gyro_y_variancia",
    "inercial_vibracao.gyro_z_variancia",
]
TARGET_COLUMN = "contexto.label_coleta"
TARGET_LABEL = "quarto_aberto"
SEED = 42


def build_model(num_features: int) -> tf.keras.Sequential:
    return tf.keras.Sequential(
        [
            tf.keras.layers.InputLayer(input_shape=(num_features,)),
            tf.keras.layers.BatchNormalization(),
            tf.keras.layers.Dense(64, activation="relu"),
            tf.keras.layers.Dropout(0.3),
            tf.keras.layers.Dense(32, activation="relu"),
            tf.keras.layers.Dropout(0.2),
            tf.keras.layers.Dense(1, activation="sigmoid"),
        ]
    )


def main() -> None:
    random.seed(SEED)
    np.random.seed(SEED)
    tf.random.set_seed(SEED)

    script_dir = Path(os.path.abspath(os.path.dirname(__file__)))
    parquet_path = script_dir / "telemetry.parquet"
    tflite_output = script_dir / "modelo_dnn_invasao_tflite213.tflite"
    scaler_output = script_dir / "scaler_telemetria_tflite213.json"

    if not parquet_path.exists():
        raise FileNotFoundError(f"Arquivo nao localizado: {parquet_path}")

    df = pd.read_parquet(parquet_path)
    missing = [column for column in FEATURES + [TARGET_COLUMN] if column not in df.columns]
    if missing:
        raise ValueError(f"Colunas ausentes no dataset: {missing}")

    X = df[FEATURES].values
    y = (df[TARGET_COLUMN] == TARGET_LABEL).astype(int).values

    X_temp, X_test, y_temp, y_test = train_test_split(
        X,
        y,
        test_size=0.20,
        random_state=SEED,
        stratify=y,
    )
    X_train, X_val, y_train, y_val = train_test_split(
        X_temp,
        y_temp,
        test_size=0.20,
        random_state=SEED,
        stratify=y_temp,
    )

    smote = SMOTE(random_state=SEED)
    X_train_resampled, y_train_resampled = smote.fit_resample(X_train, y_train)

    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train_resampled)
    X_val_scaled = scaler.transform(X_val)
    X_test_scaled = scaler.transform(X_test)

    scaler_output.write_text(
        json.dumps(
            {
                "mean": [float(value) for value in scaler.mean_.tolist()],
                "scale": [float(value) for value in scaler.scale_.tolist()],
            },
            indent=2,
        ),
        encoding="utf-8",
    )

    model = build_model(len(FEATURES))
    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=0.001),
        loss="binary_crossentropy",
        metrics=["accuracy"],
    )

    early_stopping = tf.keras.callbacks.EarlyStopping(
        monitor="val_loss",
        patience=10,
        restore_best_weights=True,
        verbose=1,
    )

    model.fit(
        X_train_scaled,
        y_train_resampled,
        epochs=60,
        batch_size=32,
        validation_data=(X_val_scaled, y_val),
        callbacks=[early_stopping],
        verbose=1,
    )

    y_pred_proba = model.predict(X_test_scaled)
    for threshold in [0.50, 0.45, 0.40]:
        print(f"\n--- Relatorio com limiar de decisao: {threshold} ---")
        y_pred = (y_pred_proba > threshold).astype(int)
        print(classification_report(y_test, y_pred, target_names=["Normal", "Invasao"]))

    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_output.write_bytes(converter.convert())

    print(f"Modelo TFLite 2.13 exportado para: {tflite_output}")
    print(f"Scaler JSON exportado para: {scaler_output}")


if __name__ == "__main__":
    main()
