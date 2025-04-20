package com.example.closeby

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.closeby.NeighborhoodActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            // if the user is not logged in go to the login screen
            setContentView(R.layout.activity_main)

            val loginButton: Button = findViewById(R.id.loginMainButton)
            val registerButton: Button = findViewById(R.id.registerMainButton)

            loginButton.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
            }

            registerButton.setOnClickListener {
                startActivity(Intent(this, RegisterActivity::class.java))
            }

        } else {
            // if user is logged in go to the home screen
            startActivity(Intent(this, NeighborhoodActivity ::class.java))
            finish()
        }
    }
}

