package com.example.koattendance.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.time.Instant
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@IgnoreExtraProperties
data class Attendance(
        var id: Int,
        var user: String?,
        var phoneNumber:String?,
        var dateTime: Date?,
        var type: String?,
        var location:String?
) {
    constructor() :this(-1,"","",Date.from(Instant.now()),"","")

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
                "id" to id,
                "funcionario" to user,
                "telefone" to phoneNumber,
                "tipo" to type,
                "date" to dateTime,
                "Localizacao" to location
        )
    }
}
