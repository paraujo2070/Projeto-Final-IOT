package com.example.app_proprietario.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_proprietario.data.IntrusionEvent
import com.example.app_proprietario.ui.components.ErrorState
import com.example.app_proprietario.ui.components.IntrusionHistory.EmptyIntrusionHistory
import com.example.app_proprietario.ui.components.IntrusionHistory.IntrusionEventRow
import com.example.app_proprietario.ui.components.IntrusionHistory.IntrusionHistoryTopBar
import com.example.app_proprietario.ui.components.LoadingState
import com.example.app_proprietario.ui.screens.viewmodel.IntrusionHistoryUiState
import com.example.app_proprietario.ui.screens.viewmodel.IntrusionHistoryViewModel

@Composable
fun IntrusionHistoryScreen(
    viewModel: IntrusionHistoryViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is IntrusionHistoryUiState.Loading -> LoadingState()
        is IntrusionHistoryUiState.Error -> ErrorState(
            message = state.message,
            onRetry = { viewModel.loadHistory() }
        )
        is IntrusionHistoryUiState.Success -> IntrusionHistoryScreen(
            events = state.events,
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            onBack = onBack
        )
    }
}

@Composable
fun IntrusionHistoryScreen(
    events: List<IntrusionEvent>,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            IntrusionHistoryTopBar(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                onBack = onBack
            )
        }
    ) { paddingValues ->
        if (events.isEmpty()) {
            EmptyIntrusionHistory(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Text(
                        text = "Últimas 24 horas",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(events) { event ->
                    IntrusionEventRow(event = event)
                }
            }
        }
    }
}