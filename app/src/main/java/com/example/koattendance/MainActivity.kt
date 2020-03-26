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
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.auth_fragment.*
import kotlinx.coroutines.channels.consumesAll

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

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home,R.id.nav_auth, R.id.nav_slideshow,R.id.nav_attendance), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        basicReadWrite()
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
        Log.e("APP","AQIO")
        when (requestCode) {
            REQUEST_FIDO2_REGISTER -> {
                Log.e("APP","111111")
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
                Log.e("APP","122222")
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

    fun basicReadWrite() {
        // [START write_message]
        // Write a message to the database
        val database = Firebase.database
        val myRef = database.getReference("message")

        myRef.setValue("Hello, World!")
        // [END write_message]

        // [START read_message]
        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue<String>()
                Log.d(TAG, "Value is: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
        // [END read_message]
    }

}
