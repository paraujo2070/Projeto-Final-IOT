package com.example.app_proprietario.ui.components.PropertyDetails

import androidx.compose.foundation.BorderStroke as ComposeBorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_proprietario.data.MoldStatus
import com.example.app_proprietario.data.Room
import com.example.app_proprietario.ui.theme.BorderStroke
import com.example.app_proprietario.ui.theme.MoldColor
import com.example.app_proprietario.ui.theme.MoldColorSoft
import com.example.app_proprietario.ui.theme.Surface
import com.example.app_proprietario.ui.theme.TextMuted
import com.example.app_proprietario.ui.theme.TextPrimary
import com.example.app_proprietario.ui.theme.TextSecondary

private const val HIGH_HUMIDITY_THRESHOLD = 70
private const val HIGH_TEMPERATURE_THRESHOLD = 30

@Composable
fun RoomCard(room: Room, onClick: () -> Unit) {
    val hasMoldRisk = room.moldStatus == MoldStatus.RISK_DETECTED
    val isHumidityHigh = room.humidity > HIGH_HUMIDITY_THRESHOLD
    val isTemperatureHigh = room.temperature > HIGH_TEMPERATURE_THRESHOLD

    val cardBackground = if (hasMoldRisk) MoldColorSoft else Surface
    val titleColor = if (hasMoldRisk) MoldColor else TextPrimary
    val chevronColor = if (hasMoldRisk) MoldColor else TextMuted
    val dividerColor = if (hasMoldRisk) MoldColor else BorderStroke
    val labelColor = if (hasMoldRisk) MoldColor else TextSecondary
    val mutedIconColor = if (hasMoldRisk) MoldColor else TextMuted

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = ComposeBorderStroke(0.5.dp, if (hasMoldRisk) MoldColor else BorderStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = room.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 8.dp),
                    tint = chevronColor
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                color = dividerColor
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                MetricRow(
                    icon = {
                        Icon(
                            imageVector = if (hasMoldRisk) Icons.Filled.Warning else Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = mutedIconColor
                        )
                    },
                    label = "Status",
                    labelColor = labelColor,
                    value = if (hasMoldRisk) "Risco de mofo" else "Sem mofo",
                    valueColor = if (hasMoldRisk) MoldColor else TextSecondary,
                    valueWeight = if (hasMoldRisk) FontWeight.Bold else FontWeight.Medium
                )

                MetricRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = mutedIconColor
                        )
                    },
                    label = "Umidade",
                    labelColor = labelColor,
                    value = "${room.humidity}%",
                    valueColor = if (hasMoldRisk) MoldColor else if (isHumidityHigh) MoldColor else TextPrimary,
                    valueWeight = FontWeight.Normal
                )

                MetricRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Thermostat,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = mutedIconColor
                        )
                    },
                    label = "Temperatura",
                    labelColor = labelColor,
                    value = "${room.temperature}°C",
                    valueColor = if (hasMoldRisk) MoldColor else if (isTemperatureHigh) MoldColor else TextPrimary,
                    valueWeight = FontWeight.Normal
                )
            }
        }
    }
}