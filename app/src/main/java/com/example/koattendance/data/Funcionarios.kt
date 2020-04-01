package com.example.koattendance.data

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Funcionarios(
        var user: String?,
        var name: String?,
        var phoneNumber: String?,
        var location: String?,
        var branch: String?,
        @Exclude var token: String?,
        @Exclude var validated: Boolean


)
{

    constructor() : this("", "", "", "", "","",false)

    @Exclude
    fun toMap(): Map<String, String?> {
        return mapOf(
                "ID" to user,
                "Nome" to name,
                "Telefone" to phoneNumber,
                "Localização" to location,
                "Posto" to branch
        )
    }

    override fun toString(): String {
        return "Codigo: $user , Nome : $name , localidade : $location , posto: $branch , telefone: $phoneNumber "
    }
}