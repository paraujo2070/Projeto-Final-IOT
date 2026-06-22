package com.example.projetofinal_iot.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_proprietario.data.Property
import com.example.app_proprietario.data.remote.UbidotsConfig
import com.example.app_proprietario.domain.usecase.GetPropertyDetailsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PropertyDetailsUiState {
    data object Loading : PropertyDetailsUiState
    data class Success(val property: Property, val isRefreshing: Boolean = false) : PropertyDetailsUiState
    data class Error(val message: String) : PropertyDetailsUiState
}

class PropertyDetailsViewModel(
    private val getPropertyDetails: GetPropertyDetailsUseCase,
    private val propertyId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<PropertyDetailsUiState>(PropertyDetailsUiState.Loading)
    val uiState: StateFlow<PropertyDetailsUiState> = _uiState.asStateFlow()

    init {
        loadProperty()
        startAutoRefresh()
    }

    fun loadProperty() {
        viewModelScope.launch {
            _uiState.value = PropertyDetailsUiState.Loading
            getPropertyDetails(propertyId)
                .onSuccess { property ->
                    _uiState.value = PropertyDetailsUiState.Success(property)
                }
                .onFailure { error ->
                    _uiState.value = PropertyDetailsUiState.Error(
                        error.message ?: "Falha ao carregar o imovel"
                    )
                }
        }
    }

    fun refresh() {
        val currentState = _uiState.value
        viewModelScope.launch {
            if (currentState is PropertyDetailsUiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = true)
            } else {
                _uiState.value = PropertyDetailsUiState.Loading
            }

            getPropertyDetails(propertyId)
                .onSuccess { property ->
                    _uiState.value = PropertyDetailsUiState.Success(property, isRefreshing = false)
                }
                .onFailure { error ->
                    _uiState.value = PropertyDetailsUiState.Error(
                        error.message ?: "Falha ao atualizar o imovel"
                    )
                }
        }
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(UbidotsConfig.AUTO_REFRESH_INTERVAL_MS)
                getPropertyDetails(propertyId)
                    .onSuccess { property ->
                        _uiState.value = PropertyDetailsUiState.Success(property)
                    }
            }
        }
    }
}
