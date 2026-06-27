package com.example.app_proprietario.ui.components.RoomDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.app_proprietario.ui.theme.Background
import com.example.app_proprietario.ui.theme.MoldColor
import com.example.app_proprietario.ui.theme.MoldColorSoft
import com.example.app_proprietario.ui.theme.Surface
import com.example.app_proprietario.ui.theme.TextPrimary
import com.example.app_proprietario.ui.theme.TextSecondary

private const val HIGH_HUMIDITY_THRESHOLD = 70
private const val LOW_HUMIDITY_THRESHOLD = 30
private const val HIGH_TEMPERATURE_THRESHOLD = 30
private const val LOW_TEMPERATURE_THRESHOLD = 18

@Composable
fun RoomStatusBanner(room: Room) {
    val hasMoldRisk = room.moldStatus == MoldStatus.RISK_DETECTED
    val iconBackgroundColor = if (hasMoldRisk) MoldColorSoft else Background
    val isHumidityOutOfRange = room.humidity > HIGH_HUMIDITY_THRESHOLD || room.humidity < LOW_HUMIDITY_THRESHOLD
    val isTemperatureOutOfRange = room.temperature > HIGH_TEMPERATURE_THRESHOLD || room.temperature < LOW_TEMPERATURE_THRESHOLD
    val description = when {
        hasMoldRisk -> "Umidade e temperatura fora da faixa segura"
        isHumidityOutOfRange && isTemperatureOutOfRange -> "Umidade e temperatura fora da faixa normal"
        isTemperatureOutOfRange -> "Temperatura fora da faixa normal"
        isHumidityOutOfRange -> "Umidade fora da faixa normal"
        else -> "Umidade e temperatura dentro da faixa segura"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = iconBackgroundColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (hasMoldRisk) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MoldColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.SentimentVerySatisfied,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = if (hasMoldRisk) "Risco de mofo" else "Sem risco de mofo",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (hasMoldRisk) MoldColor else TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
    }
}