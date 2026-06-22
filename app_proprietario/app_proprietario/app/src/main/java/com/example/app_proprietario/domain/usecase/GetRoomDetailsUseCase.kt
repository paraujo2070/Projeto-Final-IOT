package com.example.app_proprietario.domain.usecase

import com.example.app_proprietario.data.Room
import com.example.app_proprietario.domain.RoomRepository

class GetRoomDetailsUseCase(private val repository: RoomRepository) {
    suspend operator fun invoke(propertyId: String, roomId: String): Result<Room> =
        repository.getRoom(propertyId, roomId)
}