package com.example.app_proprietario.ui.screens

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_proprietario.data.IntrusionStatus
import com.example.app_proprietario.data.MoldStatus
import com.example.app_proprietario.data.Room
import com.example.app_proprietario.data.SampleData
import com.example.app_proprietario.R
import com.example.app_proprietario.ui.components.RoomDetails.MetricCard
import com.example.app_proprietario.ui.components.RoomDetails.RoomDetailsTopBar
import com.example.app_proprietario.ui.components.RoomDetails.RoomStatusBanner
import com.example.app_proprietario.ui.components.RoomDetails.StatusItemCard
import com.example.app_proprietario.ui.components.SyncFooter

@Composable
fun RoomDetailsScreen(
    propertyName: String,
    room: Room,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            RoomDetailsTopBar(
                title = "$propertyName - ${room.name}",
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
                        text = "COMODOS",
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
                text = "Última sincronização há 4 min"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoomDetailsScreenPreview() {
    MaterialTheme {
        RoomDetailsScreen(
            propertyName = "Casa de Praia",
            room = SampleData.properties.first().rooms.first(),
            onBack = {}
        )
    }
}
