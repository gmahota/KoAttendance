package com.example.koattendance.ui.auth_new

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.example.koattendance.repository.AuthRepository

class AuthViewModel(application: Application) : AndroidViewModel(application){

    private val repository = AuthRepository.getInstance(application)



    val signInState = repository.getSignInState()

    private val _text = MutableLiveData<String>().apply {
        value = "This is gallery Fragment"
    }
    val text: LiveData<String> = _text



    private val _sending = MutableLiveData<Boolean>()

    val sending: LiveData<Boolean>
        get() = _sending

    val username = MutableLiveData<String>()

    val password = MutableLiveData<String>()
    private val _processing = MutableLiveData<Boolean>()
    val processing: LiveData<Boolean>
        get() = _processing



    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    val signInEnabled = MediatorLiveData<Boolean>().apply {
        fun update(processing: Boolean, password: String) {
            value = !processing && password.isNotBlank()
        }
        addSource(_processing) { update(it, password.value ?: "") }
        addSource(password) { update(_processing.value == true, it) }
    }


    fun auth() {
        sendUsername()
        repository.password(password.value ?: "", _processing)


    }

    fun sendUsername() {
        val username = username.value

        if (username != null && username.isNotBlank()) {
            repository.username(username, _sending)
        }
    }



    fun signinResponse(data: Intent) {
        repository.signinResponse(data, _processing)
    }
}