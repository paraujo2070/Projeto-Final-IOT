package com.example.app_proprietario.domain.usecase

import com.example.app_proprietario.data.IntrusionEvent
import com.example.app_proprietario.domain.RoomRepository

class GetIntrusionHistoryUseCase(private val repository: RoomRepository) {
    suspend operator fun invoke(propertyId: String, hoursBack: Int = 24): Result<List<IntrusionEvent>> =
        repository.getIntrusionHistory(propertyId, hoursBack)
}
