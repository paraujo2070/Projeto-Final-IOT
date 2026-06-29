package com.example.app_proprietario.data.repository

import com.example.app_proprietario.data.IntrusionEvent
import com.example.app_proprietario.data.Property
import com.example.app_proprietario.data.Room
import com.example.app_proprietario.data.RoomWithPropertyInfo
import com.example.app_proprietario.data.SampleData
import com.example.app_proprietario.data.mapper.IntrusionHistoryMapper
import com.example.app_proprietario.data.mapper.RoomMapper
import com.example.app_proprietario.data.remote.RawSensorReading
import com.example.app_proprietario.data.remote.UbidotsApi
import com.example.app_proprietario.data.remote.UbidotsConfig
import com.example.app_proprietario.data.remote.UbidotsDeviceDto
import com.example.app_proprietario.data.remote.UbidotsPropertyConfig
import com.example.app_proprietario.data.remote.UbidotsVariables
import com.example.app_proprietario.domain.RoomRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.TimeUnit

class RoomRepositoryImpl(
    private val api: UbidotsApi,
    private val token: String = UbidotsConfig.AUTH_TOKEN
) : RoomRepository {

    companion object {
        const val UNASSIGNED_PROPERTY_ID = "sem-imovel"
        const val UNASSIGNED_PROPERTY_NAME = "Comodos sem imovel"
        const val DEFAULT_INTRUSION_HOURS_BACK = 24
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

    override suspend fun getIntrusionHistory(propertyId: String, hoursBack: Int): Result<List<IntrusionEvent>> =
        runCatching {
            if (propertyId == SampleData.mockedProperty.id) {
                return@runCatching emptyList()
            }

            val property = fetchLiveProperties().find { it.id == propertyId }
                ?: error("Imovel $propertyId não encontrado")

            fetchIntrusionEventsForProperty(property, hoursBack)
        }

    private suspend fun fetchLiveProperties(): List<Property> = coroutineScope {
        val devices = api.getDevices(token).results
            .filterNot { it.label in UbidotsPropertyConfig.IGNORED_DEVICE_LABELS }

        val roomsWithInfo = devices
            .map { device -> async { fetchRoomWithPropertyInfo(device) } }
            .awaitAll()

        val baseProperties = roomsWithInfo
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

        baseProperties
            .map { property ->
                async {
                    val hadIntrusion = fetchIntrusionEventsForProperty(
                        property,
                        DEFAULT_INTRUSION_HOURS_BACK
                    ).isNotEmpty()
                    property.copy(hadIntrusionLast24h = hadIntrusion)
                }
            }
            .awaitAll()
    }

    private suspend fun fetchIntrusionEventsForProperty(
        property: Property,
        hoursBack: Int
    ): List<IntrusionEvent> = coroutineScope {
        val devices = api.getDevices(token).results
            .filterNot { it.label in UbidotsPropertyConfig.IGNORED_DEVICE_LABELS }
            .filter { device -> property.rooms.any { it.id == device.label } }

        val endMs = System.currentTimeMillis()
        val startMs = endMs - TimeUnit.HOURS.toMillis(hoursBack.toLong())

        devices
            .map { device -> async { fetchRoomIntrusionEvents(device, startMs, endMs) } }
            .awaitAll()
            .flatten()
            .sortedByDescending { it.startMs }
    }

    private suspend fun fetchRoomWithPropertyInfo(device: UbidotsDeviceDto): RoomWithPropertyInfo {
        val variables = api.getDeviceVariables(token, device.id).results
        val reading = RawSensorReading.fromVariables(variables)
        return RoomMapper.toRoomWithPropertyInfo(device, reading)
    }

    private suspend fun fetchRoomIntrusionEvents(
        device: UbidotsDeviceDto,
        startMs: Long,
        endMs: Long
    ): List<IntrusionEvent> {
        val variables = api.getDeviceVariables(token, device.id).results
        val intrusionVariable = variables.find { it.label == UbidotsVariables.INTRUSION_DETECTED }
            ?: return emptyList()

        val values = api.getVariableValues(
            token = token,
            variableId = intrusionVariable.id,
            startMs = startMs,
            endMs = endMs
        ).results

        return IntrusionHistoryMapper.groupIntoEvents(
            roomId = device.label,
            roomName = device.name,
            rawValues = values
        )
    }
}
