package br.com.pedro_araujo.coleta_de_dados.network

interface TelemetryPublisher {
    suspend fun connect(url: String, clientId: String, userName: String?, password: String?): Boolean
    suspend fun publishPendingData(topic: String)
    fun disconnect()
}
