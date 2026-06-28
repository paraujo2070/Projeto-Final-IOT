package com.example.app_proprietario.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface UbidotsApi {
    @GET("api/v2.0/devices/")
    suspend fun getDevices(
        @Header("X-Auth-Token") token: String
    ): UbidotsDeviceListResponse

    @GET("api/v2.0/devices/{deviceId}/variables/")
    suspend fun getDeviceVariables(
        @Header("X-Auth-Token") token: String,
        @Path("deviceId") deviceId: String
    ): UbidotsVariableListResponse

    @GET("api/v1.6/variables/{variableId}/values/")
    suspend fun getVariableValues(
        @Header("X-Auth-Token") token: String,
        @Path("variableId") variableId: String,
        @Query("start") startMs: Long,
        @Query("end") endMs: Long,
        @Query("page_size") pageSize: Int = 500
    ): UbidotsValueListResponse
}