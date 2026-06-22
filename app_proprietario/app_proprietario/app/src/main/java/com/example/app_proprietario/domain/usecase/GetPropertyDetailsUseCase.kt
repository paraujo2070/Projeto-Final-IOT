package com.example.app_proprietario.domain.usecase

import com.example.app_proprietario.data.Property
import com.example.app_proprietario.domain.RoomRepository

class GetPropertyDetailsUseCase(private val repository: RoomRepository) {
    suspend operator fun invoke(propertyId: String): Result<Property> =
        repository.getProperty(propertyId)
}