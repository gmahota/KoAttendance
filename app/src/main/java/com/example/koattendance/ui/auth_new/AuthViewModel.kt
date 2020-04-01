package com.example.koattendance.ui.auth_new

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.koattendance.api.AddHeaderInterceptor
import com.example.koattendance.data.Funcionarios
import com.example.koattendance.repository.AuthRepository
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.net.URL
import java.util.concurrent.TimeUnit


class AuthViewModel(application: Application) : AndroidViewModel(application){

    private val repository = AuthRepository.getInstance(application)

    private var database = Firebase?.database?.reference

    private  lateinit var dbUsers: DatabaseReference

    private val client = OkHttpClient.Builder()
            .addInterceptor(AddHeaderInterceptor())
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)
            .connectTimeout(40, TimeUnit.SECONDS)
            .build()


    private val myList: MutableList<Funcionarios> = mutableListOf()

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

    val phone = MutableLiveData<String>()

    val validationCode = MutableLiveData<String>()

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

    /// [START Send Phone Verify With USend]
    fun PhoneVerify(phoneNumber: String,smsCode :Int ) {
        try{

            repository.user_validationCode(smsCode.toString(),_processing)

            _processing.value = true

            var str_url = "https://api.usendit.co.mz/v2/remoteusendit.asmx/SendMessage?";

            val username = "mahotag"
            val password = "AgnesZoe1518!"

            Log.e("app",phoneNumber)
            Log.e("app",smsCode.toString())

            val msisdn = "258$phoneNumber"

            var mobileOperator =phoneNumber.substring(0, 2)

            when(mobileOperator){
                "82" -> mobileOperator = "23"
                "84" -> mobileOperator = "22"
                "85" -> mobileOperator = "22"
                "86" -> mobileOperator = "21"
                "87" -> mobileOperator = "21"
                else -> mobileOperator = mobileOperator
            }
            Log.e("app",phoneNumber)
            str_url += "username=$username"
            str_url += "&password=$password"
            str_url +="&partnerEventId="
            str_url +="&timezone="
            str_url +="&partnerMsgId="
            str_url +="&partnerMsgId="
            str_url += "&sender=uSendit"
            str_url +="&msisdn=$msisdn"
            str_url +="&mobileOperator=$mobileOperator"
            str_url +="&priority=0"
            str_url+="&expirationDatetime="
            str_url+="&messageText=$smsCode é o seu código de validação para KoAttendance!"
            str_url+="&scheduleDatetime="
            str_url+="&beginTime="
            str_url+="&endTime="
            str_url+="&workingDays=false"
            str_url += "&isFlash=false"

            Log.d("app", "body $str_url")

            val request = Request.Builder()
                    .url(URL(str_url))
                    .build()

            val response = client.newCall(request).execute()

            Log.d("app","body " + response.body?.string())

        }catch (e: IOException){
            repository.user_validationCode("123456",_processing)
            Log.e("app",e.printStackTrace().toString())

        }catch (e: Exception){
            repository.user_validationCode("123456",_processing)
            Log.e("app",e.printStackTrace().toString())
        }

        _processing.value = false
    }

    fun  GetValidation(code: String) : Boolean{
        return repository.user_Validated(code,_processing);
    }



    // [START initialize_database_ref]

    // [END initialize_database_ref]

    fun getUser(): String {
        return repository.get_User(_processing).toString()
    }

    fun getAllUsers(callback: (list: List<Funcionarios>) -> Unit) {
        dbUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(snapshotError: DatabaseError) {
                TODO("not implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val list : MutableList<Funcionarios> = mutableListOf()
                val children = snapshot!!.children
                children.forEach {
                    it.getValue(Funcionarios::class.java)?.let { it1 -> list.add(it1) }
                }
                //callback(list)
            }
        })
    }

    fun getUserFromPhoneNumber(phoneNumber: String):Boolean{

        dbUsers = FirebaseDatabase.getInstance().getReference("funcionarios")
                //.orderByChild("telefone").equalTo(phoneNumber)

        var query = FirebaseDatabase.getInstance().getReference("funcionarios")
                .orderByChild("Telefone")
                .equalTo(phoneNumber)

        var valueEventListener = object : ValueEventListener {
            override fun onCancelled(snapshotError: DatabaseError) {
                TODO("not implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                myList.clear()
                for (productSnapshot in snapshot!!.children) {
                    val func: Funcionarios? = productSnapshot.getValue(Funcionarios::class.java)
                    if (func != null) {

                        func.phoneNumber = phoneNumber

                        myList.add(func)
                    }
                }
                //snapshot!!.children.mapNotNullTo(myList) { it.getValue<Funcionarios>(Funcionarios::class.java) }
            }
        }
        query.run {
            addListenerForSingleValueEvent(valueEventListener)
        }

        //getListUserFromPhone()

        Log.e("ap",myList.toString())

        return if(myList.count() > 0){
            val user = myList[0]


            repository.set_User(user,_processing)

            Log.e("App", user.toString())
            true
        }
        else{
            false
        }

    }

    private fun getListUserFromPhone() {

        dbUsers.run {
            addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(snapshotError: DatabaseError) {
                    TODO("not implemented")
                }
                override fun onDataChange(snapshot: DataSnapshot) {

                    myList.clear()

                    for (productSnapshot in snapshot!!.children) {
                        val func: Funcionarios? = productSnapshot.getValue(Funcionarios::class.java)
                        if (func != null) {
                            myList.add(func)
                        }
                    }
                }
            })
        }
    }

    private fun getUserFromPhone() {

        dbUsers.run {
            addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(snapshotError: DatabaseError) {
                    TODO("not implemented")
                }
                override fun onDataChange(snapshot: DataSnapshot) {

                    myList.clear()
                    snapshot!!.children.mapNotNullTo(myList) { it.getValue<Funcionarios>(Funcionarios::class.java) }
                }
            })
        }
    }

    private fun old(){
        //        val childEventListener = object : ChildEventListener  {
//            override fun onChildAdded(p0: DataSnapshot, previousChildName: String?) {
//                // A new message has been added
//                // onChildAdded() will be called for each node at the first time
//                val message = p0!!.getValue(Funcionarios::class.java)
//                myList.add(message!!)
//
//            }
//
//            override fun onChildChanged(p0: DataSnapshot, previousChildName: String?) {
//                Log.e("AuthFragment.TAG", "onChildChanged:" + p0!!.key)
//
//                // A message has changed
//                val message = p0.getValue(Funcionarios::class.java)
//
//                //Toast.makeText(this@MessageActivity, "onChildChanged: " + message!!.body, Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onChildRemoved(p0: DataSnapshot) {
//                Log.e("AuthFragment.TAG", "onChildRemoved:" + p0!!.key)
//
//                // A message has been removed
//                val message = p0.getValue(Funcionarios::class.java)
//                //Toast.makeText(this@MessageActivity, "onChildRemoved: " + message!!.body, Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onChildMoved(p0: DataSnapshot, previousChildName: String?) {
//                Log.e("AuthFragment.TAG", "onChildMoved:" + p0!!.key)
//
//                // A message has changed position
//                val message = p0.getValue(Funcionarios::class.java)
//
//                //Toast.makeText(activity, "onChildMoved: " + message!!.body, Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onCancelled(p0: DatabaseError) {
//                Log.e("AuthFragment.TAG", "postMessages:onCancelled", p0!!.toException())
//
//                //Toast.makeText(this@MessageActivity, "Failed to load Message.", Toast.LENGTH_SHORT).show()
//            }
//
//        }
//        dbUsers!!.addChildEventListener(childEventListener)

        // .on("child_added",function(snapshot) {
        // console.log(snapshot.key);
        //})
    }
}