package br.com.pedro_araujo.coleta_de_dados.network

import android.util.Log
import br.com.pedro_araujo.coleta_de_dados.data.local.TelemetryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttPublisher @Inject constructor(
    private val telemetryDao: TelemetryDao
) {
    companion object {
        private const val TAG = "MqttPublisher"
        private const val QOS = 1
    }

    private var mqttClient: MqttClient? = null

    suspend fun connect(brokerUrl: String, clientId: String, userName: String?, password: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            if (mqttClient?.isConnected == true) return@withContext true

            mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())
            val options = MqttConnectOptions().apply {
                isCleanSession = false
                userName?.let { this.userName = it }
                password?.let { this.password = it.toCharArray() }
                setAutomaticReconnect(true)
            }

            mqttClient?.connect(options)
            Log.d(TAG, "Connected to MQTT Broker: $brokerUrl")
            true
        } catch (e: MqttException) {
            Log.e(TAG, "Failed to connect to MQTT: ${e.message}", e)
            false
        }
    }

    suspend fun publishPendingData(topic: String) = withContext(Dispatchers.IO) {
        if (mqttClient?.isConnected != true) {
            Log.w(TAG, "Cannot publish: MQTT not connected")
            return@withContext
        }

        val pending = telemetryDao.getPendingTelemetry()
        Log.i(TAG, "📦 Encontrados ${pending.size} registros pendentes para publicação")
        for (item in pending) {
            try {
                val message = MqttMessage(item.payloadJson.toByteArray()).apply {
                    qos = QOS
                }
                mqttClient?.publish(topic, message)
                Log.d(TAG, "📤 Mensagem ID ${item.id} enviada com sucesso")
                telemetryDao.deleteById(item.id)
                Log.v(TAG, "🗑️ Registro removido do BD local após publicação")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao publicar mensagem ID ${item.id}: ${e.message}")
                break // Stop on error to maintain order
            }
        }
    }

    fun disconnect() {
        try {
            mqttClient?.disconnect()
            mqttClient?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting MQTT: ${e.message}")
        }
    }
}
