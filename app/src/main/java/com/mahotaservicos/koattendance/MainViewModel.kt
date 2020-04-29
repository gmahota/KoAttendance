/*
 * Copyright 2019 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mahotaservicos.koattendance

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mahotaservicos.koattendance.data.Attendance
import com.mahotaservicos.koattendance.repository.AuthRepository
import com.google.android.gms.fido.fido2.Fido2ApiClient
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository.getInstance(application)

    private val _processing = MutableLiveData<Boolean>()
    val processing: LiveData<Boolean>
        get() = _processing

    val _lat = MutableLiveData<Double>().apply {
        value = 0.0
    }
    val lat: LiveData<Double> = _lat

    val _long = MutableLiveData<Double>().apply {
        value = 0.0
    }
    val long: LiveData<Double> = _long

    val signInState = repository.getSignInState()

    fun setFido2ApiClient(client: Fido2ApiClient?) {
        repository.setFido2APiClient(client)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setAttendance(){

        var la =  _lat.value!!
        var lo =  _long.value!!

        repository.set_GeoLocation(la,lo,_processing)
    }
}
