package com.mahotaservicos.koattendance.data

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Employee(
        @Exclude var user: String?,
        var code: String?,
        var name: String?,
        var phoneNumber: String?,
        var location: String?,
        @Exclude var branch: String?,
        @Exclude var token: String?,
        @Exclude var validated: Boolean
)
{

    constructor() : this("", "", "","", "", "","",false)

    @Exclude
    fun toMap(): Map<String, String?> {
        return mapOf(
                "code" to code,
                "name" to name,
                "phoneNumber" to phoneNumber,
                "location" to location
        )
    }

    override fun toString(): String {
        return "User:$user Codigo: $code , Nome : $name , localidade : $location , posto: $branch , telefone: $phoneNumber "
    }
}