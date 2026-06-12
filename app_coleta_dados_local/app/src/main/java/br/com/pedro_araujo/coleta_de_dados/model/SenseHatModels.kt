package br.com.pedro_araujo.coleta_de_dados.model

data class ClimateData(
    val temperatureC: Double,
    val humidityPct: Double
)

data class PressureData(
    val barometerHpa: Double
)

data class InertialData(
    val accelX: Double,
    val accelY: Double,
    val accelZ: Double,
    val gyroX: Double,
    val gyroY: Double,
    val gyroZ: Double
)
