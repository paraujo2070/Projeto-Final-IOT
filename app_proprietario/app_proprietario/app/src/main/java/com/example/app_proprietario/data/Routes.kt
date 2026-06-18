package com.example.app_proprietario.data

object Routes {
    const val PROPERTY_LIST    = "property_list"
    const val PROPERTY_DETAILS = "property_details/{propertyId}"
    const val ROOM_DETAILS     = "room_details/{propertyId}/{roomId}"
    fun propertyDetails(propertyId: String) = "property_details/$propertyId"
    fun roomDetails(propertyId: String, roomId: String) = "room_details/$propertyId/$roomId"
}
