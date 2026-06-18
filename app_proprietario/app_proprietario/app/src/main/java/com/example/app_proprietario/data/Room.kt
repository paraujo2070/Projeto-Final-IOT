package com.example.app_proprietario.data

data class Room(
    val id: String,
    val name: String,
    val humidity: Int,
    val temperature: Int,
    val intrusionStatus: IntrusionStatus,
    val moldStatus: MoldStatus
) {
    val overallStatus: String
        get() = when {
            intrusionStatus == IntrusionStatus.INTRUSION_DETECTED -> "Invasão detectada!"
            moldStatus == MoldStatus.RISK_DETECTED -> "Risco de mofo!"
            else -> "Tudo normal"
        }

    val overallStatusDescription: String
        get() = when {
            intrusionStatus == IntrusionStatus.INTRUSION_DETECTED -> "Movimento incomum detectado"
            moldStatus == MoldStatus.RISK_DETECTED -> "Umidade e temperatura fora do normal"
            else -> "Sem invasão e sem risco de mofo"
        }

    val hasAlert: Boolean
        get() = intrusionStatus == IntrusionStatus.INTRUSION_DETECTED || moldStatus == MoldStatus.RISK_DETECTED
}

