package com.example.app_proprietario.ui.screens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_proprietario.data.IntrusionEvent
import com.example.app_proprietario.domain.usecase.GetIntrusionHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface IntrusionHistoryUiState {
    data object Loading : IntrusionHistoryUiState
    data class Success(val events: List<IntrusionEvent>, val isRefreshing: Boolean = false) : IntrusionHistoryUiState
    data class Error(val message: String) : IntrusionHistoryUiState
}

class IntrusionHistoryViewModel(
    private val getIntrusionHistory: GetIntrusionHistoryUseCase,
    private val propertyId: String,
    private val hoursBack: Int = 24
) : ViewModel() {

    private val _uiState = MutableStateFlow<IntrusionHistoryUiState>(IntrusionHistoryUiState.Loading)
    val uiState: StateFlow<IntrusionHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = IntrusionHistoryUiState.Loading
            getIntrusionHistory(propertyId, hoursBack)
                .onSuccess { events ->
                    _uiState.value = IntrusionHistoryUiState.Success(events)
                }
                .onFailure { error ->
                    _uiState.value = IntrusionHistoryUiState.Error(
                        error.message ?: "Falha ao carregar o histórico"
                    )
                }
        }
    }

    fun refresh() {
        val currentState = _uiState.value
        viewModelScope.launch {
            if (currentState is IntrusionHistoryUiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = true)
            } else {
                _uiState.value = IntrusionHistoryUiState.Loading
            }

            getIntrusionHistory(propertyId, hoursBack)
                .onSuccess { events ->
                    _uiState.value = IntrusionHistoryUiState.Success(events, isRefreshing = false)
                }
                .onFailure { error ->
                    _uiState.value = IntrusionHistoryUiState.Error(
                        error.message ?: "Falha ao atualizar o histórico"
                    )
                }
        }
    }
}
