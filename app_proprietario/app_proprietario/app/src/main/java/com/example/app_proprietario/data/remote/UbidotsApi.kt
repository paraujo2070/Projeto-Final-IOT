package com.example.app_proprietario.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

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
}