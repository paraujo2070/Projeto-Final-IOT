package com.example.projetofinal_iot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
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
import com.example.app_proprietario.data.IntrusionStatus
import com.example.app_proprietario.data.MoldStatus
import com.example.app_proprietario.data.Room
import com.example.app_proprietario.ui.components.ErrorState
import com.example.app_proprietario.ui.components.LoadingState
import com.example.app_proprietario.ui.components.RoomDetails.MetricCard
import com.example.app_proprietario.ui.components.RoomDetails.RoomDetailsTopBar
import com.example.app_proprietario.ui.components.RoomDetails.RoomStatusBanner
import com.example.app_proprietario.ui.components.RoomDetails.StatusItemCard
import com.example.app_proprietario.ui.components.SyncFooter
import com.example.projetofinal_iot.ui.viewmodel.RoomDetailsUiState
import com.example.projetofinal_iot.ui.viewmodel.RoomDetailsViewModel

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
                    StatusItemCard(
                        icon = { Icon(Icons.Outlined.Shield, null, Modifier.size(22.dp)) },
                        title = if (room.intrusionStatus == IntrusionStatus.INTRUSION_DETECTED)
                            "Invasão detectada!" else "Sem invasão",
                        description = if (room.intrusionStatus == IntrusionStatus.INTRUSION_DETECTED)
                            "Movimento incomum detectado"
                        else
                            "Nenhum movimento incomum detectado",
                        isAlert = room.intrusionStatus == IntrusionStatus.INTRUSION_DETECTED
                    )

                    StatusItemCard(
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_humidity),
                                null,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        title = if (room.moldStatus == MoldStatus.RISK_DETECTED)
                            "Risco de mofo!" else "Sem risco de mofo",
                        description = if (room.moldStatus == MoldStatus.RISK_DETECTED)
                            "Umidade e temperatura fora da faixa segura"
                        else
                            "Umidade e temperatura dentro da faixa segura",
                        isAlert = room.moldStatus == MoldStatus.RISK_DETECTED
                    )

                    Text(
                        text = "DADOS DO COMODO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
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
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            title = "Umidade",
                            value = "${room.humidity}%",
                            description = when {
                                room.humidity > 70 -> "Umidade alta"
                                room.humidity < 30 -> "Umidade baixa"
                                else -> "Umidade normal"
                            }
                        )

                        MetricCard(
                            modifier = Modifier.weight(1f),
                            icon = {
                                Icon(
                                    Icons.Outlined.Thermostat,
                                    null, Modifier.size(16.dp)
                                )
                            },
                            title = "Temperatura",
                            value = "${room.temperature}° C",
                            description = when {
                                room.temperature > 30 -> "Temperatura alta"
                                room.temperature < 18 -> "Temperatura baixa"
                                else -> "Temperatura normal"
                            }
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