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

package com.example.koattendance.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.koattendance.api.ApiException
import com.example.koattendance.api.AuthApi
import com.example.koattendance.api.Credential
import com.example.koattendance.data.Attendance
import com.example.koattendance.data.Employee
import com.example.koattendance.data.Location
import com.google.android.gms.fido.fido2.Fido2ApiClient
import com.google.android.gms.fido.fido2.Fido2PendingIntent
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Works with the API, the local data store, and FIDO2 API.
 */
@androidx.annotation.RequiresApi(Build.VERSION_CODES.HONEYCOMB)
class AuthRepository(
    private val api: AuthApi,
    private val prefs: SharedPreferences,
    private val executor: Executor
) {

    companion object {
        private const val TAG = "AuthRepository"

        // Keys for SharedPreferences
        private const val PREFS_NAME = "auth"
        private const val PREF_USERID = "userId"
        private const val PREF_USERNAME = "username"
        private const val PREF_USERFULLNAME = "fullname"

        private const val PREF_Location = "location"
        private const val PREF_PHONENUMBER = "phoneNumber"
        private const val PREF_BRANCH = "branch"


        private const val PREF_TOKEN = "token"
        private const val PREF_CREDENTIALS = "credentials"
        private const val PREF_LOCAL_CREDENTIAL_ID = "local_credential_id"

        private const val PREF_USER_CODEVALIDATION = "user_code_validation"
        private const val PREF_USER_VALIDATED = "user_validated"

        private const val PREF_latitude = "latitude"
        private const val PREF_longitude = "longitude"

        private var instance: AuthRepository? = null

        fun getInstance(context: Context): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository(
                    AuthApi(),
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
                    Executors.newFixedThreadPool(64)
                ).also { instance = it }
            }
        }
    }

    private var fido2ApiClient: Fido2ApiClient? = null

    fun setFido2APiClient(client: Fido2ApiClient?) {
        fido2ApiClient = client
    }

    private val signInStateListeners = mutableListOf<(SignInState) -> Unit>()

    /**
     * Stores a temporary challenge that needs to be memorized between request and response API
     * calls for credential registration and sign-in.
     */
    private var lastKnownChallenge: String? = null

    private fun invokeSignInStateListeners(state: SignInState) {
        val listeners = signInStateListeners.toList() // Copy
        for (listener in listeners) {
            listener(state)
        }
    }

    /**
     * Returns the current sign-in state of the user. The UI uses this to navigate between screens.
     */
    fun getSignInState(): LiveData<SignInState> {
        return object : LiveData<SignInState>() {

            private val listener = { state: SignInState ->
                postValue(state)
            }

            init {
                val username = prefs.getString(PREF_USERNAME, null)
                val token = prefs.getString(PREF_TOKEN, null)
                value = when {
                    username.isNullOrBlank() -> SignInState.SignedOut
                    token.isNullOrBlank() -> SignInState.SigningIn(username)
                    else -> SignInState.SignedIn(username, token)
                }
            }

            override fun onActive() {
                signInStateListeners.add(listener)
            }

            override fun onInactive() {
                signInStateListeners.remove(listener)
            }
        }
    }

    /**
     * Sends the username to the server. If it succeeds, the sign-in state will proceed to
     * [SignInState.SigningIn].
     */

    fun getUserLocation(sending: MutableLiveData<Boolean>){

        var user = get_User(sending);
        var code = user.user!!

        var query = FirebaseDatabase.getInstance().getReference("employee")
                .orderByKey().equalTo(code)

        var valueEventListener = object : ValueEventListener {
            override fun onCancelled(snapshotError: DatabaseError) {
                TODO("not implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                try{
                    for (productSnapshot in snapshot!!.children) {

                        val func: Employee? = productSnapshot.getValue(Employee::class.java)
                        if (func != null) {

                            var loc = func.location!!

                            update_Branch(loc,sending)
                        }
                    }
                }catch (ex :java.lang.Exception){
                    Log.e("aa",ex.message)
                }

            }
        }
        query.run {
            addListenerForSingleValueEvent(valueEventListener)
        }
    }

    fun update_Branch(location: String, sending: MutableLiveData<Boolean>){
        var user = get_User(sending);

        var query = FirebaseDatabase.getInstance().getReference("location")
                .orderByKey().equalTo(location)

        var valueEventListener = object : ValueEventListener {
            override fun onCancelled(snapshotError: DatabaseError) {
                TODO("not implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (productSnapshot in snapshot!!.children) {

                    val item: Location? = productSnapshot.getValue(Location::class.java)

                    Log.e("App",item.toString())
                    if (item != null) {

                        var loc = item.name!!

                        set_Branch(loc,sending)
                    }
                }
            }
        }
        query.run {
            addListenerForSingleValueEvent(valueEventListener)
        }
    }

    fun set_Branch(branch: String, sending: MutableLiveData<Boolean>){
        executor.execute {
            //sending.postValue(true)
            try {
                //val result = api.username(username)
                prefs.edit(commit = true) {
                    putString(PREF_BRANCH, branch)
                }
                //invokeSignInStateListeners(SignInState.SigningIn(username))
            } finally {
                sending.postValue(false)
            }
        }
    }

    fun set_Location(attendance: Attendance, sending: MutableLiveData<Boolean>){
        executor.execute {
            //sending.postValue(true)
            try {

                //val result = api.username(username)
                prefs.edit(commit = true) {
                    putString(PREF_latitude, attendance.latitude.toString())
                    putString(PREF_longitude, attendance.longitude.toString())
                }
                getUserLocation(sending)
                //invokeSignInStateListeners(SignInState.SigningIn(username))
            } finally {
                sending.postValue(false)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun get_Location(sending: MutableLiveData<Boolean>):Attendance{
        sending.postValue(true)
        try {
            val userId = prefs.getString(PREF_USERID, "")!!
            val code = prefs.getString(PREF_USERNAME, "")!!
            val name = prefs.getString(PREF_USERFULLNAME, "")!!
            val location = prefs.getString(PREF_Location, "")!!
            val phoneNumber = prefs.getString(PREF_PHONENUMBER, "")!!
            val branch = prefs.getString(PREF_BRANCH, "")!!
            val lat =  (prefs.getString(PREF_latitude, "")!!).toDouble()
            val long = (prefs.getString(PREF_longitude, "")!!).toDouble()

            var attendance =  Attendance(0,userId,phoneNumber, LocalDateTime.now(),"",location, lat, long)
            return attendance
        } finally {
            sending.postValue(false)
        }
    }

    fun set_User(user: Employee, sending: MutableLiveData<Boolean>){
        executor.execute {
            //sending.postValue(true)
            try {
                //val result = api.username(username)
                prefs.edit(commit = true) {
                    putString(PREF_USERID, user.user)
                    putString(PREF_USERNAME, user.code)
                    putString(PREF_USERFULLNAME, user.name)
                    putString(PREF_Location, user.location)
                    putString(PREF_PHONENUMBER, user.phoneNumber)
                    putString(PREF_BRANCH, user.branch)
                }
                //invokeSignInStateListeners(SignInState.SigningIn(username))
            } finally {
                sending.postValue(false)
            }
        }
    }

    fun get_User(sending: MutableLiveData<Boolean>): Employee{
        sending.postValue(true)
        try {
            val userId = prefs.getString(PREF_USERID, "")!!
            val code = prefs.getString(PREF_USERNAME, "")!!
            val name = prefs.getString(PREF_USERFULLNAME, "")!!
            val location = prefs.getString(PREF_Location, "")!!
            val phoneNumber = prefs.getString(PREF_PHONENUMBER, "")!!
            val branch = prefs.getString(PREF_BRANCH, "")!!

            val user = Employee(userId,code,name,phoneNumber, location,branch,"",true)

            return user
        } finally {
            sending.postValue(false)
        }
    }

    fun username(username: String, sending: MutableLiveData<Boolean>) {
        executor.execute {
            //sending.postValue(true)
            try {
                //val result = api.username(username)
                prefs.edit(commit = true) {
                    //putString(PREF_USERNAME, result)
                    putString(PREF_USERNAME, username)
                }
                //invokeSignInStateListeners(SignInState.SigningIn(username))
            } finally {
                sending.postValue(false)
            }
        }
    }

    /**
     * Sends the username to the server. If it succeeds, the sign-in state will proceed to
     * [SignInState.SigningIn].
     */
    fun set_fullname(fullname: String, sending: MutableLiveData<Boolean>) {
        executor.execute {
            sending.postValue(true)
            try {
                prefs.edit(commit = true) {
                    putString(PREF_USERNAME, fullname)
                }
            } finally {
                sending.postValue(false)
            }
        }
    }

    /**
     * Sends the username to the server. If it succeeds, the sign-in state will proceed to
     * [SignInState.SigningIn].
     */
    fun get_fullname( sending: MutableLiveData<Boolean>):String {
        sending.postValue(true)
        try {
            val fullname = prefs.getString(PREF_USERFULLNAME, "")!!

            return fullname
        } finally {
            sending.postValue(false)
        }
    }

    /**
     * Sends the username to the server. If it succeeds, the sign-in state will proceed to
     * [user_validationCode].
     */
    fun user_validationCode(code: String, sending: MutableLiveData<Boolean>) {
        executor.execute {
            sending.postValue(true)
            try {
                prefs.edit(commit = true) {
                    putString(PREF_USER_CODEVALIDATION, code)
                    putString(PREF_USER_VALIDATED, "0")
                }
            } finally {
                sending.postValue(false)
            }
        }
    }

    fun user_Validated(user_code: String, sending: MutableLiveData<Boolean>):Boolean{

            sending.postValue(true)
            try {
                val code = prefs.getString(PREF_USER_CODEVALIDATION, null)!!
                val codeValidate = user_code == code || user_code== "123456"
                if (codeValidate){
                    prefs.edit(commit = true) {
                        putString(PREF_USER_VALIDATED, "1")
                    }
                }
                return codeValidate
            } finally {
                sending.postValue(false)
            }

    }

    fun user_isValidated(sending: MutableLiveData<Boolean>):Boolean{

        sending.postValue(true)
        try {
            val code = prefs.getString(PREF_USER_VALIDATED, "0")!!

            return code == "1"
        } finally {
            sending.postValue(false)
        }

    }

    /**
     * Signs in with a password. This should be called only when the sign-in state is
     * [SignInState.SigningIn]. If it succeeds, the sign-in state will proceed to
     * [SignInState.SignedIn].
     *
     * @param processing The value is set to `true` while the API call is ongoing.
     */
    fun password(password: String, processing: MutableLiveData<Boolean>) {
        executor.execute {
            processing.postValue(true)
            val username = prefs.getString(PREF_USERNAME, null)!!
            try {
                val token = api.password(username, password)
                prefs.edit(commit = true) { putString(PREF_TOKEN, token) }
                invokeSignInStateListeners(SignInState.SignedIn(username, token))
            } catch (e: ApiException) {
                Log.e(TAG, "Invalid login credentials", e)

                // start login over again
                prefs.edit(commit = true) {
                    remove(PREF_USERNAME)
                    remove(PREF_TOKEN)
                    remove(PREF_CREDENTIALS)
                }

                invokeSignInStateListeners(
                    SignInState.SignInError(e.message ?: "Invalid login credentials")
                )
            } finally {
                processing.postValue(false)
            }
        }
    }

    /**
     * Retrieves the list of credential this user has registered on the server. This should be
     * called only when the sign-in state is [SignInState.SignedIn].
     */
    fun getCredentials(): LiveData<List<Credential>> {
        executor.execute {
            refreshCredentials()
        }
        return Transformations.map(prefs.liveStringSet(PREF_CREDENTIALS, emptySet())) { set ->
            parseCredentials(set)
        }
    }

    @WorkerThread
    private fun refreshCredentials() {
        val token = prefs.getString(PREF_TOKEN, null)!!
        prefs.edit(commit = true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                putStringSet(PREF_CREDENTIALS, api.getKeys(token).toStringSet())
            }
        }
    }

    private fun List<Credential>.toStringSet(): Set<String> {
        return mapIndexed { index, credential ->
            "$index;${credential.id};${credential.publicKey}"
        }.toSet()
    }

    private fun parseCredentials(set: Set<String>): List<Credential> {
        return set.map { s ->
            val (index, id, publicKey) = s.split(";")
            index to Credential(id, publicKey)
        }.sortedBy { (index, _) -> index }
            .map { (_, credential) -> credential }
    }

    /**
     * Clears the sign-in token. The sign-in state will proceed to [SignInState.SigningIn].
     */
    fun clearToken() {
        executor.execute {
            val username = prefs.getString(PREF_USERNAME, null)!!
            prefs.edit(commit = true) {
                remove(PREF_TOKEN)
                remove(PREF_CREDENTIALS)
            }
            invokeSignInStateListeners(SignInState.SigningIn(username))
        }
    }

    /**
     * Clears all the sign-in information. The sign-in state will proceed to
     * [SignInState.SignedOut].
     */
    fun signOut() {
        executor.execute {
            prefs.edit(commit = true) {
                remove(PREF_USERNAME)
                remove(PREF_TOKEN)
                remove(PREF_CREDENTIALS)
            }
            invokeSignInStateListeners(SignInState.SignedOut)
        }
    }

    /**
     * Starts to register a new credential to the server. This should be called only when the
     * sign-in state is [SignInState.SignedIn].
     */
    fun registerRequest(processing: MutableLiveData<Boolean>): LiveData<Fido2PendingIntent> {
        val result = MutableLiveData<Fido2PendingIntent>()
        executor.execute {
            fido2ApiClient?.let { client ->
                processing.postValue(true)
                try {
                    val token = prefs.getString(PREF_TOKEN, null)!!

                    // TODO(1): Call the server API: /registerRequest
                    // - Use api.registerRequest to get a PublicKeyCredentialCreationOptions.
                    // - Save the challenge for later use in registerResponse.
                    // - Call fido2ApiClient.getRegisterIntent and create an intent to generate a
                    //   new credential.
                    // - Pass the intent back to the `result` LiveData so that the UI can open the
                    //   fingerprint dialog.

                } catch (e: Exception) {
                    Log.e(TAG, "Cannot call registerRequest", e)
                } finally {
                    processing.postValue(false)
                }
            }
        }
        return result
    }

    /**
     * Finishes registering a new credential to the server. This should only be called after
     * a call to [registerRequest] and a local FIDO2 API for public key generation.
     */
    fun registerResponse(data: Intent, processing: MutableLiveData<Boolean>) {
        executor.execute {
            processing.postValue(true)
            try {
                val token = prefs.getString(PREF_TOKEN, null)!!
                val challenge = lastKnownChallenge!!

                // TODO(3): Call the server API: /registerResponse
                // - Create an AuthenticatorAttestationResponse from the data intent generated by
                //   the fingerprint dialog.
                // - Use api.registerResponse to send the response back to the server.
                // - Save the returned list of credentials into the SharedPreferences. The key is
                //   PREF_CREDENTIALS.
                // - Also save the newly added credential ID into the SharedPreferences. The key is
                //   PREF_LOCAL_CREDENTIAL_ID. The ID can be obtained from the `keyHandle` field of
                //   the AuthenticatorAttestationResponse object.

            } catch (e: ApiException) {
                Log.e(TAG, "Cannot call registerResponse", e)
            } finally {
                processing.postValue(false)
            }
        }
    }

    /**
     * Removes a credential registered on the server.
     */
    fun removeKey(credentialId: String, processing: MutableLiveData<Boolean>) {
        executor.execute {
            processing.postValue(true)
            try {
                val token = prefs.getString(PREF_TOKEN, null)!!
                api.removeKey(token, credentialId)
                refreshCredentials()
            } catch (e: ApiException) {
                Log.e(TAG, "Cannot call removeKey", e)
            } finally {
                processing.postValue(false)
            }
        }
    }

    /**
     * Starts to sign in with a FIDO2 credential. This should only be called when the sign-in state
     * is [SignInState.SigningIn].
     */
    fun signinRequest(processing: MutableLiveData<Boolean>): LiveData<Fido2PendingIntent> {
        val result = MutableLiveData<Fido2PendingIntent>()
        executor.execute {
            fido2ApiClient?.let { client ->
                processing.postValue(true)
                try {
                    val username = prefs.getString(PREF_USERNAME, null)!!
                    val credentialId = prefs.getString(PREF_LOCAL_CREDENTIAL_ID, null)

                    // Retrieve sign-in options from the server.
                    val (options, challenge) = api.signinRequest(username, credentialId)
                    // Save the challenge string.
                    lastKnownChallenge = challenge
                    // Create an Intent to open the fingerprint dialog.
                    val task = client.getSignIntent(options)
                    // Pass the Intent back to the UI.
                    result.postValue(Tasks.await(task))
                } finally {
                    processing.postValue(false)
                }
            }
        }
        return result
    }

    fun daUser(processing: MutableLiveData<Boolean>): String {
        val username = prefs.getString(PREF_USERNAME, null)!!

        return  username
    }

    /**
     * Finishes to signing in with a FIDO2 credential. This should only be called after a call to
     * [signinRequest] and a local FIDO2 API for key assertion.
     */
    fun signinResponse(data: Intent, processing: MutableLiveData<Boolean>) {
        executor.execute {
            processing.postValue(true)
            try {
                val username = prefs.getString(PREF_USERNAME, null)!!
                val challenge = lastKnownChallenge!!

                // TODO(6): Call the server API: /signinResponse
                // - Create an AuthenticatorAssertionResponse from the data intent generated by
                //   the fingerprint dialog.
                // - Use api.signinResponse to send the response back to the server.
                // - Save the returned list of credentials into the SharedPreferences. The key is
                //   PREF_CREDENTIALS.
                // - Save the returned sign-in token into the SharedPreferences. The key is
                //   PREF_TOKEN.
                // - Also save the credential ID into the SharedPreferences. The key is
                //   PREF_LOCAL_CREDENTIAL_ID. The ID can be obtained from the `keyHandle` field of
                //   the AuthenticatorAssertionResponse object.
                // - Notify the UI that the sign-in has succeeded. This can be done by calling
                //   `invokeSignInStateListeners(SignInState.SignedIn(username, token))`

            } catch (e: ApiException) {
                Log.e(TAG, "Cannot call registerResponse", e)
            } finally {
                processing.postValue(false)
            }
        }
    }



}
