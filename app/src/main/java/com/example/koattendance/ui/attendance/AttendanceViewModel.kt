package com.example.koattendance.ui.attendance

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.koattendance.data.Attendance
import com.example.koattendance.repository.AuthRepository
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap

@RequiresApi(Build.VERSION_CODES.O)
class AttendanceViewModel (application: Application) : AndroidViewModel(application) {

    private var database = Firebase?.database?.reference

    private  lateinit var dbAttendace: DatabaseReference

    private val _text = MutableLiveData<String>().apply {
        value = "This is Attendance Fragment"
    }
    val text: LiveData<String> = _text

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
        return repository.user_isValidated(_processing);
    }


    fun writeAttendance(date: Date, type: String, location: String) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        val key = database.child("Attendance").push().key
        if (key == null) {
            Log.w("App_Attendance", "Couldn't get push key for posts")
            return
        }
        val user = repository.get_User(_processing)

        val attendance = Attendance(-1,user.user,user.phoneNumber, date,type,location)
        val attendanceValues = attendance.toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates["/attendance/$key"] = attendanceValues
        //childUpdates["/user-posts/$userId/$key"] = attendanceValues

        database.updateChildren(childUpdates)
    }


}