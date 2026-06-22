package com.example.app_proprietario.data.mapper

import com.example.app_proprietario.data.IntrusionStatus
import com.example.app_proprietario.data.MoldStatus
import com.example.app_proprietario.data.Room
import com.example.app_proprietario.data.remote.RawSensorReading
import com.example.app_proprietario.data.remote.UbidotsDeviceDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RoomMapper {
    private const val MOTION_VARIANCE_THRESHOLD = 0.5f
    private const val HIGH_HUMIDITY_THRESHOLD = 70
    private const val HIGH_TEMPERATURE_THRESHOLD = 28

    private val timeFormatter = SimpleDateFormat("HH:mm", Locale("pt", "BR"))

    fun toRoom(device: UbidotsDeviceDto, reading: RawSensorReading): Room {
        return Room(
            id = device.label,
            name = device.name,
            humidity = reading.humidityPct.toInt(),
            temperature = reading.temperatureC.toInt(),
            intrusionStatus = detectIntrusion(reading),
            moldStatus = detectMoldRisk(reading),
            lastSync = formatNow()
        )
    }

    private fun detectIntrusion(reading: RawSensorReading): IntrusionStatus {
        val variances = listOf(
            reading.accelXVariance, reading.accelYVariance, reading.accelZVariance,
            reading.gyroXVariance, reading.gyroYVariance, reading.gyroZVariance
        )
        val motionDetected = variances.any { it > MOTION_VARIANCE_THRESHOLD }
        return if (motionDetected) IntrusionStatus.INTRUSION_DETECTED else IntrusionStatus.NO_INTRUSION
    }

    private fun detectMoldRisk(reading: RawSensorReading): MoldStatus {
        val riskyConditions = reading.humidityPct > HIGH_HUMIDITY_THRESHOLD &&
                reading.temperatureC > HIGH_TEMPERATURE_THRESHOLD
        return if (riskyConditions) MoldStatus.RISK_DETECTED else MoldStatus.NO_RISK
    }

    fun formatNow(): String = "às ${timeFormatter.format(Date())}"
}