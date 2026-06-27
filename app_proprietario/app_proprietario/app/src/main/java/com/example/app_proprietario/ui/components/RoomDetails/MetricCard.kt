package com.example.app_proprietario.ui.components.RoomDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_proprietario.ui.theme.Background
import com.example.app_proprietario.ui.theme.MoldColor
import com.example.app_proprietario.ui.theme.MoldColorSoft
import com.example.app_proprietario.ui.theme.TextMuted
import com.example.app_proprietario.ui.theme.TextPrimary
import com.example.app_proprietario.ui.theme.TextSecondary

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    title: String,
    value: String,
    description: String,
    isAlert: Boolean = false
) {
    val cardBackground = if (isAlert) MoldColorSoft else Background
    val titleColor = if (isAlert) MoldColor else TextSecondary
    val valueColor = if (isAlert) MoldColor else TextPrimary
    val descriptionColor = if (isAlert) MoldColor else TextMuted

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                icon()
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = titleColor
                )
            }

            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )

            Text(
                text = description,
                fontSize = 12.sp,
                color = descriptionColor
            )
        }
    }
}