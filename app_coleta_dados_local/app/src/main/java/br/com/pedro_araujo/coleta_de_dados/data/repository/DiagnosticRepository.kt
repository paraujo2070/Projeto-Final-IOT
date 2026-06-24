package br.com.pedro_araujo.coleta_de_dados.data.repository

import br.com.pedro_araujo.coleta_de_dados.data.local.MoldRiskEntity
import br.com.pedro_araujo.coleta_de_dados.data.local.TelemetryDao
import br.com.pedro_araujo.coleta_de_dados.ml.InferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagnosticRepository @Inject constructor(
    private val telemetryDao: TelemetryDao,
    private val inferenceManager: InferenceManager
) {
    private val _inferenceResult = MutableStateFlow("Aguardando...")
    val inferenceResult = _inferenceResult.asStateFlow()

    private val _moldRiskResult = MutableStateFlow("Aguardando...")
    val moldRiskResult = _moldRiskResult.asStateFlow()

    private val _moldDurations = MutableStateFlow(mapOf("Seguro" to 0L, "Alerta" to 0L, "Critico" to 0L))
    val moldDurations = _moldDurations.asStateFlow()

    fun updateInference(inputs: FloatArray) {
        val result = inferenceManager.runInference(inputs)
        _inferenceResult.value = result
    }

    suspend fun processMoldRisk(temp: Double, hum: Double) {
        val fullRiskString = classifyMoldRisk(temp, hum)
        _moldRiskResult.value = fullRiskString
        
        val now = System.currentTimeMillis()
        val currentState = fullRiskString.split(" ")[0] // "Seguro", "Alerta" ou "Critico"
        
        // Só salva no banco se o estado mudou em relação ao último registro
        val lastRecord = telemetryDao.getLatestMoldRisk()
        if (lastRecord == null || lastRecord.state != currentState) {
            telemetryDao.insertMoldRisk(
                MoldRiskEntity(state = currentState, temperature = temp, humidity = hum, timestamp = now)
            )
        }
        
        // Limpa dados com mais de 24h (mantendo a integridade do último estado)
        val dayAgo = now - TimeUnit.DAYS.toMillis(1)
        telemetryDao.deleteOldMoldRisk(dayAgo - TimeUnit.HOURS.toMillis(1)) // Margem de segurança
        
        calculateDurations(dayAgo)
    }

    private fun classifyMoldRisk(temp: Double, hum: Double): String {
        var risk = when {
            hum < 60 -> "Seguro"
            60 <= hum && hum <= 70 -> "Alerta"
            else -> "Critico"
        }
        if (temp >= 20.0 && temp <= 30.0) {
            risk += " (Aceleracao Termica)"
        }
        return risk
    }

    private suspend fun calculateDurations(since: Long) {
        val now = System.currentTimeMillis()
        val history = telemetryDao.getMoldRiskHistory(since).toMutableList()
        
        // Adiciona o estado que estava ativo no exato momento de 24h atrás
        val stateBefore = telemetryDao.getStateBefore(since)
        if (stateBefore != null && (history.isEmpty() || history.first().timestamp > since)) {
            history.add(0, stateBefore.copy(timestamp = since))
        }

        if (history.isEmpty()) return

        val durations = mutableMapOf("Seguro" to 0L, "Alerta" to 0L, "Critico" to 0L)
        
        for (i in 0 until history.size) {
            val current = history[i]
            val startTime = if (current.timestamp < since) since else current.timestamp
            val endTime = if (i + 1 < history.size) history[i + 1].timestamp else now
            
            val duration = endTime - startTime
            if (duration > 0) {
                durations[current.state] = (durations[current.state] ?: 0L) + duration
            }
        }
        
        _moldDurations.value = durations
    }

    fun formatDuration(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02dh %02dm %02ds", hours, minutes, seconds)
    }
}
