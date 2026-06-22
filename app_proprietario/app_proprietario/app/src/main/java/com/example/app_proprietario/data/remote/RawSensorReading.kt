package com.example.app_proprietario.data.remote

data class RawSensorReading(
    val humidityPct: Float,
    val temperatureC: Float,
    val barometerMeanHpa: Float,
    val barometerVarianceHpa: Float,
    val accelXVariance: Float,
    val accelYVariance: Float,
    val accelZVariance: Float,
    val gyroXVariance: Float,
    val gyroYVariance: Float,
    val gyroZVariance: Float,
    val lastActivityMs: Long?
) {
    companion object {
        fun fromVariables(variables: List<UbidotsVariableDto>): RawSensorReading {
            fun valueOf(label: String): Float =
                variables.find { it.label == label }?.lastValue?.value?.toFloat() ?: 0f

            val lastActivity = variables.mapNotNull { it.lastActivity }.maxOrNull()

            return RawSensorReading(
                humidityPct = valueOf(UbidotsVariables.HUMIDITY),
                temperatureC = valueOf(UbidotsVariables.TEMPERATURE),
                barometerMeanHpa = valueOf(UbidotsVariables.BAROMETER_MEAN),
                barometerVarianceHpa = valueOf(UbidotsVariables.BAROMETER_VARIANCE),
                accelXVariance = valueOf(UbidotsVariables.ACCEL_X_VARIANCE),
                accelYVariance = valueOf(UbidotsVariables.ACCEL_Y_VARIANCE),
                accelZVariance = valueOf(UbidotsVariables.ACCEL_Z_VARIANCE),
                gyroXVariance = valueOf(UbidotsVariables.GYRO_X_VARIANCE),
                gyroYVariance = valueOf(UbidotsVariables.GYRO_Y_VARIANCE),
                gyroZVariance = valueOf(UbidotsVariables.GYRO_Z_VARIANCE),
                lastActivityMs = lastActivity
            )
        }
    }
}