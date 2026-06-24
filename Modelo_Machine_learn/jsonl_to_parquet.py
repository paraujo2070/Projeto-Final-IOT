#!/usr/bin/env python3
import os
import json
import pandas as pd


def converter_jsonl_para_parquet(
    caminho_entrada: str, caminho_saida: str, achatar: bool = True
):
    
    print(f"Lendo dados de: {caminho_entrada}")

    # Lista para armazenar as linhas de JSON lidas
    dados = []

    # Lê o arquivo JSONL linha por linha para garantir robustez e baixo uso de memória
    with open(caminho_entrada, "r", encoding="utf-8") as f:
        for num_linha, linha in enumerate(f, 1):
            linha = linha.strip()
            if not linha:
                continue
            try:
                registro = json.loads(linha)
                dados.append(registro)
            except json.JSONDecodeError as e:
                print(f"Aviso: Erro ao decodificar JSON na linha {num_linha}: {e}")

    print(f"Total de registros carregados: {len(dados)}")

    if achatar:
        print("Achatando estruturas de dicionários aninhados (ex: clima, pressao)...")
        # json_normalize converte as chaves aninhadas em colunas planas separadas por ponto
        df = pd.json_normalize(dados)
    else:
        print("Mantendo dados na estrutura aninhada original...")
        df = pd.DataFrame(dados)

    
    colunas_tempo = ["received_at", "metadados.timestamp", "metadados_timestamp"]
    for col in colunas_tempo:
        if col in df.columns:
            try:
                df[col] = pd.to_datetime(df[col])
                print(f"Coluna '{col}' convertida para datetime.")
            except Exception as e:
                print(f"Aviso: Não foi possível converter '{col}' para datetime: {e}")

    # Exibe informações básicas do dataset gerado
    print("\n--- Estrutura das Colunas Criadas ---")
    print(df.info())

    print(f"\nSalvando dataset em formato Parquet: {caminho_saida}")

    df.to_parquet(caminho_saida, index=False, engine="pyarrow", compression="snappy")

    print("Conversão concluída com sucesso!")


if __name__ == "__main__":
    diretorio_atual = os.path.dirname(os.path.abspath(__file__))
    input_file = os.path.join(diretorio_atual, "telemetry.jsonl")
    output_file = os.path.join(diretorio_atual, "telemetry.parquet")

    # Executa a conversão
    if os.path.exists(input_file):
        converter_jsonl_para_parquet(
            caminho_entrada=input_file, caminho_saida=output_file, achatar=True
        )
    else:
        print(f"Erro: Arquivo '{input_file}' não encontrado.")
