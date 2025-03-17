package com.example.closeby

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CreateNeighborhoodActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var createButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_neighborhood)

        nameInput = findViewById(R.id.neighborhoodName)
        locationInput = findViewById(R.id.neighborhoodLocation)
        createButton = findViewById(R.id.createNeighborhoodButton)

        createButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val location = locationInput.text.toString().trim()

            if (name.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call method to send data to backend (to be implemented)
            createNeighborhood(name, location)
        }
    }

    private fun createNeighborhood(name: String, location: String) {
        // This is where you will send data to the backend
        Toast.makeText(this, "Neighborhood Created! (Backend logic needed)", Toast.LENGTH_SHORT).show()
    }
}
