package com.example.closeby.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.closeby.CreateNeighborhoodActivity
import com.example.closeby.R

class NeighborhoodActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_neighborhood)

        val joinButton: Button = findViewById(R.id.joinButton)
        val createButton: Button = findViewById(R.id.createButton)

        // Check if buttons are being clicked
        joinButton.setOnClickListener {
            val intent = Intent(this, JoinNeighborhoodActivity::class.java)
            startActivity(intent)
        }

        createButton.setOnClickListener {
            val intent = Intent(this, CreateNeighborhoodActivity::class.java)
            startActivity(intent)
        }
    }
}