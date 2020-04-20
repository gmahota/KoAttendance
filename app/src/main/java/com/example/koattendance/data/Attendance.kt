package com.example.koattendance.data
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@IgnoreExtraProperties
data class Attendance(
        @Exclude  var id: Int,
        var user: String?,
        var name : String?,
        var phoneNumber:String?,
        var dateTime: String?,
        var type: String?,
        var locationId:String?,
        var location:String?,
        var latitude: Double,
        var longitude:Double
) {
    constructor() :this(-1,"","","","","","","",0.0,0.0)

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
                "employee" to user,
                "name" to name,
                "phoneNumber" to phoneNumber,
                "type" to type,
                "date" to dateTime,
                "locationId" to locationId,
                "location" to location,
                "latitude" to latitude,
                "longitude" to longitude
        )
    }

    fun dateTimeToString():String{
        val pattern = "dd - MMM - yyyy - HH:mm:ss"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val d = Date.from(Instant.parse(dateTime))

        return simpleDateFormat.format(d)
    }
}
