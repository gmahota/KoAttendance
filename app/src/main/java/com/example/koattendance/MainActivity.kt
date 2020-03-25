package com.example.koattendance

import android.os.Bundle
import android.view.Menu
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.koattendance.repository.SignInState
import com.example.koattendance.ui.auth.AuthFragment
import com.example.koattendance.ui.home.HomeFragment

import com.example.koattendance.ui.username.UsernameFragment
import com.google.android.gms.fido.Fido
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        const val REQUEST_FIDO2_REGISTER = 1
        const val REQUEST_FIDO2_SIGNIN = 2
    }

    private lateinit var viewModel: MainViewModel

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home,R.id.nav_gallery, R.id.nav_slideshow,R.id.nav_attendance), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        viewModel =
            ViewModelProviders.of(this).get(MainViewModel::class.java)

        /*viewModel.signInState.observe(this) {
                state ->
            when (state) {
//                is SignInState.SignedOut -> {
//                    setContentView(R.layout.username_fragment)
//                }
//                is SignInState.SigningIn -> {
//                    setContentView(R.layout.auth_fragment)
//                }
//                is SignInState.SignInError -> {
//                    Toast.makeText(this, state.error, Toast.LENGTH_LONG).show()
//                    // return to username prompt
//                    setContentView(R.layout.username_fragment)
//                }
//                is SignInState.SignedIn -> {
//                    setContentView(R.layout.fragment_home)
//                }
            }
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_FIDO2_REGISTER -> {
                val errorExtra = data?.getByteArrayExtra(Fido.FIDO2_KEY_ERROR_EXTRA)
                if (errorExtra != null) {
                    val error = AuthenticatorErrorResponse.deserializeFromBytes(errorExtra)
                    error.errorMessage?.let { errorMessage ->
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        Log.e( "ActivityResult",errorMessage)
                    }
                } else if (resultCode != RESULT_OK) {
                    Toast.makeText(this, R.string.cancelled, Toast.LENGTH_SHORT).show()
                } else {
                    val fragment = supportFragmentManager.findFragmentById(R.id.container)
                    if (data != null && fragment is HomeFragment) {
                        //fragment.handleRegister(data)
                    }
                }
            }
            REQUEST_FIDO2_SIGNIN -> {
                val errorExtra = data?.getByteArrayExtra(Fido.FIDO2_KEY_ERROR_EXTRA)
                if (errorExtra != null) {
                    val error = AuthenticatorErrorResponse.deserializeFromBytes(errorExtra)
                    error.errorMessage?.let { errorMessage ->
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        Log.e("App_singing", errorMessage)
                    }
                } else if (resultCode != RESULT_OK) {
                    Toast.makeText(this, R.string.cancelled, Toast.LENGTH_SHORT).show()
                } else {
                    val fragment = supportFragmentManager.findFragmentById(R.id.container)
                    if (data != null && fragment is AuthFragment) {
                        fragment.handleSignin(data)
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.setFido2ApiClient(Fido.getFido2ApiClient(this))
    }

    override fun onPause() {
        super.onPause()
        viewModel.setFido2ApiClient(null)
    }

    private fun showFragment(clazz: Class<out Fragment>, create: () -> Fragment) {
        val manager = supportFragmentManager

        if (!clazz.isInstance(manager.findFragmentById(R.id.container))) {
            var transaction = manager.beginTransaction()
            transaction.replace(R.id.container, create())
            transaction.commit()
        }
    }
}
