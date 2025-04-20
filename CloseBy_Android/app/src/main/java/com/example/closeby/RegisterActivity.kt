package com.example.closeby

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.closeby.NeighborhoodActivity
import org.json.JSONObject
import com.google.firebase.auth.FirebaseAuth


class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // check if fields are empty
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // call registerUser
                registerUser(email, password)
            } else {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT)
                    .show()
            }
        }


//        val btnLogin = findViewById<Button>(R.id.btnLogin)
//        btnLogin.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }

    }

    private fun checkEmailExistence(email: String, password: String) {
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        // Email is not in use, proceed with registration
                        registerUser(email, password)
                    } else {
                        // Email is already in use, show an appropriate message
                        Toast.makeText(this, "Email is already registered", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle error checking the email
                    Toast.makeText(this, "Error checking email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun registerUser(email: String, password: String) {
        val auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Firebase registration success, now send the email to your backend
                    sendEmailToServer(email)
                    Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, NeighborhoodActivity::class.java)
                    startActivity(intent)
                } else {
                    // Firebase registration failed
                    Toast.makeText(
                        this,
                        "Firebase error: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun sendEmailToServer(email: String) {
        val jsonObject = JSONObject()
        jsonObject.put("email", email)

        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Request.Method.POST, "http://10.0.2.2:3000/api/register",
            Response.Listener { response ->
                Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, NeighborhoodActivity::class.java)
                startActivity(intent)
                finish()
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Server error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getBody(): ByteArray {
                return jsonObject.toString().toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(stringRequest)
    }


}
