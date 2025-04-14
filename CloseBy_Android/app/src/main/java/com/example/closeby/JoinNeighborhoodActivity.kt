package com.example.closeby.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.closeby.LoginActivity
import com.example.closeby.NeighborhoodMainActivity
import com.example.closeby.R
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject


class JoinNeighborhoodActivity : AppCompatActivity() {
    private lateinit var neighborhoodCodeEditText: EditText
    private lateinit var joinButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_neighborhood)

        neighborhoodCodeEditText = findViewById(R.id.neighborhoodCodeEditText)
        joinButton = findViewById(R.id.joinButton)

        joinButton.setOnClickListener {
            val code = neighborhoodCodeEditText.text.toString().trim().uppercase()
            if (code.length != 6) {
                Toast.makeText(this, "Please enter a valid 6-character code.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get the current user's Firebase UID
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = user.uid
            Log.d("JoinNeighborhood", "User ID: $userId")

            val json = JSONObject().apply {
                put("neighborhoodCode", code)
                put("userId", userId)
            }

            val request = JsonObjectRequest(
                Request.Method.POST,
                "http://10.0.2.2:3000/checkNeighborhoodCode", // Backend URL
                json,
                { response ->
                    Log.d("JoinNeighborhood", "API Response: $response")  // Log the full response

                    // code found
                    val name = response.getString("name")
                    Toast.makeText(this, "Joined $name", Toast.LENGTH_SHORT).show()

                    val location = response.getJSONObject("location")
                    val lat = location.getDouble("lat")
                    val lng = location.getDouble("lng")

                    // location check
                    Log.d("Neighborhood", "Found: $name at $lat, $lng")
                    // You can pass this info to the next step
                    val intent = Intent(this, NeighborhoodMainActivity::class.java).apply {
                        putExtra("neighborhoodName", name)
                        putExtra("latitude", lat)
                        putExtra("longitude", lng)
                    }
                    startActivity(Intent(this, NeighborhoodMainActivity::class.java))
                },
                { error ->
                    Log.e("JoinNeighborhood", "Error occurred: ${error.message}", error)
                    if (error.networkResponse != null) {
                        Log.e("JoinNeighborhood", "Status Code: ${error.networkResponse.statusCode}")
                        val errorBody = String(error.networkResponse.data)
                        Log.e("JoinNeighborhood", "Error Body: $errorBody")
                    }


                    Toast.makeText(this, "Neighborhood not found.", Toast.LENGTH_SHORT).show()
                }
            )

            // Add the request to the queue
            Volley.newRequestQueue(this).add(request)

            
        }
    }
}

//SV8AT5
//H3Q1D8