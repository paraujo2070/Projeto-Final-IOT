package com.example.app_proprietario.domain

import com.example.app_proprietario.data.IntrusionEvent
import com.example.app_proprietario.data.Property
import com.example.app_proprietario.data.Room

interface RoomRepository {
    suspend fun getAllProperties(): Result<List<Property>>

    suspend fun getProperty(propertyId: String): Result<Property>

    suspend fun getRoom(propertyId: String, roomId: String): Result<Room>

    suspend fun getIntrusionHistory(propertyId: String, hoursBack: Int = 24): Result<List<IntrusionEvent>>
}
