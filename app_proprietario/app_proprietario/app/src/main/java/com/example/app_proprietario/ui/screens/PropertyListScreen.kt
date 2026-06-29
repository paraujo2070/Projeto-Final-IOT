package com.example.app_proprietario.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app_proprietario.data.Property
import com.example.app_proprietario.ui.components.ErrorState
import com.example.app_proprietario.ui.components.LoadingState
import com.example.app_proprietario.ui.components.PropertyListScreen.PropertyCard
import com.example.app_proprietario.ui.components.PropertyListScreen.PropertyListTopBar
import com.example.app_proprietario.ui.screens.viewmodel.PropertyListUiState
import com.example.app_proprietario.ui.screens.viewmodel.PropertyListViewModel

@Composable
fun PropertyListScreen(
    viewModel: PropertyListViewModel,
    onPropertyClick: (Property) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is PropertyListUiState.Loading -> LoadingState()
        is PropertyListUiState.Error -> ErrorState(
            message = state.message,
            onRetry = { viewModel.loadProperties() }
        )
        is PropertyListUiState.Success -> PropertyListScreen(
            properties = state.properties,
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            onPropertyClick = onPropertyClick
        )
    }
}

@Composable
fun PropertyListScreen(
    properties: List<Property>,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onPropertyClick: (Property) -> Unit
) {
    Scaffold(
        topBar = {
            PropertyListTopBar(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(properties) { property ->
                PropertyCard(
                    property = property,
                    onClick = { onPropertyClick(property) }
                )
            }
        }
    }
}