import os
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report, accuracy_score
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType

def classificar_risco_mofo(temp: float, hum: float) -> str:
    # Proteção contra valores nulos em caso de falha física do sensor
    if pd.isna(temp) or pd.isna(hum):
        return "Dados Incompletos"

    if hum < 60:
        risco = "Seguro"
    elif 60 <= hum <= 70:
        risco = "Alerta"
    else:
        risco = "Critico"
    
    if 20 <= temp <= 30:
        risco += " (Aceleracao Termica)"
        
    return risco

def main():
    # --- Resolução de Caminhos ---
    script_dir = os.path.dirname(os.path.abspath(__file__))
    parquet_file = os.path.join(script_dir, "telemetry.parquet")
    
    if not os.path.exists(parquet_file):
        raise FileNotFoundError(f"Arquivo de telemetria não encontrado na rota: {parquet_file}")

    print("Carregando dados de telemetria...")
    df = pd.read_parquet(parquet_file)

    # --- Variáveis de Colunas Corrigidas ---
    coluna_temp = 'clima.temperatura_c'
    coluna_hum = 'clima.umidade_relativa_pct' # <- Corrigido conforme inspeção de dados
    coluna_target = 'contexto.label_coleta'
    
    # Validação de integridade do pipeline de dados
    if coluna_temp not in df.columns or coluna_hum not in df.columns:
        print(f"\n[ERRO CRÍTICO] Falha de acoplamento: Colunas de clima ausentes.")
        return 

    print("\nProcessando Modelo de Mofo...")
    df['risco_mofo'] = df.apply(
        lambda row: classificar_risco_mofo(row[coluna_temp], row[coluna_hum]), 
        axis=1
    )
    
    print("\nDistribuicao de Risco de Mofo:")
    print(df['risco_mofo'].value_counts())

    print("\nProcessando Modelo de Invasao...")
    
    if coluna_target not in df.columns:
         print(f"\n[ERRO CRÍTICO] Coluna '{coluna_target}' não encontrada. Impossível gerar labels.")
         return

    # Transformação do label categórico em variável alvo binária
    df['target_invasao'] = (df[coluna_target] == 'quarto_aberto').astype(int)
    
    features = [
        'pressao.barometro_hpa_media', 
        'pressao.barometro_hpa_variancia', 
        'inercial_vibracao.accel_x_variancia', 
        'inercial_vibracao.accel_y_variancia', 
        'inercial_vibracao.accel_z_variancia', 
        'inercial_vibracao.gyro_x_variancia', 
        'inercial_vibracao.gyro_y_variancia', 
        'inercial_vibracao.gyro_z_variancia'
    ]
    
    # Validação rigorosa do vetor de características
    features_ausentes = [f for f in features if f not in df.columns]
    if features_ausentes:
        print(f"\n[ERRO CRÍTICO] Divergência no pipeline. Features não encontradas:")
        for f in features_ausentes:
            print(f" - {f}")
        return

    X = df[features]
    y = df['target_invasao']
    
    # Divisão estratificada para garantir representatividade das classes nos testes
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.3, random_state=42, stratify=y
    )
    
    print("\nTreinando Random Forest Classifier...")
    rf_model = RandomForestClassifier(n_estimators=100, random_state=42)
    rf_model.fit(X_train, y_train)
    
    y_pred = rf_model.predict(X_test)
    print("\nRelatorio de Classificacao (Invasao):")
    print(classification_report(y_test, y_pred))
    print(f"Acuracia Global: {accuracy_score(y_test, y_pred):.4f}\n")
    
    importances = pd.DataFrame({
        'feature': features,
        'importance': rf_model.feature_importances_
    }).sort_values(by='importance', ascending=False)
    
    print("Importancia das Variaveis para Detecao de Invasao:")
    print(importances)
    
    # --- Exportação de Artefatos ---
    onnx_output_file = os.path.join(script_dir, "modelo_invasao.onnx")
    parquet_output_file = os.path.join(script_dir, "telemetry_with_predictions.parquet")

    initial_type = [('float_input', FloatTensorType([None, len(features)]))]
    onx = convert_sklearn(rf_model, initial_types=initial_type)
    
    with open(onnx_output_file, "wb") as f:
        f.write(onx.SerializeToString())

    df.to_parquet(parquet_output_file)
    
    print("\nExportacao concluida com sucesso:")
    print(f" -> Modelo ONNX: {onnx_output_file}")
    print(f" -> Predicoes: {parquet_output_file}")

if __name__ == "__main__":
    main()