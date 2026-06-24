package br.com.pedro_araujo.coleta_de_dados.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import br.com.pedro_araujo.coleta_de_dados.R
import br.com.pedro_araujo.coleta_de_dados.data.local.TelemetryDao
import br.com.pedro_araujo.coleta_de_dados.data.local.TelemetryEntity
import br.com.pedro_araujo.coleta_de_dados.data.repository.DiagnosticRepository
import br.com.pedro_araujo.coleta_de_dados.hardware.SenseHatController
import br.com.pedro_araujo.coleta_de_dados.model.*
import br.com.pedro_araujo.coleta_de_dados.network.TelemetryPublisher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.pow

@AndroidEntryPoint
class TelemetryService : LifecycleService() {

    @Inject lateinit var senseHatController: SenseHatController
    @Inject lateinit var telemetryDao: TelemetryDao
    @Inject lateinit var telemetryPublisher: TelemetryPublisher
    @Inject lateinit var diagnosticRepository: DiagnosticRepository

    private var job: Job? = null
    private val CHANNEL_ID = "TelemetryServiceChannel"
    private val NOTIFICATION_ID = 1

    companion object {
        private const val TAG = "TelemetryService"
        private const val WINDOW_SECONDS = 10
        private const val SAMPLING_DELAY_MS = 1000L // 1Hz sampling
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "🟢 TelemetryService criado")
        createNotificationChannel()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID, 
                createNotification(),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.i(TAG, "▶️ TelemetryService iniciado (onStartCommand)")
        
