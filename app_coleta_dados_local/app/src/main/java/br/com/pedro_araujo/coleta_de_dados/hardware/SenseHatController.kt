package br.com.pedro_araujo.coleta_de_dados.hardware

import android.util.Log
import br.com.pedro_araujo.coleta_de_dados.model.ClimateData
import br.com.pedro_araujo.coleta_de_dados.model.InertialData
import br.com.pedro_araujo.coleta_de_dados.model.PressureData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SenseHatController @Inject constructor() {

    companion object {
        private const val TAG = "SenseHatController"
    }

    suspend fun getClimateData(): ClimateData? = withContext(Dispatchers.IO) {
        val tempOutput = executeCli("temp")
        val humOutput = executeCli("humidity")
        
        if (tempOutput == null || humOutput == null) return@withContext null

        try {
            // Tenta extrair apenas números caso venha texto junto (ex: "Temperature: 25.0")
            val temp = extractDouble(tempOutput) ?: 0.0
            val humidity = extractDouble(humOutput) ?: 0.0
            ClimateData(temp, humidity)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing climate: T=$tempOutput, H=$humOutput", e)
            null
        }
    }

    suspend fun getPressureData(): PressureData? = withContext(Dispatchers.IO) {
        val output = executeCli("pressure") ?: return@withContext null
        try {
            val pressure = extractDouble(output) ?: 0.0
            PressureData(pressure)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing pressure: $output", e)
            null
        }
    }

    suspend fun getInertialData(): InertialData? = withContext(Dispatchers.IO) {
        val accelOutput = executeCli("accel")
        val gyroOutput = executeCli("gyro")

        if (accelOutput == null || gyroOutput == null) return@withContext null

        try {
            // Usa regex para encontrar todos os números decimais na string (ex: -0.022, 0.888, etc)
            val numberRegex = Regex("[-+]?[0-9]*\\.?[0-9]+")
            
            val aMatches = numberRegex.findAll(accelOutput).map { it.value.toDouble() }.toList()
            val gMatches = numberRegex.findAll(gyroOutput).map { it.value.toDouble() }.toList()

            InertialData(
                accelX = aMatches.getOrNull(0) ?: 0.0,
                accelY = aMatches.getOrNull(1) ?: 0.0,
                accelZ = aMatches.getOrNull(2) ?: 0.0,
                gyroX = gMatches.getOrNull(0) ?: 0.0,
                gyroY = gMatches.getOrNull(1) ?: 0.0,
                gyroZ = gMatches.getOrNull(2) ?: 0.0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing inertial: A=$accelOutput, G=$gyroOutput", e)
            null
        }
    }

    private fun extractDouble(input: String): Double? {
        // Regex para pegar o primeiro número decimal na string
        val match = Regex("[-+]?[0-9]*\\.?[0-9]+").find(input)
        return match?.value?.toDoubleOrNull()
    }

    private fun executeCli(command: String): String? {
        var process: Process? = null
        return try {
            process = ProcessBuilder("sensehat_cli", command)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            val result = output.trim()
            if (result.isNotEmpty()) {
                Log.v(TAG, "🔍 CLI Output ($command): '$result'")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error executing sensehat_cli $command: ${e.message}")
            null
        } finally {
            process?.destroy()
        }
    }
}
