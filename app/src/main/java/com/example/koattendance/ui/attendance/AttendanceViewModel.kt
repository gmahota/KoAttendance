package com.example.koattendance.ui.attendance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.koattendance.repository.AuthRepository

class AttendanceViewModel (application: Application) : AndroidViewModel(application) {

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

}