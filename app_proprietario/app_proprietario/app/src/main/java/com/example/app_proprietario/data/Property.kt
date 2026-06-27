package com.example.app_proprietario.data

data class Property(
    val id: String,
    val name: String,
    val rooms: List<Room>,
    val lastSync: String = "há 4 min"
)