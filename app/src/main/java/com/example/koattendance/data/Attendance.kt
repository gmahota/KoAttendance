package com.example.koattendance.data
import java.time.LocalDateTime
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.time.Instant
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@IgnoreExtraProperties
data class Attendance(
        @Exclude  var id: Int,
        var user: String?,
        var phoneNumber:String?,
        var dateTime: String?,
        var type: String?,
        var location:String?,
        var latitude: Double,
        var longitude:Double
) {
    constructor() :this(-1,"","","","","",0.0,0.0)

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
                "employee" to user,
                "phoneNumber" to phoneNumber,
                "type" to type,
                "date" to dateTime,
                "location" to location,
                "latitude" to latitude,
                "longitude" to longitude
        )
    }
}
