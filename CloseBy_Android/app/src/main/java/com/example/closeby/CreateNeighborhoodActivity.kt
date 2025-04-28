package com.example.closeby

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import org.json.JSONObject

class CreateNeighborhoodActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var nameInput: EditText
    private lateinit var createButton: Button

    private val LOCATION_PERMISSION_REQUEST_CODE = 1  // location permission

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)

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

            // Send current location to the backend
            fetchLocationAndCreateNeighborhood(name)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun fetchLocationAndCreateNeighborhood(name: String) {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Double check permission right before accessing location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener(this) { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnSuccessListener
                    createNeighborhood(name, latitude, longitude, userId)
                } else {
                    requestLocationUpdates(name)
                }
            }
    }


    private fun requestLocationUpdates(name: String) {
        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                locationResult.let {
                    val location = it.lastLocation
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
                        createNeighborhood(name, latitude, longitude, userId)
                    }
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        // --- IMPORTANT: Permission check before requesting updates ---
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }


    private fun createNeighborhood(name: String, latitude: Double, longitude: Double, userId: String) {
        // Check if the neighborhood already exists in Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("neighborhoods")
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No duplicate, proceed with creating the neighborhood

                    // Add to Firestore immediately
                    addNeighborhoodToFirestore(name, latitude, longitude, userId)

                    // THEN call backend
                    val url = "http://10.0.2.2:3000/createNeighborhood"
                    val jsonObject = JSONObject()
                    jsonObject.put("name", name)
                    jsonObject.put("latitude", latitude)
                    jsonObject.put("longitude", longitude)
                    jsonObject.put("user_id", userId)

                    val request = JsonObjectRequest(
                        Request.Method.POST, url, jsonObject,
                        { response ->
                            Toast.makeText(this, "Neighborhood Created!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, NeighborhoodActivity::class.java)
                            intent.putExtra("neighborhood_name", name)
                            startActivity(intent)
                            finish()
                        },
                        { error ->
                            Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    )

                    requestQueue.add(request)

                } else {
                    // Neighborhood already exists, show an error message
                    Toast.makeText(this, "Neighborhood with this name already exists.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error checking neighborhood existence: ", e)
                Toast.makeText(this, "Error checking for existing neighborhood", Toast.LENGTH_SHORT).show()
            }
    }


    private fun addNeighborhoodToFirestore(name: String, latitude: Double, longitude: Double, userId: String) {
        val db = FirebaseFirestore.getInstance()

        val neighborhood = hashMapOf(
            "name" to name,
            "latitude" to latitude,
            "longitude" to longitude,
            "userId" to userId,
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        db.collection("neighborhoods")
            .add(neighborhood)
            .addOnSuccessListener { neighborhoodDocRef ->
                Log.d("Firestore", "Neighborhood successfully added to Firestore")

                // Create multiple default channels (e.g., "general", "random")
                val defaultChannels = listOf(
                    hashMapOf("name" to "Main"),
                    hashMapOf("name" to "General"),
                    hashMapOf("name" to "Tech"),
                    hashMapOf("name" to "Random")
                )


                // Add the default channels to the neighborhood
                for (channel in defaultChannels) {
                    neighborhoodDocRef.collection("channels")
                        .add(channel)
                        .addOnSuccessListener { channelDocRef ->
                            Log.d("Firestore", "Channel ${channel["name"]} created")

                            // After creating the channel, create the default message
                            val defaultMessage = hashMapOf(
                                "senderId" to userId,
                                "text" to "Welcome to the ${channel["name"]} channel!",
                                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                            )

                            // Add the default message to the newly created channel's messages collection
                            channelDocRef.collection("messages")
                                .add(defaultMessage)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Default message added to channel ${channel["name"]}")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirestoreError", "Error adding default message to channel ${channel["name"]}: ", e)
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreError", "Error creating channel: ", e)
                        }
                }

                Toast.makeText(this, "Neighborhood and default channels with messages created", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error adding neighborhood: ", e)
                Toast.makeText(this, "Error adding to Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val name = nameInput.text.toString().trim()
                fetchLocationAndCreateNeighborhood(name)
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

