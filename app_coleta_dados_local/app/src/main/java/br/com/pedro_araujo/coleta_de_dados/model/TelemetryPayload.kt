package br.com.pedro_araujo.coleta_de_dados.model

import kotlinx.serialization.Serializable

@Serializable
data class TelemetryPayload(
    val metadados: Metadata,
    val clima: Climate,
    val pressao: Pressure,
    val inercial_vibracao: InertialVibration,
    val contexto: ContextInfo
)

@Serializable
data class Metadata(
    val device_id: String,
    val timestamp: String,
    val janela_amostragem_segundos: Int
)

@Serializable
data class Climate(
    val temperatura_c: Double,
    val umidade_relativa_pct: Double
)

@Serializable
data class Pressure(
    val barometro_hpa_media: Double,
    val barometro_hpa_variancia: Double
)

@Serializable
data class InertialVibration(
    val accel_x_variancia: Double,
    val accel_y_variancia: Double,
    val accel_z_variancia: Double,
    val gyro_x_variancia: Double,
    val gyro_y_variancia: Double,
    val gyro_z_variancia: Double
)

@Serializable
data class ContextInfo(
    val status_sistema: String,
    val label_coleta: String
)
