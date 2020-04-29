package com.mahotaservicos.koattendance.ui.attendance

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mahotaservicos.koattendance.repository.AuthRepository
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.collections.HashMap
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class AttendanceViewModel (application: Application) : AndroidViewModel(application) {

    private var database = Firebase?.database?.reference

    private  lateinit var dbAttendace: DatabaseReference

    private val _text = MutableLiveData<String>().apply {

    }
    val text: LiveData<String> = _text

    private val _text_msg= MutableLiveData<String>().apply {

    }
    val text_msg: LiveData<String> = _text_msg


    private val repository = AuthRepository.getInstance(application)

    private val _processing = MutableLiveData<Boolean>()
    val processing: LiveData<Boolean>
        get() = _processing

    public  val signinIntent =repository.signinRequest(_processing)

    fun getUser(): String {
        return  repository.daUser(_processing)
    }

    fun getFullName() {
        repository.get_fullname(_processing)
    }

    fun  getUserIsValidaded() : Boolean{
        var isValidate = repository.user_isValidated(_processing)
        if(isValidate){
            var user = repository.get_User(_processing);

            _text.value ="Time Clock Picker From - " + user.branch
        }else{
            _text.value =  "O seu dispositivo ainda não se encontra credenciado para usar a aplicação queira porfavor registrar o seu número/dispositivo"
        }

        return isValidate;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun writeAttendance(type: String) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        val key = database.child("Attendance").push().key

        if (key == null) {
            Log.w("App_Attendance", "Couldn't get push key for posts")
            return
        }

        val attendance = repository.get_AttendanceData(_processing)

        attendance.type = type
        attendance.dateTime = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT )

        val attendanceValues = attendance.toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates["/attendance/$key"] = attendanceValues

        database.updateChildren(childUpdates)

        if(type == "Clock-In"){
            _text_msg.value = "Sr(a) " +attendance.name;
            _text.value = "Clock-In with Sucess - "  + attendance.dateTimeToString() +"\n" +"Posto:" + attendance.location;
        }else{
            _text_msg.value = "Sr(a) " +attendance.name;
            _text.value = "Clock-Out with Sucess - " + attendance.dateTimeToString()+"\n" +"Posto:" + attendance.location;
        }
    }


}