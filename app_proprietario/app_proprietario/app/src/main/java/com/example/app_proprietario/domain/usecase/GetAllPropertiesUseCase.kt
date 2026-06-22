package com.example.app_proprietario.domain.usecase

import com.example.app_proprietario.data.Property
import com.example.app_proprietario.domain.RoomRepository

class GetAllPropertiesUseCase(private val repository: RoomRepository) {
    suspend operator fun invoke(): Result<List<Property>> =
        repository.getAllProperties()
}