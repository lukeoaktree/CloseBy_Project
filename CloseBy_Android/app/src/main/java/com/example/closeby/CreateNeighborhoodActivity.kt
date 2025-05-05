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

// (imports stay the same)

class CreateNeighborhoodActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var nameInput: EditText
    private lateinit var createButton: Button

    private var creatingNeighborhood = false // 🛡️ new boolean to prevent double creation
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

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

            if (creatingNeighborhood) {
                Log.d("CreateNeighborhood", "Already creating, ignoring duplicate click")
                return@setOnClickListener
            }

            creatingNeighborhood = true
            createButton.isEnabled = false // prevent clicking multiple times

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

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this) { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnSuccessListener
                    checkAndCreateNeighborhood(name, latitude, longitude, userId)
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
                locationResult?.let {
                    val location = it.lastLocation
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
                        checkAndCreateNeighborhood(name, latitude, longitude, userId)
                    }
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun checkAndCreateNeighborhood(name: String, latitude: Double, longitude: Double, userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("neighborhoods")
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No duplicate, now call backend
                    createNeighborhoodInBackend(name, latitude, longitude, userId)
                } else {
                    creatingNeighborhood = false
                    createButton.isEnabled = true
                    Toast.makeText(this, "Neighborhood with this name already exists.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                creatingNeighborhood = false
                createButton.isEnabled = true
                Log.e("FirestoreError", "Error checking neighborhood existence: ", e)
                Toast.makeText(this, "Error checking for existing neighborhood", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createNeighborhoodInBackend(name: String, latitude: Double, longitude: Double, userId: String) {
        val url = "http://10.0.2.2:3000/createNeighborhood"
        val jsonObject = JSONObject().apply {
            put("name", name)
            put("latitude", latitude)
            put("longitude", longitude)
            put("user_id", userId)
        }
        Log.d("CreateNeighborhoodPayload", jsonObject.toString())

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                Log.d("CreateNeighborhood", "Backend creation success")

                // Extract the join code from the response
                val joinCode = response.optString("joinCode")
                if (joinCode.isNotEmpty()) {
                    copyToClipboard(joinCode) // Copy the join code to the clipboard
                }

                // Backend successful -> Now add to Firestore
                addNeighborhoodToFirestore(name, latitude, longitude, userId)
            },
            { error ->
                Log.e("CreateNeighborhood", "Backend error: ${error.message}")
                creatingNeighborhood = false
                createButton.isEnabled = true
                Toast.makeText(this, "Backend error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(request)
    }

    // Function to copy the join code to the clipboard
    private fun copyToClipboard(joinCode: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Join Code", joinCode)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Join code copied to clipboard!", Toast.LENGTH_SHORT).show()
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

                val defaultChannels = listOf(
                    hashMapOf("name" to "Main"),
                    hashMapOf("name" to "General"),
                    hashMapOf("name" to "Dog Walking"),
                    hashMapOf("name" to "Events")
                )

                for (channel in defaultChannels) {
                    neighborhoodDocRef.collection("channels")
                        .add(channel)
                        .addOnSuccessListener { channelDocRef ->
                            val defaultMessage = hashMapOf(
                                "senderId" to userId,
                                "text" to "Welcome to the ${channel["name"]} channel!",
                                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                            )

                            channelDocRef.collection("messages")
                                .add(defaultMessage)
                        }
                }

                Toast.makeText(this, "Neighborhood and channels created!", Toast.LENGTH_SHORT).show()


                // Navigate to NeighborhoodActivity
                val intent = Intent(this, NeighborhoodActivity::class.java)
                intent.putExtra("neighborhood_name", name)
                startActivity(intent)
                finish()

            }
            .addOnFailureListener { e ->
                creatingNeighborhood = false
                createButton.isEnabled = true
                Log.e("FirestoreError", "Error adding neighborhood: ", e)
                Toast.makeText(this, "Error adding to Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    }

