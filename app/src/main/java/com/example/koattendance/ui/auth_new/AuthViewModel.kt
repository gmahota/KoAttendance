package com.example.koattendance.ui.auth_new

import retrofit2.Call
import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.koattendance.api.*
import com.example.koattendance.api.AddHeaderInterceptor
import com.example.koattendance.api.ApiInterface
import com.example.koattendance.api.MessageResponse
import com.example.koattendance.data.Employee
import com.example.koattendance.helper.round
import com.example.koattendance.repository.AuthRepository
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.util.concurrent.TimeUnit

class AuthViewModel(application: Application) : AndroidViewModel(application){

    companion object {
        private const val TAG = "AuthView"
    }

    private val repository = AuthRepository.getInstance(application)

    private var database = Firebase?.database?.reference

    private  lateinit var dbUsers: DatabaseReference

    private val client = OkHttpClient.Builder()
            .addInterceptor(AddHeaderInterceptor())
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)
            .connectTimeout(40, TimeUnit.SECONDS)
            .build()


    private val myList: MutableList<Employee> = mutableListOf()

    val signInState = repository.getSignInState()

    private val _explanation3 = MutableLiveData<String>().apply {
        value = ""
    }
    val explanation3: LiveData<String> = _explanation3

    private val _text = MutableLiveData<String>().apply {
        value = "This is slideshow Fragment"
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
    fun PhoneVerify(phoneNumber: String) {
        try{
            val scode = 123456
            val smsCode = scode.round(6)

            repository.user_validationCode(smsCode.toString(),_processing)

            _processing.value = true

            PhoneVerifyUsendit(phoneNumber,smsCode);
            //makeSendSMSApiRequest()

        }catch (e: IOException){
            Log.e("app",e.printStackTrace().toString())

        }catch (e: Exception){
            Log.e("app",e.printStackTrace().toString())
        }

        _processing.value = false
    }

    private fun makeSendSMSApiRequest(fromNumber: String, toNumber: String, message: String) {

        val sendSMSapiInterface: ApiInterface = ApiClient.getClient().create(ApiInterface::class.java)

        val call: Call<MessageResponse> = sendSMSapiInterface.getMessageResponse(Config.ApiKey, Config.ApiSecret,
                fromNumber, toNumber, message)

        call.enqueue(object : Callback<MessageResponse?> {
            override fun onResponse(call: Call<MessageResponse?>?, response: Response<MessageResponse?>) {
                try {
//                    Log.d(TAG, java.lang.String.valueOf(response.code()))
//                    if (response.code() === 200) {
//                        Log.d(TAG, response.body().toString())
//                        Log.d(TAG, response.body().getMessages().toString())
//                        Log.d(TAG, response.body().getMessageCount())
//                        for (i in 0 until response.body().getMessageCount().length()) {
//                            Log.d(TAG, response.body().getMessages().get(i).getTo())
//                            Log.d(TAG, response.body().getMessages().get(i).getMessageId())
//                            Log.d(TAG, response.body().getMessages().get(i).getStatus())
//                            Log.d(TAG, response.body().getMessages().get(i).getRemainingBalance())
//                            Log.d(TAG, response.body().getMessages().get(i).getMessagePrice())
//                            Log.d(TAG, response.body().getMessages().get(i).getNetwork())
//                            displayResult = """
//                                TO: ${response.body().getMessages().get(i).getTo().toString()}
//                                Message-id: ${response.body().getMessages().get(i).getMessageId().toString()}
//                                Status: ${response.body().getMessages().get(i).getStatus().toString()}
//                                Remaining balance: ${response.body().getMessages().get(i).getRemainingBalance().toString()}
//                                Message price: ${response.body().getMessages().get(i).getMessagePrice().toString()}
//                                Network: ${response.body().getMessages().get(i).getNetwork().toString()}
//
//
//                                """.trimIndent()
//                        }
//                        messageAreaTV.setText(displayResult)
//                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    Log.e(TAG, e.localizedMessage)
                }
            }

            override fun onFailure(call: Call<MessageResponse?>?, t: Throwable) {
                Log.e(TAG, t.localizedMessage)
            }
        })
    }

    /// [START Send Phone Verify With USend]
    fun PhoneVerifyUsendit(phoneNumber: String,smsCode :Int ) {
        try{

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
        }catch (e: IOException){
            repository.user_validationCode("123456",_processing)
            Log.e("app",e.printStackTrace().toString())

        }catch (e: Exception){
            repository.user_validationCode("123456",_processing)
            Log.e("app",e.printStackTrace().toString())
        }
    }

    fun  Validation(codeValidation: String) {

        if(repository.user_Validated(codeValidation,_processing))
            _explanation3.value = "Validado com Sucesso!! Benvindo a Ko-Attendance!!"
        else
            _explanation3.value = "O Codigo Introduzido não é valido!!"
    }

    fun getUser(): Employee {
        return repository.get_User(_processing)
    }

    fun getAllUsers(callback: (list: List<Employee>) -> Unit) {
        dbUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(snapshotError: DatabaseError) {
                TODO("not implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val list : MutableList<Employee> = mutableListOf()
                val children = snapshot!!.children
                children.forEach {
                    it.getValue(Employee::class.java)?.let { it1 -> list.add(it1) }
                }
                //callback(list)
            }
        })
    }

    fun getUserFromPhoneNumber(phoneNumber: String){
        try
        {
            _explanation3.value = "Aguarde a validação no servidor do número acima"
            _processing.value = true

            var query = FirebaseDatabase.getInstance().getReference("employee")
                    .orderByChild("phoneNumber")
                    .equalTo(phoneNumber)

            var valueEventListener = object : ValueEventListener {
                override fun onCancelled(snapshotError: DatabaseError) {
                    TODO("not implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    myList.clear()
                    for (productSnapshot in snapshot!!.children) {

                        val func: Employee? = productSnapshot.getValue(Employee::class.java)
                        if (func != null) {
                            func.user = productSnapshot.key
                            func.phoneNumber = phoneNumber

                            myList.add(func)
                        }
                    }

                    if(myList.count() > 0){
                        val user = myList[0]

                        repository.set_User(user,_processing)

                        PhoneVerify(phoneNumber)

                        _explanation3.value = "Hi ${user.name}, we send a SMS to you phone to validate your number, insert the code on field - validation code"
                    }
                    else{
                        _explanation3.value = "Please check if your cellphone is connect to the internet and click again on Validate /n!" +
                                "O número introduzido não existe na base de dados Porfavor, verifique se colocou devidamente os dados o seu telefone e volte a tentar novamente!"

                    }
                    _processing.value = true
                    //snapshot!!.children.mapNotNullTo(myList) { it.getValue<Funcionarios>(Funcionarios::class.java) }
                }
            }
            query.run {
                addListenerForSingleValueEvent(valueEventListener)
            }
        }catch(ee: Exception)
        {
            Log.e("App", ee.message)
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
                        val func: Employee? = productSnapshot.getValue(Employee::class.java)
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
                    snapshot!!.children.mapNotNullTo(myList) { it.getValue<Employee>(Employee::class.java) }
                }
            })
        }
    }
}