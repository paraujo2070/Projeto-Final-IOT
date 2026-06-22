package com.example.app_proprietario.data

import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val PROPERTY_LIST    = "property_list"
    const val PROPERTY_DETAILS = "property_details/{propertyId}"
    const val ROOM_DETAILS     = "room_details/{propertyId}/{propertyName}/{roomId}"

    fun propertyDetails(propertyId: String) = "property_details/$propertyId"

    fun roomDetails(propertyId: String, propertyName: String, roomId: String): String {
        val encodedName = URLEncoder.encode(propertyName, "UTF-8")
        return "room_details/$propertyId/$encodedName/$roomId"
    }

    fun decodePropertyName(raw: String): String =
        URLDecoder.decode(raw, "UTF-8")
}