package br.com.pedro_araujo.coleta_de_dados.network

import android.util.Log
import br.com.pedro_araujo.coleta_de_dados.data.local.TelemetryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

interface TelemetryApiService {
    @POST("/telemetry")
    suspend fun sendTelemetry(@Body payload: okhttp3.RequestBody): Response<Unit>
}

@Singleton
class HttpPublisher @Inject constructor(
    private val telemetryDao: TelemetryDao
) : TelemetryPublisher {
    companion object {
        private const val TAG = "HttpPublisher"
    }

    private var apiService: TelemetryApiService? = null

    override suspend fun connect(url: String, clientId: String, userName: String?, password: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .build()
            
            apiService = retrofit.create(TelemetryApiService::class.java)
            Log.d(TAG, "Initialized HTTP client for: $url")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize HTTP client: ${e.message}", e)
            false
        }
    }

    override suspend fun publishPendingData(topic: String) = withContext(Dispatchers.IO) {
        val service = apiService
        if (service == null) {
            Log.w(TAG, "Cannot publish: HTTP client not initialized")
            return@withContext
        }

        val pending = telemetryDao.getPendingTelemetry()
        Log.i(TAG, "📦 Encontrados ${pending.size} registros pendentes para publicação")
        
        for (item in pending) {
            try {
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = item.payloadJson.toRequestBody(mediaType)
                val response = service.sendTelemetry(requestBody)
                if (response.isSuccessful) {
                    Log.d(TAG, "📤 Mensagem ID ${item.id} enviada com sucesso")
                    telemetryDao.deleteById(item.id)
                    Log.v(TAG, "🗑️ Registro removido do BD local após publicação")
                } else {
                    Log.e(TAG, "❌ Erro ao publicar mensagem ID ${item.id}: ${response.code()}")
                    break 
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao publicar mensagem ID ${item.id}: ${e.message}")
                break
            }
        }
    }

    override fun disconnect() {
        // HTTP client doesn't need explicit disconnection
        apiService = null
    }
}
