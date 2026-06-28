package com.example.app_proprietario.data

data class IntrusionEvent(
    val roomId: String,
    val roomName: String,
    val startMs: Long,
    val endMs: Long,
    val isOngoing: Boolean
)
