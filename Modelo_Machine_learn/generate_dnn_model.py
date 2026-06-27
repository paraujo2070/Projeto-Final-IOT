import os
import joblib
import pandas as pd
import numpy as np
import tensorflow as tf
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import classification_report
from imblearn.over_sampling import SMOTE

def build_tflite_optimized_dnn(num_features: int) -> tf.keras.Sequential:
    return tf.keras.Sequential([
        tf.keras.layers.InputLayer(shape=(num_features,)),
        tf.keras.layers.BatchNormalization(),
        tf.keras.layers.Dense(64, activation='relu'),
        tf.keras.layers.Dropout(0.3),
        tf.keras.layers.Dense(32, activation='relu'),
        tf.keras.layers.Dropout(0.2),
        tf.keras.layers.Dense(1, activation='sigmoid')
    ])

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    parquet_file = os.path.join(script_dir, "telemetry.parquet")
    
    if not os.path.exists(parquet_file):
        raise FileNotFoundError(f"Arquivo não localizado: {parquet_file}")

    df = pd.read_parquet(parquet_file)
    df['target_invasao'] = (df['contexto.label_coleta'] == 'quarto_aberto').astype(int)
    
    features = [
        'pressao.barometro_hpa_media', 'pressao.barometro_hpa_variancia', 
        'inercial_vibracao.accel_x_variancia', 'inercial_vibracao.accel_y_variancia', 
        'inercial_vibracao.accel_z_variancia', 'inercial_vibracao.gyro_x_variancia', 
        'inercial_vibracao.gyro_y_variancia', 'inercial_vibracao.gyro_z_variancia'
    ]
    
    X = df[features].values
    y = df['target_invasao'].values
    
    # 1. Separação de Teste (Dados puros para avaliação final)
    X_temp, X_test, y_temp, y_test = train_test_split(
        X, y, test_size=0.20, random_state=42, stratify=y
    )
    
    # 2. Separação de Validação (Dados puros para o Early Stopping)
    X_train, X_val, y_train, y_val = train_test_split(
        X_temp, y_temp, test_size=0.20, random_state=42, stratify=y_temp
    )

    # Aplicação do SMOTE estritamente no subconjunto de treino isolado
    smote = SMOTE(random_state=42)
    X_train_resampled, y_train_resampled = smote.fit_resample(X_train, y_train)

    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train_resampled)
    X_val_scaled = scaler.transform(X_val)
    X_test_scaled = scaler.transform(X_test)
    
    joblib.dump(scaler, os.path.join(script_dir, "scaler_telemetria.pkl"))

    model = build_tflite_optimized_dnn(len(features))
    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=0.001), 
        loss='binary_crossentropy', 
        metrics=['accuracy']
    )
    
    early_stopping = tf.keras.callbacks.EarlyStopping(
        monitor='val_loss',
        patience=10,
        restore_best_weights=True,
        verbose=1
    )
    
    print("\nIniciando treinamento com particionamento estrito de Validação...")
    
    # Substituição do validation_split pelo validation_data isolado
    model.fit(
        X_train_scaled, y_train_resampled, 
        epochs=60, 
        batch_size=32, 
        validation_data=(X_val_scaled, y_val), 
        callbacks=[early_stopping],
        verbose=1
    )

    print("\nAvaliando Generalização no Conjunto de Teste Puro:")
    y_pred_proba = model.predict(X_test_scaled)
    
    limiares = [0.50, 0.45, 0.40]
    for limiar in limiares:
        print(f"\n--- Relatório com Limiar de Decisão: {limiar} ---")
        y_pred = (y_pred_proba > limiar).astype(int)
        print(classification_report(y_test, y_pred, target_names=['Normal', 'Invasao']))

    print("\nIniciando conversão para TensorFlow Lite (INT8 Padrão)...")
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()

    tflite_output_file = os.path.join(script_dir, "modelo_dnn_invasao.tflite")
    with open(tflite_output_file, "wb") as f:
        f.write(tflite_model)
        
    print(f"Sucesso! Modelo exportado estruturalmente para: {tflite_output_file}")

if __name__ == "__main__":
    main()