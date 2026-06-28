package com.example.app_proprietario.data

import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val PROPERTY_LIST    = "property_list"
    const val PROPERTY_DETAILS = "property_details/{propertyId}"
    const val ROOM_DETAILS     = "room_details/{propertyId}/{propertyName}/{roomId}"
    const val INTRUSION_HISTORY = "intrusion_history/{propertyId}"

    fun propertyDetails(propertyId: String): String {
        val encodedId = URLEncoder.encode(propertyId, "UTF-8")
        return "property_details/$encodedId"
    }

    fun roomDetails(propertyId: String, propertyName: String, roomId: String): String {
        val encodedId = URLEncoder.encode(propertyId, "UTF-8")
        val encodedName = URLEncoder.encode(propertyName, "UTF-8")
        val encodedRoomId = URLEncoder.encode(roomId, "UTF-8")
        return "room_details/$encodedId/$encodedName/$encodedRoomId"
    }

    fun intrusionHistory(propertyId: String): String {
        val encodedId = URLEncoder.encode(propertyId, "UTF-8")
        return "intrusion_history/$encodedId"
    }

    fun decode(raw: String): String =
        URLDecoder.decode(raw, "UTF-8")
}