package com.example.app_proprietario.ui.screens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_proprietario.data.Property
import com.example.app_proprietario.data.remote.UbidotsConfig
import com.example.app_proprietario.domain.usecase.GetAllPropertiesUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PropertyListUiState {
    data object Loading : PropertyListUiState
    data class Success(val properties: List<Property>, val isRefreshing: Boolean = false) : PropertyListUiState
    data class Error(val message: String) : PropertyListUiState
}

class PropertyListViewModel(
    private val getAllProperties: GetAllPropertiesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PropertyListUiState>(PropertyListUiState.Loading)
    val uiState: StateFlow<PropertyListUiState> = _uiState.asStateFlow()

    init {
        loadProperties()
        startAutoRefresh()
    }

    fun loadProperties() {
        viewModelScope.launch {
            _uiState.value = PropertyListUiState.Loading
            getAllProperties()
                .onSuccess { properties ->
                    _uiState.value = PropertyListUiState.Success(properties)
                }
                .onFailure { error ->
                    _uiState.value = PropertyListUiState.Error(
                        error.message ?: "Falha ao carregar os imoveis"
                    )
                }
        }
    }

    fun refresh() {
        val currentState = _uiState.value
        viewModelScope.launch {
            if (currentState is PropertyListUiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = true)
            } else {
                _uiState.value = PropertyListUiState.Loading
            }

            getAllProperties()
                .onSuccess { properties ->
                    _uiState.value = PropertyListUiState.Success(properties, isRefreshing = false)
                }
                .onFailure { error ->
                    _uiState.value = PropertyListUiState.Error(
                        error.message ?: "Falha ao atualizar os imoveis"
                    )
                }
        }
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(UbidotsConfig.AUTO_REFRESH_INTERVAL_MS)
                getAllProperties()
                    .onSuccess { properties ->
                        _uiState.value = PropertyListUiState.Success(properties)
                    }
            }
        }
    }
}