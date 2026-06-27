package com.example.app_proprietario.ui.components.PropertyDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_proprietario.data.IntrusionStatus
import com.example.app_proprietario.data.Property
import com.example.app_proprietario.ui.theme.IntrusionColor
import com.example.app_proprietario.ui.theme.TextPrimary
import com.example.app_proprietario.ui.theme.TextSecondary

@Composable
fun PropertyStatusBanner(property: Property) {
    val intrusionDetected = property.rooms.any { it.intrusionStatus == IntrusionStatus.INTRUSION_DETECTED }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = if (intrusionDetected) IntrusionColor else TextPrimary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = if (intrusionDetected) "Invasão detectada" else "Sem invasão",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (intrusionDetected) IntrusionColor else TextPrimary
                )

                Text(
                    text = if (intrusionDetected)
                        "Ocorreu movimento incomum neste imóvel"
                    else
                        "Nenhum movimento incomum detectado neste imóvel",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}