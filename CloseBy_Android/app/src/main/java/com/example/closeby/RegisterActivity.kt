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
import com.example.closeby.ui.NeighborhoodActivity
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

            // check if fields are empty
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // call registerUser
                registerUser(email, password)
            } else {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        val jsonObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("password", password)

        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Request.Method.POST, "http://10.0.2.2:3000/api/register",
            Response.Listener { response ->
                // user registered successfully
                Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, NeighborhoodActivity::class.java)

                startActivity(intent)
                finish()  // close the RegisterActivity so that the user can't go back
            },
            Response.ErrorListener { error ->
                // failed to register
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {

            override fun getBody(): ByteArray {
                return jsonObject.toString().toByteArray()  // send the email and password in JSON format
            }

            // set content type as application/json for the request
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(stringRequest)

    }
}