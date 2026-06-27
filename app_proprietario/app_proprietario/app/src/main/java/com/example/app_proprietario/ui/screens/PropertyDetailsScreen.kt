package com.example.app_proprietario.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_proprietario.data.Property
import com.example.app_proprietario.data.Room
import com.example.app_proprietario.ui.components.ErrorState
import com.example.app_proprietario.ui.components.LoadingState
import com.example.app_proprietario.ui.components.PropertyDetails.PropertyDetailsTopBar
import com.example.app_proprietario.ui.components.PropertyDetails.PropertyStatusBanner
import com.example.app_proprietario.ui.components.PropertyDetails.RoomCard
import com.example.app_proprietario.ui.components.SyncFooter
import com.example.app_proprietario.ui.viewmodel.PropertyDetailsUiState
import com.example.app_proprietario.ui.viewmodel.PropertyDetailsViewModel

@Composable
fun PropertyDetailsScreen(
    viewModel: PropertyDetailsViewModel,
    onBack: () -> Unit,
    onRoomClick: (Room) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is PropertyDetailsUiState.Loading -> LoadingState()
        is PropertyDetailsUiState.Error -> ErrorState(
            message = state.message,
            onRetry = { viewModel.loadProperty() }
        )
        is PropertyDetailsUiState.Success -> PropertyDetailsScreen(
            property = state.property,
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            onBack = onBack,
            onRoomClick = onRoomClick
        )
    }
}

@Composable
fun PropertyDetailsScreen(
    property: Property,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onBack: () -> Unit,
    onRoomClick: (Room) -> Unit
) {
    Scaffold(
        topBar = {
            PropertyDetailsTopBar(
                title = property.name,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    PropertyStatusBanner(property = property)
                }

                item {
                    Text(
                        text = "COMODOS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(property.rooms) { room ->
                    RoomCard(
                        room = room,
                        onClick = { onRoomClick(room) }
                    )
                }
            }

            SyncFooter(
                text = "Ultima sincronização ${property.lastSync}"
            )
        }
    }
}