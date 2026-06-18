package com.example.app_proprietario.data

data class Property(
    val id: String,
    val name: String,
    val rooms: List<Room>,
    val lastSync: String = "há 4 min"
) {
    val roomsWithAlert: List<Room>
        get() = rooms.filter { it.hasAlert }

    val statusSummary: String
        get() {
            val count = roomsWithAlert.size
            return when (count) {
                0 -> "Tudo normal"
                1 -> "1 comodo precisa de atenção"
                else -> "$count comodos precisam de atenção"
            }
        }

    val statusSummaryDescription: String
        get() {
            val first = roomsWithAlert.firstOrNull() ?: return "Todos os comodos estão normais"
            return when {
                first.intrusionStatus == IntrusionStatus.INTRUSION_DETECTED ->
                    "Invasão detectada na ${first.name}"
                first.moldStatus == MoldStatus.RISK_DETECTED ->
                    "Risco de mofo na ${first.name}"
                else -> "Verifique os comodos"
            }
        }
}