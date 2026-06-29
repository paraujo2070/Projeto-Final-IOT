package com.example.app_proprietario.data.remote

data class RawSensorReading(
    val humidityPct: Float,
    val temperatureC: Float,
    val intrusionDetectedRaw: Float,
    val intrusionConfidence: Float,
    val moldRiskCodeRaw: Float,
    val thermalAcceleration: Float,
    val lastActivityMs: Long?,
    val gatewayId: String?,
    val propertyLabel: String?
) {
    companion object {
        fun fromVariables(variables: List<UbidotsVariableDto>): RawSensorReading {
            fun valueOf(label: String): Float =
                variables.find { it.label == label }?.lastValue?.value?.toFloat() ?: 0f

            val lastActivity = variables.mapNotNull { it.lastActivity }.maxOrNull()
            val context = variables
                .find { it.label == UbidotsVariables.INTRUSION_DETECTED }
                ?.lastValue?.context
                ?: variables.firstNotNullOfOrNull { it.lastValue?.context }

            return RawSensorReading(
                humidityPct = valueOf(UbidotsVariables.HUMIDITY),
                temperatureC = valueOf(UbidotsVariables.TEMPERATURE),
                intrusionDetectedRaw = valueOf(UbidotsVariables.INTRUSION_DETECTED),
                intrusionConfidence = valueOf(UbidotsVariables.INTRUSION_CONFIDENCE),
                moldRiskCodeRaw = valueOf(UbidotsVariables.MOLD_RISK_CODE),
                thermalAcceleration = valueOf(UbidotsVariables.THERMAL_ACCELERATION),
                lastActivityMs = lastActivity,
                gatewayId = context?.gatewayId,
                propertyLabel = context?.labelColeta
            )
        }
    }
}