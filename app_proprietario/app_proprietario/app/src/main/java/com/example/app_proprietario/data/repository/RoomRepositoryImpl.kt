package com.example.app_proprietario.data.repository

import com.example.app_proprietario.data.Property
import com.example.app_proprietario.data.Room
import com.example.app_proprietario.data.RoomWithPropertyInfo
import com.example.app_proprietario.data.SampleData
import com.example.app_proprietario.data.mapper.RoomMapper
import com.example.app_proprietario.data.remote.RawSensorReading
import com.example.app_proprietario.data.remote.UbidotsApi
import com.example.app_proprietario.data.remote.UbidotsConfig
import com.example.app_proprietario.data.remote.UbidotsDeviceDto
import com.example.app_proprietario.data.remote.UbidotsPropertyConfig
import com.example.app_proprietario.domain.RoomRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class RoomRepositoryImpl(
    private val api: UbidotsApi,
    private val token: String = UbidotsConfig.AUTH_TOKEN
) : RoomRepository {

    companion object {
        const val UNASSIGNED_PROPERTY_ID = "sem-imovel"
        const val UNASSIGNED_PROPERTY_NAME = "Comodos sem imovel"
    }

    override suspend fun getAllProperties(): Result<List<Property>> = runCatching {
        listOf(SampleData.mockedProperty) + fetchLiveProperties()
    }

    override suspend fun getProperty(propertyId: String): Result<Property> = runCatching {
        if (propertyId == SampleData.mockedProperty.id) {
            return@runCatching SampleData.mockedProperty
        }
        fetchLiveProperties().find { it.id == propertyId }
            ?: error("Imovel $propertyId não encontrado")
    }

    override suspend fun getRoom(propertyId: String, roomId: String): Result<Room> = runCatching {
        if (propertyId == SampleData.mockedProperty.id) {
            return@runCatching SampleData.mockedProperty.rooms.find { it.id == roomId }
                ?: error("Comodo $roomId não encontrado no imovel mockado")
        }

        val devices = api.getDevices(token).results
            .filterNot { it.label in UbidotsPropertyConfig.IGNORED_DEVICE_LABELS }
        val device = devices.find { it.label == roomId }
            ?: error("Device $roomId não encontrado na Ubidots")
        fetchRoomWithPropertyInfo(device).room
    }

    private suspend fun fetchLiveProperties(): List<Property> = coroutineScope {
        val devices = api.getDevices(token).results
            .filterNot { it.label in UbidotsPropertyConfig.IGNORED_DEVICE_LABELS }

        val roomsWithInfo = devices
            .map { device -> async { fetchRoomWithPropertyInfo(device) } }
            .awaitAll()

        roomsWithInfo
            .groupBy { it.gatewayId ?: UNASSIGNED_PROPERTY_ID }
            .map { (gatewayId, roomsInGateway) ->
                val propertyName = roomsInGateway.firstNotNullOfOrNull { it.propertyName }
                    ?: if (gatewayId == UNASSIGNED_PROPERTY_ID) UNASSIGNED_PROPERTY_NAME else gatewayId

                Property(
                    id = gatewayId,
                    name = propertyName,
                    rooms = roomsInGateway.map { it.room },
                    lastSync = RoomMapper.formatNow()
                )
            }
    }

    private suspend fun fetchRoomWithPropertyInfo(device: UbidotsDeviceDto): RoomWithPropertyInfo {
        val variables = api.getDeviceVariables(token, device.id).results
        val reading = RawSensorReading.fromVariables(variables)
        return RoomMapper.toRoomWithPropertyInfo(device, reading)
    }
}