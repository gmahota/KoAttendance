package com.mahotaservicos.koattendance.ui.auth

import android.os.Build
import android.os.Bundle
import android.os.StrictMode
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
import com.mahotaservicos.koattendance.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.N)
class AuthFragment: Fragment() {
    companion object {
        private const val TAG = "AuthFragment"
    }

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        var _Context = context!!
        FirebaseApp.initializeApp(_Context)

        authViewModel = ViewModelProviders.of(this).get(AuthViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_auth, container, false)

        authViewModel.processing.observe(viewLifecycleOwner, Observer {
            processing ->
            val processingDial = root.findViewById(R.id.processing) as ContentLoadingProgressBar
            if (processing) {
                processingDial.show()

            } else {
                processingDial.hide()

            }
        })

        var txt_explanation3= root.findViewById(R.id.explanation3) as TextView

        authViewModel.explanation3.observe(viewLifecycleOwner, Observer {
            txt_explanation3.text = it
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

        var txt_phoneNumber = root.findViewById(R.id.input_phone) as TextInputEditText
        var txt_code= root.findViewById(R.id.input_validationCode) as TextInputEditText

        btn_validation.setOnClickListener {
            var code = txt_code.text.toString()

            if(code.isNotEmpty()) {
                authViewModel.validation(code)
            }else{
                val phoneNumber = txt_phoneNumber.text.toString()

                authViewModel.getUserFromPhoneNumber(phoneNumber)
            }
        }
        /// End Phone Validation
        return root
    }

    override fun onStart() {
        super.onStart()
    }
}
