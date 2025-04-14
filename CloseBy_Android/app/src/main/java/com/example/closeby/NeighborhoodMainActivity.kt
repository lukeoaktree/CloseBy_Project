package com.example.closeby

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.closeby.ui.MessagesAdapter

class NeighborhoodMainActivity : AppCompatActivity() {

    private lateinit var neighborhoodNameTextView: TextView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var messagesRecyclerView: RecyclerView

    private lateinit var messagesAdapter: MessagesAdapter  // Adapter for RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {

        val neighborhoodName = intent.getStringExtra("neighborhoodName")
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_neighborhood_main)

        neighborhoodNameTextView = findViewById(R.id.neighborhoodName)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)

        // Set up RecyclerView
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesAdapter = MessagesAdapter()
        messagesRecyclerView.adapter = messagesAdapter

        // For now, set a hardcoded neighborhood name (could be passed as intent extra)
        neighborhoodNameTextView.text = "Neighborhood Name"

        sendButton.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                // Add message to RecyclerView (to be replaced with backend later)
                messagesAdapter.addMessage(message)
                messageInput.text.clear()
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
