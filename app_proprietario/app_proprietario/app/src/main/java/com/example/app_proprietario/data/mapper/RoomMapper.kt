package com.example.app_proprietario.data.mapper

import com.example.app_proprietario.data.IntrusionStatus
import com.example.app_proprietario.data.MoldStatus
import com.example.app_proprietario.data.Room
import com.example.app_proprietario.data.RoomWithPropertyInfo
import com.example.app_proprietario.data.remote.RawSensorReading
import com.example.app_proprietario.data.remote.UbidotsDeviceDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RoomMapper {
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale("pt", "BR"))

    fun toRoom(device: UbidotsDeviceDto, reading: RawSensorReading): Room {
        return Room(
            id = device.label,
            name = device.name,
            humidity = reading.humidityPct.toInt(),
            temperature = reading.temperatureC.toInt(),
            intrusionStatus = toIntrusionStatus(reading.intrusionDetectedRaw),
            moldStatus = toMoldStatus(reading.moldRiskCodeRaw),
            lastSync = formatNow()
        )
    }

    fun toRoomWithPropertyInfo(device: UbidotsDeviceDto, reading: RawSensorReading): RoomWithPropertyInfo {
        return RoomWithPropertyInfo(
            room = toRoom(device, reading),
            gatewayId = reading.gatewayId,
            propertyName = reading.propertyLabel
        )
    }

    private fun toIntrusionStatus(intrusionDetectedRaw: Float): IntrusionStatus =
        if (intrusionDetectedRaw >= 1f) IntrusionStatus.INTRUSION_DETECTED else IntrusionStatus.NO_INTRUSION

    private fun toMoldStatus(moldRiskCodeRaw: Float): MoldStatus =
        if (moldRiskCodeRaw >= 1f) MoldStatus.RISK_DETECTED else MoldStatus.NO_RISK

    fun formatNow(): String = "às ${timeFormatter.format(Date())}"
}