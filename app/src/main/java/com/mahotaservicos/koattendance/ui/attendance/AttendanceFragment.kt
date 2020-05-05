package com.mahotaservicos.koattendance.ui.attendance

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle

import android.security.keystore.KeyProperties
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mahotaservicos.koattendance.R
import java.util.concurrent.Executor
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.widget.*
import androidx.core.app.ActivityCompat


class AttendanceFragment : Fragment() {

    private lateinit var attendanceViewModel: AttendanceViewModel

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        attendanceViewModel =
                ViewModelProviders.of(this).get(AttendanceViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_attendance, container, false)

        var txtData =root.findViewById(R.id.txt_Data) as TextView
        attendanceViewModel.text.observe(viewLifecycleOwner, Observer {
            txtData.text = it
        })

        var txtMsg =root.findViewById(R.id.txtOutros) as TextView
        attendanceViewModel.text_msg.observe(viewLifecycleOwner, Observer {
            txtMsg.text = it
        })

        var btn_login = root.findViewById(R.id.btn_login) as Button
        var btn_logout = root.findViewById(R.id.btn_logout) as Button

        var isValidated = attendanceViewModel.getUserIsValidaded()

        if(!isValidated){

            btn_login.isEnabled = false
            btn_logout.isEnabled = false

        }else{
            btn_login.isEnabled = true
            btn_logout.isEnabled = true
        }

        canAuthenticate()

        sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity)

        btn_login.setOnClickListener {
            initBiometric("Clock-In")
        }

        btn_logout.setOnClickListener{
            initBiometric("Clock-Out")

        }
        return root
    }

    private fun canAuthenticate(){
        val context: Context = context!!

        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e("MY_APP_TAG", "No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Log.e("MY_APP_TAG", "The user hasn't associated " +
                        "any biometric credentials with their account.")
        }
    }

    private  fun initBiometric(type:String){
        // clearing user_name and password edit text views on reset button click
        val context: Context = context!!
        executor = ContextCompat.getMainExecutor(context)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)

                    Toast.makeText(context,
                            "Authentication error: $errString", Toast.LENGTH_SHORT)
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)

                    attendanceViewModel.writeAttendance(type)


                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(context, "Authentication failed",
                            Toast.LENGTH_SHORT)
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Make your clock time with your biometric credential")
            .setDescription("Time Clock With Biometric.")
            .setNegativeButtonText("Cancel?")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    companion object {
        private const val TAG = "BiometricPrompt"
        private const val KEYSTORE = "AndroidKeyStore"
        private const val KEY_NAME = "MY_KEY"
        private const val DATA_ENCRYPTED = "DATA_ENCRYPTED"
        private const val INITIALIZATION_VECTOR = "INITIALIZATION_VECTOR"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private fun keyTransformation() = listOf(ALGORITHM, BLOCK_MODE, PADDING).joinToString(separator = "/")
    }
}
