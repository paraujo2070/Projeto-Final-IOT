package com.example.app_proprietario.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.app_proprietario.R
import com.example.app_proprietario.data.MoldStatus
import com.example.app_proprietario.data.Room
import com.example.app_proprietario.ui.components.ErrorState
import com.example.app_proprietario.ui.components.LoadingState
import com.example.app_proprietario.ui.components.RoomDetails.MetricCard
import com.example.app_proprietario.ui.components.RoomDetails.RoomDetailsTopBar
import com.example.app_proprietario.ui.components.RoomDetails.RoomStatusBanner
import com.example.app_proprietario.ui.components.SyncFooter
import com.example.app_proprietario.ui.screens.viewmodel.RoomDetailsUiState
import com.example.app_proprietario.ui.screens.viewmodel.RoomDetailsViewModel
import com.example.app_proprietario.ui.theme.MoldColor
import com.example.app_proprietario.ui.theme.TextSecondary

private const val HIGH_HUMIDITY_THRESHOLD = 70
private const val LOW_HUMIDITY_THRESHOLD = 30
private const val HIGH_TEMPERATURE_THRESHOLD = 30
private const val LOW_TEMPERATURE_THRESHOLD = 18

@Composable
fun RoomDetailsScreen(
    viewModel: RoomDetailsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is RoomDetailsUiState.Loading -> LoadingState()
        is RoomDetailsUiState.Error -> ErrorState(
            message = state.message,
            onRetry = { viewModel.loadRoom() }
        )
        is RoomDetailsUiState.Success -> RoomDetailsScreen(
            propertyName = state.propertyName,
            room = state.room,
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            onBack = onBack
        )
    }
}

@Composable
fun RoomDetailsScreen(
    propertyName: String,
    room: Room,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onBack: () -> Unit
) {
    val hasMoldRisk = room.moldStatus == MoldStatus.RISK_DETECTED
    val isHumidityAlert = hasMoldRisk || room.humidity > HIGH_HUMIDITY_THRESHOLD
    val isTemperatureAlert = hasMoldRisk || room.temperature > HIGH_TEMPERATURE_THRESHOLD

    Scaffold(
        topBar = {
            RoomDetailsTopBar(
                title = room.name,
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                RoomStatusBanner(room = room)

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "DADOS DO COMODO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_humidity),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isHumidityAlert) MoldColor else TextSecondary,
                                )
                            },
                            title = "Umidade",
                            value = "${room.humidity}%",
                            description = when {
                                room.humidity > HIGH_HUMIDITY_THRESHOLD -> "Umidade alta"
                                room.humidity < LOW_HUMIDITY_THRESHOLD -> "Umidade baixa"
                                else -> "Umidade normal"
                            },
                            isAlert = isHumidityAlert
                        )

                        MetricCard(
                            modifier = Modifier.weight(1f),
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Thermostat,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isTemperatureAlert) MoldColor else TextSecondary,
                                )
                            },
                            title = "Temperatura",
                            value = "${room.temperature}° C",
                            description = when {
                                room.temperature > HIGH_TEMPERATURE_THRESHOLD -> "Temperatura alta"
                                room.temperature < LOW_TEMPERATURE_THRESHOLD -> "Temperatura baixa"
                                else -> "Temperatura normal"
                            },
                            isAlert = isTemperatureAlert
                        )
                    }
                }
            }

            SyncFooter(
                text = "Última sincronização ${room.lastSync}"
            )
        }
    }
}