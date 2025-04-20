package com.example.closeby

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.closeby.NeighborhoodActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import org.json.JSONObject

class CreateNeighborhoodActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var nameInput: EditText
    private lateinit var createButton: Button

    private val LOCATION_PERMISSION_REQUEST_CODE = 1  // location permission

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_neighborhood)

        requestQueue = Volley.newRequestQueue(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        nameInput = findViewById(R.id.neighborhoodName)
        createButton = findViewById(R.id.createNeighborhoodButton)

        createButton.setOnClickListener {
            val name = nameInput.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a neighborhood name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // send current location to the backend
            fetchLocationAndCreateNeighborhood(name)
        }
    }

    private fun fetchLocationAndCreateNeighborhood(name: String) {
        // check location permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // request permissions if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // permissions are granted
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this, OnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val userId = 1

                    // send data to the server
                    createNeighborhood(name, latitude, longitude, userId)
                } else {
                    Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun createNeighborhood(name: String, latitude: Double, longitude: Double, userId: Int) {
        val url = "http://10.0.2.2:3000/createNeighborhood"

        val jsonObject = JSONObject()
        jsonObject.put("name", name)
        jsonObject.put("latitude", latitude)
        jsonObject.put("longitude", longitude)
        jsonObject.put("user_id", userId)

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            // Response.ErrorListener is unnecessary here
            { response ->
                Toast.makeText(this, "Neighborhood Created!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, NeighborhoodActivity::class.java) // Redirect to another screen
                startActivity(intent)
                finish()
            },
           // Response.ErrorListener is unnecessary here
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(request)
    }

    // user response to location request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // if permission granted
                val name = nameInput.text.toString().trim()
                fetchLocationAndCreateNeighborhood(name)
            } else {
                // permission denied message
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
