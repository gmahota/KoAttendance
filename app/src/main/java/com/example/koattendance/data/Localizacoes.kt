package com.example.koattendance.data

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Localizacoes(
        var id: String? = "",
        var nome: String? = ""
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
                "ID" to id,
                "nome" to nome
        )
    }
}
