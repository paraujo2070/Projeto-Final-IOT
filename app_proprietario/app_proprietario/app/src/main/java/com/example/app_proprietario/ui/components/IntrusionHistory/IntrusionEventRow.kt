package com.example.app_proprietario.ui.components.IntrusionHistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_proprietario.data.IntrusionEvent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun IntrusionEventRow(event: IntrusionEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.padding(top = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = CircleShape
                    )
            )
        }

        Column {
            Text(
                text = if (event.isOngoing) "Movimento em andamento" else "Movimento detectado",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatEventWindow(event),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private fun formatEventWindow(event: IntrusionEvent): String {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
    val dateFormatter = SimpleDateFormat("dd/MM", Locale("pt", "BR"))

    val dayLabel = dayLabelFor(event.startMs, dateFormatter)
    val startTime = timeFormatter.format(Date(event.startMs))

    val base = if (event.startMs == event.endMs) {
        "$dayLabel às $startTime"
    } else {
        val endTime = timeFormatter.format(Date(event.endMs))
        "$dayLabel, $startTime às $endTime"
    }

    return if (event.isOngoing) "$base (em andamento)" else base
}

private fun dayLabelFor(timestampMs: Long, dateFormatter: SimpleDateFormat): String {
    val target = Calendar.getInstance().apply { timeInMillis = timestampMs }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    return when {
        isSameDay(target, today) -> "Hoje"
        isSameDay(target, yesterday) -> "Ontem"
        else -> dateFormatter.format(Date(timestampMs))
    }
}

private fun isSameDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