        // Reforça o startForeground no onStartCommand para evitar ANR/Crash no Android 14
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID, 
                createNotification(),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }

        if (job == null) {
            job = lifecycleScope.launch {
                telemetryLoop()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.w(TAG, "🛑 TelemetryService sendo destruído")
        job?.cancel()
        super.onDestroy()
    }

    private suspend fun telemetryLoop() {
        Log.i(TAG, "🚀 Iniciando loop de telemetria (Janela: $WINDOW_SECONDS s)")
        
        val accelSamplesX = mutableListOf<Double>()
        val accelSamplesY = mutableListOf<Double>()
        val accelSamplesZ = mutableListOf<Double>()
        val gyroSamplesX = mutableListOf<Double>()
        val gyroSamplesY = mutableListOf<Double>()
        val gyroSamplesZ = mutableListOf<Double>()
        val pressureSamples = mutableListOf<Double>()
        var latestClimate: ClimateData? = null
        
        var secondsCounter = 0

        while (currentCoroutineContext().isActive) {
            val climate = senseHatController.getClimateData()
            val pressure = senseHatController.getPressureData()
            val inertial = senseHatController.getInertialData()

            climate?.let { latestClimate = it }
            pressure?.let { 
                pressureSamples.add(it.barometerHpa)
                if (pressureSamples.size > WINDOW_SECONDS) pressureSamples.removeAt(0)
            }
            inertial?.let {
                accelSamplesX.add(it.accelX)
                accelSamplesY.add(it.accelY)
                accelSamplesZ.add(it.accelZ)
                gyroSamplesX.add(it.gyroX)
                gyroSamplesY.add(it.gyroY)
                gyroSamplesZ.add(it.gyroZ)
                
                if (accelSamplesX.size > WINDOW_SECONDS) {
                    accelSamplesX.removeAt(0)
                    accelSamplesY.removeAt(0)
                    accelSamplesZ.removeAt(0)
                    gyroSamplesX.removeAt(0)
                    gyroSamplesY.removeAt(0)
                    gyroSamplesZ.removeAt(0)
                }
            }

            // A cada 2 segundos, faz inferência e mofo
            if (secondsCounter % 2 == 0) {
                if (pressureSamples.isNotEmpty()) {
                    val inputs = floatArrayOf(
                        pressureSamples.average().toFloat(),
                        calculateVariance(pressureSamples).toFloat(),
                        calculateVariance(accelSamplesX).toFloat(),
                        calculateVariance(accelSamplesY).toFloat(),
                        calculateVariance(accelSamplesZ).toFloat(),
                        calculateVariance(gyroSamplesX).toFloat(),
                        calculateVariance(gyroSamplesY).toFloat(),
                        calculateVariance(gyroSamplesZ).toFloat()
                    )
                    diagnosticRepository.updateInference(inputs)
                }
                
                latestClimate?.let {
                    diagnosticRepository.processMoldRisk(it.temperatureC, it.humidityPct)
                }
            }

            // A cada 10 segundos, salva o payload normal no banco e publica
            if (secondsCounter > 0 && secondsCounter % WINDOW_SECONDS == 0) {
                val payload = buildPayload(
                    latestClimate,
                    pressureSamples.toList(),
                    accelSamplesX.toList(), accelSamplesY.toList(), accelSamplesZ.toList(),
                    gyroSamplesX.toList(), gyroSamplesY.toList(), gyroSamplesZ.toList()
                )
                saveAndPublish(payload)
            }

            secondsCounter++
            delay(SAMPLING_DELAY_MS)
        }
    }

    private fun buildPayload(
        climate: ClimateData?,
        pressureSamples: List<Double>,
        ax: List<Double>,
        ay: List<Double>,
        az: List<Double>,
        gx: List<Double>,
        gy: List<Double>,
        gz: List<Double>
    ): TelemetryPayload {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        // Retrieve config from SharedPreferences
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("device_id", "Unknown") ?: "Unknown"
        val status = prefs.getString("system_status", "Normal") ?: "Normal"
        val label = prefs.getString("label_coleta", "ambiente_normal") ?: "ambiente_normal"

        return TelemetryPayload(
            metadados = Metadata(deviceId, sdf.format(Date()), WINDOW_SECONDS),
            clima = Climate(climate?.temperatureC ?: 0.0, climate?.humidityPct ?: 0.0),
            pressao = Pressure(
                barometro_hpa_media = pressureSamples.average().takeIf { !it.isNaN() } ?: 0.0,
                barometro_hpa_variancia = calculateVariance(pressureSamples)
            ),
            inercial_vibracao = InertialVibration(
                accel_x_variancia = calculateVariance(ax),
                accel_y_variancia = calculateVariance(ay),
                accel_z_variancia = calculateVariance(az),
                gyro_x_variancia = calculateVariance(gx),
                gyro_y_variancia = calculateVariance(gy),
                gyro_z_variancia = calculateVariance(gz)
            ),
            contexto = ContextInfo(status, label)
        )
    }

    private suspend fun saveAndPublish(payload: TelemetryPayload) {
        val json = Json.encodeToString(payload)
        Log.d(TAG, "💾 Persistindo ciclo de telemetria no BD: $json")
        try {
            telemetryDao.insert(TelemetryEntity(payloadJson = json, timestamp = System.currentTimeMillis()))
            Log.v(TAG, "✅ Inserção no BD concluída")
            
            // Incrementa contador de sessão
            val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val currentTotal = prefs.getInt("total_cycles_session", 0)
            prefs.edit().putInt("total_cycles_session", currentTotal + 1).apply()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao salvar no BD: ${e.message}")
        }

        // Attempt to publish
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val broker = prefs.getString("broker_url", "") ?: ""
        val topic = prefs.getString("mqtt_topic", "telemetry") ?: "telemetry"
        
        if (broker.isNotEmpty()) {
            Log.d(TAG, "🌐 Tentando publicar via HTTP para: $broker")
            if (telemetryPublisher.connect(broker, "EdgeNode_${System.currentTimeMillis()}", null, null)) {
                telemetryPublisher.publishPendingData(topic)
            } else {
                Log.w(TAG, "⚠️ Falha na conexão HTTP. Dados permanecem no BD local.")
            }
        } else {
            Log.i(TAG, "ℹ️ Broker não configurado. Dados apenas salvos localmente.")
        }
    }

    private fun calculateVariance(samples: List<Double>): Double {
        if (samples.isEmpty()) return 0.0
        val avg = samples.average()
        return samples.map { (it - avg).pow(2) }.average()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Telemetry Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Edge IoT Node")
            .setContentText("Collecting telemetry data...")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
}
