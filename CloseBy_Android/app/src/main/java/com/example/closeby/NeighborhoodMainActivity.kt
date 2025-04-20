package com.example.closeby

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.closeby.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NeighborhoodMainActivity : AppCompatActivity() {

    private lateinit var neighborhoodNameTextView: TextView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var messagesRecyclerView: RecyclerView

    private lateinit var messagesAdapter: MessageAdapter  // Adapter for RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val neighborhoodId = intent.getStringExtra("neighborhoodId") ?: return
        val neighborhoodName = intent.getStringExtra("neighborhoodName") ?: "Neighborhood"
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        setContentView(R.layout.activity_neighborhood_main)

        neighborhoodNameTextView = findViewById(R.id.neighborhoodName)
        neighborhoodNameTextView.text = neighborhoodName

        neighborhoodNameTextView = findViewById(R.id.neighborhoodName)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)

        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesAdapter = MessageAdapter(mutableListOf())
        messagesRecyclerView.adapter = messagesAdapter

        // set name of neighborhood
        neighborhoodNameTextView.text = neighborhoodName

        sendButton.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(neighborhoodId, message)
                messageInput.text.clear()
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        listenForMessages(neighborhoodId)

    }
    fun sendMessage(neighborhoodId: String, messageText: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val message = Message(
            senderId = currentUserId,
            text = messageText,
            timestamp = System.currentTimeMillis()
        )

        db.collection("neighborhoods")
            .document(neighborhoodId)
            .collection("messages")
            .add(message)
    }
    fun listenForMessages(neighborhoodId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("neighborhoods")
            .document(neighborhoodId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Firestore", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshots == null || snapshots.isEmpty) {
                    Log.d("Firestore", "No messages found.")
                    return@addSnapshotListener
                }

                val messageList = mutableListOf<Message>()
                for (doc in snapshots) {
                    val message = doc.toObject(Message::class.java)
                    Log.d("Firestore", "Message: ${message.text}")
                    messageList.add(message)
                }
                Log.d("Firestore", "Updating messages in adapter. Messages count: ${messageList.size}")
                messagesAdapter.updateMessages(messageList)
                messagesRecyclerView.scrollToPosition(messageList.size - 1)
            }
    }


}

