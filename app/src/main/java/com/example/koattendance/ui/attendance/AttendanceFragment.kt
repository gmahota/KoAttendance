package com.example.koattendance.ui.attendance

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.koattendance.KEY_NAME
import com.example.koattendance.R
import java.security.Key
import java.security.KeyStore
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

import android.content.SharedPreferences

import android.util.Base64

import androidx.core.content.edit
import com.example.koattendance.DEFAULT_KEY_NAME

import java.util.concurrent.Executors


@RequiresApi(Build.VERSION_CODES.M)
class AttendanceFragment : Fragment() {

    private lateinit var attendanceViewModel: AttendanceViewModel

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private lateinit var sharedPreferences: SharedPreferences
    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        attendanceViewModel =
                ViewModelProviders.of(this).get(AttendanceViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_attendance, container, false)
        val textView: TextView = root.findViewById(R.id.text_gallery)
        attendanceViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        var btn_login = root.findViewById(R.id.btn_login) as Button
        var btn_logout = root.findViewById(R.id.btn_logout) as Button

        canAuthenticate()
        //generateSecretKey_bio()
        //initBiometric()

        sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity)

        btn_login.setOnClickListener {

            encryptPrompt(
                data = DEFAULT_KEY_NAME.toByteArray(),
                failedAction = { Log.e("e","encrypt failed") },
                successAction = {
                    textView.text = String(it)
                    Log.d("App","encrypt success")
                }
            )
//            // Exceptions are unhandled within this snippet.
//            val cipher = getCipher()
//
//            val secretKey = getSecretKey()
//
//            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
//
//            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }

        btn_logout.setOnClickListener{
            decryptPrompt(
                failedAction = { Log.e("App","decrypt failed") },
                successAction = {
                    textView.text = String(it)
                    Log.d("App","decrypt success")
                }
            )
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

    private  fun initBiometric(){
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
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)




//                    val encryptedInfo: ByteArray? = result.cryptoObject?.cipher?.doFinal(
//                        KEY_NAME.toByteArray(Charset.defaultCharset())
//                    )
//                    Log.d("MY_APP_TAG", "Encrypted information: " +
//                            Arrays.toString(encryptedInfo))

                    Toast.makeText(context,
                            "Authentication succeeded!",  Toast.LENGTH_SHORT)
                        .show()

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
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.

        //val biometricLoginButton = findViewById<Button>(R.id.biometric_login)
        //biometricLoginButton.setOnClickListener {
        //    biometricPrompt.authenticate(promptInfo)

    }

    fun decryptPrompt(failedAction: () -> Unit, successAction: (ByteArray) -> Unit) {
        try {
            val secretKey = getKey()
            val initializationVector = getInitializationVector()
            if (secretKey != null && initializationVector != null) {
                val cipher = getDecryptCipher(secretKey, initializationVector)
                handleDecrypt(cipher, failedAction, successAction)
            } else {
                failedAction()
            }
        } catch (e: Exception) {
            Log.d(TAG, "Decrypt BiometricPrompt exception", e)
            failedAction()
        }
    }

    fun encryptPrompt(
        data: ByteArray,
        failedAction: () -> Unit,
        successAction: (ByteArray) -> Unit
    ) {
        try {
            val secretKey = createKey()
            val cipher = getEncryptCipher(secretKey)
            handleEncrypt(cipher, data, failedAction, successAction)
        } catch (e: Exception) {
            Log.d(TAG, "Encrypt BiometricPrompt exception", e)
            failedAction()
        }
    }

    private fun getKey(): Key? = keyStore.getKey(KEY_NAME, null)

    private fun createKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM, KEYSTORE)
        val keyGenParameterSpec =
            KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(BLOCK_MODE)
                .setEncryptionPaddings(PADDING)
                .setUserAuthenticationRequired(true)
                .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    private fun getInitializationVector(): ByteArray? {
        val iv = sharedPreferences.getString(INITIALIZATION_VECTOR, null)
        return when {
            iv != null -> Base64.decode(iv, Base64.DEFAULT)
            else -> null
        }
    }

    private fun getEncryptedData(): ByteArray? {
        val iv = sharedPreferences.getString(DATA_ENCRYPTED, null)
        return when {
            iv != null -> Base64.decode(iv, Base64.DEFAULT)
            else -> null
        }
    }

    private fun saveEncryptedData(dataEncrypted: ByteArray, initializationVector: ByteArray) {
        sharedPreferences.edit {
            putString(DATA_ENCRYPTED, Base64.encodeToString(dataEncrypted, Base64.DEFAULT))
            putString(INITIALIZATION_VECTOR, Base64.encodeToString(initializationVector, Base64.DEFAULT))
        }
    }

    private fun getDecryptCipher(key: Key, iv: ByteArray): Cipher =
        Cipher.getInstance(keyTransformation()).apply { init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv)) }

    private fun getEncryptCipher(key: Key): Cipher =
        Cipher.getInstance(keyTransformation()).apply { init(Cipher.ENCRYPT_MODE, key) }

    private fun handleDecrypt(
        cipher: Cipher,
        failedAction: () -> Unit,
        successAction: (ByteArray) -> Unit
    ) {

        val executor = Executors.newSingleThreadExecutor()
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                result.cryptoObject?.cipher?.let { cipher ->
                    val encrypted = getEncryptedData()
                    val data = cipher.doFinal(encrypted)
                    activity?.runOnUiThread { successAction(data) }
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(TAG, "Authentication error. $errString ($errorCode)")
                activity?.runOnUiThread { failedAction() }
            }
        })

        val promptInfo = biometricPromptInfo()
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    private fun handleEncrypt(
        cipher: Cipher,
        data: ByteArray,
        failedAction: () -> Unit,
        successAction: (ByteArray) -> Unit
    ) {

        val executor = Executors.newSingleThreadExecutor()
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                result.cryptoObject?.cipher?.let { resultCipher ->
                    val iv = resultCipher.iv
                    val encryptedData = resultCipher.doFinal(data)
                    saveEncryptedData(encryptedData, iv)
                    activity?.runOnUiThread { successAction(encryptedData) }
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(TAG, "Authentication error. $errString ($errorCode)")
                activity?.runOnUiThread { failedAction() }
            }
        })

        val promptInfo = biometricPromptInfo()
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    private fun biometricPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Prompt Title")
            .setSubtitle("Prompt Subtitle")
            .setDescription("Prompt Description: lorem ipsum dolor sit amet.")
            .setNegativeButtonText("Deseja Cancelar?")
            .build()
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

