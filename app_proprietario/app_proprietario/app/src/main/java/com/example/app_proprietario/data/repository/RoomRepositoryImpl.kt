package com.example.app_proprietario.data.repository

import com.example.app_proprietario.data.Property
import com.example.app_proprietario.data.Room
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

    override suspend fun getAllProperties(): Result<List<Property>> = runCatching {
        val livePropertyResult = getProperty(UbidotsPropertyConfig.PROPERTY_ID)
        listOf(SampleData.mockedProperty, livePropertyResult.getOrThrow())
    }

    override suspend fun getProperty(propertyId: String): Result<Property> = runCatching {
        when (propertyId) {
            SampleData.mockedProperty.id -> SampleData.mockedProperty
            UbidotsPropertyConfig.PROPERTY_ID -> fetchLiveProperty()
            else -> error("Imovel $propertyId não encontrado")
        }
    }

    override suspend fun getRoom(propertyId: String, roomId: String): Result<Room> = runCatching {
        when (propertyId) {
            SampleData.mockedProperty.id ->
                SampleData.mockedProperty.rooms.find { it.id == roomId }
                    ?: error("Comodo $roomId não encontrado no imovel mockado")

            UbidotsPropertyConfig.PROPERTY_ID -> {
                val devices = api.getDevices(token).results
                    .filterNot { it.label in UbidotsPropertyConfig.IGNORED_DEVICE_LABELS }
                val device = devices.find { it.label == roomId }
                    ?: error("Device $roomId não encontrado na Ubidots")
                fetchRoom(device)
            }

            else -> error("Imovel $propertyId não encontrado")
        }
    }

    private suspend fun fetchLiveProperty(): Property = coroutineScope {
        val devices = api.getDevices(token).results
            .filterNot { it.label in UbidotsPropertyConfig.IGNORED_DEVICE_LABELS }

        val rooms = devices
            .map { device -> async { fetchRoom(device) } }
            .awaitAll()

        Property(
            id = UbidotsPropertyConfig.PROPERTY_ID,
            name = UbidotsPropertyConfig.PROPERTY_NAME,
            rooms = rooms,
            lastSync = RoomMapper.formatNow()
        )
    }

    private suspend fun fetchRoom(device: UbidotsDeviceDto): Room {
        val variables = api.getDeviceVariables(token, device.id).results
        val reading = RawSensorReading.fromVariables(variables)
        return RoomMapper.toRoom(device, reading)
    }
}
