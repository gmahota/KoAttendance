package com.example.koattendance.ui.auth_new

import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.koattendance.R
import com.example.koattendance.data.Funcionarios
import com.example.koattendance.helper.round
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.N)
class AuthFragment: Fragment() {
    companion object {
        private const val TAG = "AuthFragment"
        const val REQUEST_FIDO2_REGISTER = 1
        const val REQUEST_FIDO2_SIGNIN = 2
    }

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private lateinit var authViewModel: AuthViewModel

    private var database = Firebase?.database?.reference

    private  lateinit var dbUsers: DatabaseReference
    private var mMessageListener: ChildEventListener ? = null
    val messageList = ArrayList<Funcionarios>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        var _Context = context!!
        FirebaseApp.initializeApp(_Context)

        authViewModel =
            ViewModelProviders.of(this).get(AuthViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_auth, container, false)

        authViewModel.processing.observe(viewLifecycleOwner, Observer {
            processing ->
            val processingDial = root.findViewById(R.id.processing) as ContentLoadingProgressBar
            if (processing) {
                processingDial.show()
                Log.d(  "sendUsername", "3" )

                //Handler().postDelayed({ startActivity( Intent(context, AuthFragment::class.java)) }, 4000L)
            } else {
                processingDial.hide()
                Log.d(  "sendUsername", "4" )
                //Handler().postDelayed({ startActivity( Intent(context, AuthFragment::class.java)) }, 4000L)
            }
        })

        var btn_sign_in = root.findViewById(R.id.btn_sign_in) as Button

        btn_sign_in.setOnClickListener {
            val userText: TextView = root.findViewById(R.id.input_username)
            val passwordText: TextView = root.findViewById(R.id.input_password)
            authViewModel.username.value = userText.text.toString()
            authViewModel.password.value = passwordText.text.toString()
            authViewModel.auth()
        }

        /// Begin Phone Validation
        val auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("pt")

        var btn_validation = root.findViewById(R.id.btn_validate) as Button
        var txt_validationCode = root.findViewById(R.id.input_validationCode) as TextInputEditText

        var txt_phoneNumber = root.findViewById(R.id.input_phone) as TextInputEditText
        var txt_code= root.findViewById(R.id.input_validationCode) as TextInputEditText

        var txt_explanation3= root.findViewById(R.id.explanation3) as TextView

        val processingDial = root.findViewById(R.id.processing) as ContentLoadingProgressBar

        //database = Firebase.database.reference

        //dbUsers = FirebaseDatabase.getInstance().getReference("funcionarios")

        //firebaseListenerInit()