//    fun decryptData(ciphertext: ByteArray, cipher: Cipher): String {
//        val plaintext = cipher.doFinal(ciphertext)
//        return String(plaintext, Charset.forName("UTF-8"))
//    }
//
//    @RequiresApi(Build.VERSION_CODES.M)
//    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
//        val keyGenerator = KeyGenerator.getInstance(
//            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
//        keyGenerator.init(keyGenParameterSpec)
//        keyGenerator.generateKey()
//    }
//
//    @RequiresApi(Build.VERSION_CODES.M)
//    private fun generateSecretKey_bio() {
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            generateSecretKey( KeyGenParameterSpec.Builder(
//                    KEY_NAME,
//                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
//                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
//                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
//                .setUserAuthenticationRequired(true)
//                // Invalidate the keys if the user has registered a new biometric
//                // credential, such as a new fingerprint. Can call this method only
//                // on Android 7.0 (API level 24) or higher. The variable
//                // "invalidatedByBiometricEnrollment" is true by default.
//                .setInvalidatedByBiometricEnrollment(true)
//                .build())
//        }else{
//            Log.d("MY_APP_TAG", "Key is invalid.")
//        }
//
//
//    }
//
//    private fun getSecretKey():SecretKey{
//        val keyStore = KeyStore.getInstance("AndroidKeyStore")
//
//        // Before the keystore can be accessed, it must be loaded.
//        keyStore.load(null)
//
//        val aliases: Enumeration<String> = keyStore.aliases()
//        Log.d("MY_APP_TAG", aliases.toString())
//
//        return keyStore?.getKey(KEY_NAME, null) as SecretKey
//    }
//
//    private fun getCipher(): Cipher {
//        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
//                + KeyProperties.BLOCK_MODE_CBC + "/"
//                + KeyProperties.ENCRYPTION_PADDING_PKCS7)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.M)
//    private fun encryptSecretInformation() {
//        // Exceptions are unhandled for getCipher() and getSecretKey().
//
//            val cipher = getCipher()
//            val secretKey = getSecretKey()
//            try {
//                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
//                val encryptedInfo: ByteArray = cipher.doFinal(
//                    String().toByteArray(Charset.defaultCharset())
//                )
//                Log.d(
//                    "MY_APP_TAG", "Encrypted information: " +
//                            encryptedInfo.contentToString()
//                )
//            } catch (e: InvalidKeyException) {
//                Log.e("MY_APP_TAG", "Key is invalid.")
//            }
////            catch (e: UserNotAuthenticatedException) {
////                Log.d("MY_APP_TAG", "The key's validity timed out.")
////                biometricPrompt.authenticate(promptInfo)
////            }
//
//    }
}
