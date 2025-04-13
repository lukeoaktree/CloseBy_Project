package com.example.closeby

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.Toast
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.example.closeby.databinding.ActivityMainBinding
import com.example.closeby.ui.NeighborhoodActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            // If the user is not logged in, navigate to the login screen
            setContentView(R.layout.activity_main)  // The layout with login/register buttons

            val loginButton: Button = findViewById(R.id.loginMainButton)
            val registerButton: Button = findViewById(R.id.registerMainButton)

            loginButton.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
            }

            registerButton.setOnClickListener {
                startActivity(Intent(this, RegisterActivity::class.java))
            }

        } else {
            // User is logged in, navigate to the home screen or another screen
            startActivity(Intent(this, NeighborhoodActivity ::class.java))
            finish() // Prevent going back to the login/register screen
        }
    }
}

