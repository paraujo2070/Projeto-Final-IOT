package com.example.projetofinal_iot.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_proprietario.data.Room
import com.example.app_proprietario.domain.usecase.GetRoomDetailsUseCase
import com.example.app_proprietario.data.remote.UbidotsConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RoomDetailsUiState {
    data object Loading : RoomDetailsUiState
    data class Success(val propertyName: String, val room: Room, val isRefreshing: Boolean = false) : RoomDetailsUiState
    data class Error(val message: String) : RoomDetailsUiState
}

class RoomDetailsViewModel(
    private val getRoomDetails: GetRoomDetailsUseCase,
    private val propertyId: String,
    private val propertyName: String,
    private val roomId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<RoomDetailsUiState>(RoomDetailsUiState.Loading)
    val uiState: StateFlow<RoomDetailsUiState> = _uiState.asStateFlow()

    init {
        loadRoom()
        startAutoRefresh()
    }

    fun loadRoom() {
        viewModelScope.launch {
            _uiState.value = RoomDetailsUiState.Loading
            getRoomDetails(propertyId, roomId)
                .onSuccess { room ->
                    _uiState.value = RoomDetailsUiState.Success(propertyName, room)
                }
                .onFailure { error ->
                    _uiState.value = RoomDetailsUiState.Error(
                        error.message ?: "Falha ao carregar o comodo"
                    )
                }
        }
    }

    fun refresh() {
        val currentState = _uiState.value
        viewModelScope.launch {
            if (currentState is RoomDetailsUiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = true)
            } else {
                _uiState.value = RoomDetailsUiState.Loading
            }

            getRoomDetails(propertyId, roomId)
                .onSuccess { room ->
                    _uiState.value = RoomDetailsUiState.Success(propertyName, room, isRefreshing = false)
                }
                .onFailure { error ->
                    _uiState.value = RoomDetailsUiState.Error(
                        error.message ?: "Falha ao atualizar o comodo"
                    )
                }
        }
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(UbidotsConfig.AUTO_REFRESH_INTERVAL_MS)
                getRoomDetails(propertyId, roomId)
                    .onSuccess { room ->
                        _uiState.value = RoomDetailsUiState.Success(propertyName, room)
                    }
            }
        }
    }
}
