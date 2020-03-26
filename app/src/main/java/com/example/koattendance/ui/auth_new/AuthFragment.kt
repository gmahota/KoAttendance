package com.example.koattendance.ui.auth_new

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.ContentLoadingProgressBar
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.android.fido2.ui.observeOnce
import com.example.koattendance.MainActivity
import com.example.koattendance.R
import com.example.koattendance.repository.SignInState
import com.example.koattendance.ui.home.HomeFragment
import com.example.koattendance.ui.username.UsernameFragment


class AuthFragment: Fragment() {
    companion object {
        private const val TAG = "AuthFragment"
        const val REQUEST_FIDO2_REGISTER = 1
        const val REQUEST_FIDO2_SIGNIN = 2
    }

    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        authViewModel =
            ViewModelProviders.of(this).get(AuthViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_auth, container, false)

        authViewModel.signInState.observe(viewLifecycleOwner, Observer {
            state ->
            when (state) {
//                is SignInState.SignedOut -> {
//                    Handler().postDelayed({ startActivity( Intent(context, AuthFragment::class.java)) }, 4000L)
//                    //showFragment(AuthFragment::class.java) { AuthFragment() }
//                }
//                is SignInState.SigningIn -> {
//                    Handler().postDelayed({ startActivity( Intent(context, AuthFragment::class.java)) }, 4000L)
//                    //showFragment(AuthFragment::class.java) { AuthFragment() }
//                }
//                is SignInState.SignInError -> {
//                    Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
//                    Handler().postDelayed({ startActivity( Intent(context, UsernameFragment::class.java)) }, 4000L)
//                    // return to username prompt
//                    //showFragment(UsernameFragment::class.java) { UsernameFragment() }
//                }
//                is SignInState.SignedIn -> {
//                    Handler().postDelayed({ startActivity( Intent(context, MainActivity::class.java)) }, 4000L)
//                }
            }
        })



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

//        { processing ->
//            if (processing) {
//                binding.processing.show()
//            } else {
//                binding.processing.hide()
//            }
//        }

        //val textView: TextView = root.findViewById(R.id.username)

//        authViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return root
    }



//    private fun showFragment(clazz: Class<out Fragment>, create: () -> Fragment) {
//        val manager = supportFragmentManager
//
//        if (!clazz.isInstance(manager.findFragmentById(R.id.container))) {
//            var transaction = manager.beginTransaction()
//            transaction.replace(R.id.container, create())
//            transaction.commit()
//        }
//    }
}
