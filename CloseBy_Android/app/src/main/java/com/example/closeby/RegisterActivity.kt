package com.example.closeby

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

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

            // Check if fields are empty
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Call the registerUser function to send the network request
                registerUser(email, password)
            } else {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        val jsonObject = JSONObject()
        jsonObject.put("email", email)  // Only sending email and password
        jsonObject.put("password", password)

        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Request.Method.POST, "http://10.0.2.2:3000/api/register",  // Use 10.0.2.2 for emulator
            Response.Listener { response ->
                // Success - user registered successfully
                Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Error - failed to register
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {

            override fun getBody(): ByteArray {
                return jsonObject.toString().toByteArray()  // Send the email and password in JSON format
            }

            // Set content type as application/json for the request
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        // Add the request to the queue
        requestQueue.add(stringRequest)
    }
}
