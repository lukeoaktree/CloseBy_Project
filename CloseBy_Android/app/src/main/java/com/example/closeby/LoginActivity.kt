package com.example.closeby

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.closeby.ui.NeighborhoodActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Proceed to the next activity
                    val user = auth.currentUser
                    if (user != null) {

                        Toast.makeText(this, "Login successful! :D", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this, NeighborhoodActivity::class.java))
                        finish()
                    }
                } else {
                    // Handle different error scenarios
                    val errorCode = (task.exception as FirebaseAuthException).errorCode
                    when (errorCode) {
                        "ERROR_INVALID_EMAIL" -> {
                            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                        }

                        "ERROR_WRONG_PASSWORD" -> {
                            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                        }

                        "ERROR_USER_NOT_FOUND" -> {
                            Toast.makeText(
                                this,
                                "No user found with this email",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {
                            Toast.makeText(
                                this,
                                "Login failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
    }
}

