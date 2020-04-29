package com.mahotaservicos.koattendance.data

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Location(
        @Exclude var id: String? = "",
        var name: String? = ""
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
                "name" to name
        )
    }
}
