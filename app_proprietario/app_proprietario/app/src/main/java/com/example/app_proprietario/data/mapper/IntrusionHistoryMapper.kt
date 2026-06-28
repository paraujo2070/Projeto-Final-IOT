package com.example.app_proprietario.data.mapper

import com.example.app_proprietario.data.IntrusionEvent
import com.example.app_proprietario.data.remote.UbidotsValueDto
import java.util.concurrent.TimeUnit

object IntrusionHistoryMapper {
    private val MAX_GAP_BETWEEN_READINGS_MS = TimeUnit.MINUTES.toMillis(3)

    fun groupIntoEvents(
        roomId: String,
        roomName: String,
        rawValues: List<UbidotsValueDto>
    ): List<IntrusionEvent> {
        val intrusionTimestamps = rawValues
            .filter { it.timestamp != null && it.value != null && it.value >= 1.0 }
            .mapNotNull { it.timestamp }
            .sorted()

        if (intrusionTimestamps.isEmpty()) return emptyList()

        val lastOverallTimestamp = rawValues.mapNotNull { it.timestamp }.maxOrNull()

        val events = mutableListOf<IntrusionEvent>()
        var windowStart = intrusionTimestamps.first()
        var windowEnd = intrusionTimestamps.first()

        for (timestamp in intrusionTimestamps.drop(1)) {
            val gap = timestamp - windowEnd
            if (gap <= MAX_GAP_BETWEEN_READINGS_MS) {
                windowEnd = timestamp
            } else {
                events += buildEvent(roomId, roomName, windowStart, windowEnd, lastOverallTimestamp)
                windowStart = timestamp
                windowEnd = timestamp
            }
        }

        events += buildEvent(roomId, roomName, windowStart, windowEnd, lastOverallTimestamp)

        return events
    }

    private fun buildEvent(
        roomId: String,
        roomName: String,
        startMs: Long,
        endMs: Long,
        lastOverallTimestamp: Long?
    ): IntrusionEvent = IntrusionEvent(
        roomId = roomId,
        roomName = roomName,
        startMs = startMs,
        endMs = endMs,
        isOngoing = endMs == lastOverallTimestamp
    )
}
