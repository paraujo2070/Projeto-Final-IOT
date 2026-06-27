package com.example.app_proprietario.data

data class Room(
    val id: String,
    val name: String,
    val humidity: Int,
    val temperature: Int,
    val intrusionStatus: IntrusionStatus,
    val moldStatus: MoldStatus,
    val lastSync: String = "sem dados"
)