        btn_validation.setOnClickListener {
            var code = txt_code.text.toString()

            if(code.isNotEmpty()) {
                var validation = authViewModel.GetValidation(code)

                if(validation)
                    txt_explanation3.text = "Validado com Sucesso!! Benvindo a Ko-Attendance!!" //+ System.lineSeparator() + "Valide estes são os seus dados de acesso: " + authViewModel.getUser()
                else
                    txt_explanation3.text = "O Codigo Introduzido não é valido!!"
            }else{
                txt_explanation3.text = "Aguarde a validação no servidor do número acima"

                processingDial.show()


                txt_validationCode.isEnabled = true
                val phoneNumber = txt_phoneNumber.text.toString()

                val scode = 123456
                val smsCode = scode.round(6)

                if( authViewModel.getUserFromPhoneNumber(phoneNumber)){
                    //testPhoneAutoRetrieve(phoneNumber)
                    authViewModel.PhoneVerify(phoneNumber,smsCode)

                    processingDial.hide()

                    txt_explanation3.text = getString(R.string.validationAlert)
                }else{
                    txt_explanation3.text = "Verifique se encontra-se ligado a internet ou o seu número não se encontra registrado na base de dados!"
                }


            }

        }
        /// End Phone Validation
        return root
    }

    private fun firebaseListenerInit() {
        val childEventListener = object : ChildEventListener  {
            override fun onChildAdded(p0: DataSnapshot, previousChildName: String?) {
                // A new message has been added
                // onChildAdded() will be called for each node at the first time
                val message = p0!!.getValue(Funcionarios::class.java)
                messageList.add(message!!)

            }

            override fun onChildChanged(p0: DataSnapshot, previousChildName: String?) {
                Log.e(TAG, "onChildChanged:" + p0!!.key)

                // A message has changed
                val message = p0.getValue(Funcionarios::class.java)

                //Toast.makeText(this@MessageActivity, "onChildChanged: " + message!!.body, Toast.LENGTH_SHORT).show()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                Log.e(TAG, "onChildRemoved:" + p0!!.key)

                // A message has been removed
                val message = p0.getValue(Funcionarios::class.java)
                //Toast.makeText(this@MessageActivity, "onChildRemoved: " + message!!.body, Toast.LENGTH_SHORT).show()
            }

            override fun onChildMoved(p0: DataSnapshot, previousChildName: String?) {
                Log.e(TAG, "onChildMoved:" + p0!!.key)

                // A message has changed position
                val message = p0.getValue(Funcionarios::class.java)

                //Toast.makeText(activity, "onChildMoved: " + message!!.body, Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.e(TAG, "postMessages:onCancelled", p0!!.toException())

                //Toast.makeText(this@MessageActivity, "Failed to load Message.", Toast.LENGTH_SHORT).show()
            }

        }

        dbUsers!!.addChildEventListener(childEventListener)

        // copy for removing at onStop()
        mMessageListener = childEventListener
    }

    override fun onStart() {
        super.onStart()
    }

    // [START Firebase_testPhoneVerify]
    // Call this both in the silent sign-in task's OnCompleteListener and in the
    // Activity's onActivityResult handler.
    private fun testPhoneVerify() {
        // [START auth_test_phone_verify]
        val phoneNumber = "+258849535156"
        val testVerificationCode = "123456"
        val activity = activity!!
        // Whenever verification is triggered with the whitelisted number,
        // provided it is not set for auto-retrieval, onCodeSent will be triggered.
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, 30L /*timeout*/, TimeUnit.SECONDS,
                activity, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onCodeSent(
                    verificationId: String,
                    forceResendingToken: PhoneAuthProvider.ForceResendingToken
            ) {
                // Save the verification id somewhere
                // ...

                // The corresponding whitelisted code above should be used to complete sign-in.
                enableUserManuallyInputCode()
            }

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                // Sign in with the credential
                // ...
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // ...
            }
        })
        // [END auth_test_phone_verify]
    }

    fun enableUserManuallyInputCode() {
        // No-op
    }

    private fun testPhoneAutoRetrieve(phoneNumber: String) {
        // [START auth_test_phone_auto]
        // The test phone number and code should be whitelisted in the console.
        val activity = activity!!

        val number = "+258$phoneNumber"

        val scode = 1
        val smsCode = scode.round(6)

        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseAuthSettings = firebaseAuth.firebaseAuthSettings

        // Configure faking the auto-retrieval with the whitelisted numbers.
        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(number, smsCode.toString())

        val phoneAuthProvider = PhoneAuthProvider.getInstance()
        phoneAuthProvider.verifyPhoneNumber(
                number,
                60L,
                TimeUnit.SECONDS,
                activity, /* activity */
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        // Instant verification is applied and a credential is directly returned.
                        // ...
                        //authViewModel.PhoneVerify(phoneNumber,smsCode);

                        Log.d(TAG, "onVerificationCompleted:$credential")
                    }

                    // [START_EXCLUDE]
                    override fun onVerificationFailed(e: FirebaseException) {
                        // This callback is invoked in an invalid request for verification is made,
                        // for instance if the the phone number format is not valid.
                        Log.w(TAG, "onVerificationFailed", e)

                        if (e is FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            // ...
                        } else if (e is FirebaseTooManyRequestsException) {
                            // The SMS quota for the project has been exceeded
                            // ...
                        }

                        // Show a message and update the UI
                        // ...
                    }
                    // [END_EXCLUDE]


                    override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        // The SMS verification code has been sent to the provided phone number, we
                        // now need to ask the user to enter the code and then construct a credential
                        // by combining the code with a verification ID.

                        Log.d(TAG, "onCodeSent:$verificationId")

                        // Save verification ID and resending token so we can use them later
                        storedVerificationId = verificationId
                        resendToken = token

                        // ...
                    }
                })
        // [END auth_test_phone_auto]
    }

    /// [END Firebase_testPhoneVerify]

//    private fun showFragment(clazz: Class<out Fragment>, create: () -> Fragment) {
//        val manager = supportFragmentManager
//
//        if (!clazz.isInstance(manager.findFragmentById(R.id.container))) {
//            var transaction = manager.beginTransaction()
//            transaction.replace(R.id.container, create())
//            transaction.commit()
//        }
//    }


    /// [END Send Phone Verify With USend]

}
