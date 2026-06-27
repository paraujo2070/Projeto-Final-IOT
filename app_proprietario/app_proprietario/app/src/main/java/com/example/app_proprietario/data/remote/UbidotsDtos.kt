package com.example.app_proprietario.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UbidotsDeviceListResponse(
    val count: Int = 0,
    val next: String? = null,
    val previous: String? = null,
    val results: List<UbidotsDeviceDto> = emptyList()
)

@Serializable
data class UbidotsDeviceDto(
    val id: String,
    val label: String,
    val name: String,
    @SerialName("lastActivity") val lastActivity: Long? = null,
    @SerialName("isActive") val isActive: Boolean = true,
    val tags: List<String> = emptyList()
)

@Serializable
data class UbidotsVariableListResponse(
    val count: Int = 0,
    val next: String? = null,
    val previous: String? = null,
    val results: List<UbidotsVariableDto> = emptyList()
)

@Serializable
data class UbidotsVariableDto(
    val id: String,
    val label: String,
    val name: String,
    @SerialName("lastActivity") val lastActivity: Long? = null,
    @SerialName("lastValue") val lastValue: UbidotsLastValueDto? = null
)

@Serializable
data class UbidotsLastValueDto(
    val value: Double? = null,
    val timestamp: Long? = null,
    val context: UbidotsContextDto? = null
)

@Serializable
data class UbidotsContextDto(
    @SerialName("gateway_id") val gatewayId: String? = null,
    @SerialName("label_coleta") val labelColeta: String? = null,
    @SerialName("device_id") val deviceId: String? = null,
    @SerialName("intrusion_source") val intrusionSource: String? = null,
    @SerialName("mold_risk_label") val moldRiskLabel: String? = null,
    @SerialName("status_sistema") val statusSistema: String? = null,
    @SerialName("source_timestamp") val sourceTimestamp: String? = null,
    @SerialName("janela_amostragem_segundos") val sampleWindowSeconds: Int? = null
)