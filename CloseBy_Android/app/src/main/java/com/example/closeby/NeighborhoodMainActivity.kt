package com.example.closeby

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration

class NeighborhoodMainActivity : AppCompatActivity() {

    private lateinit var neighborhoodNameTextView: TextView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var channelsRecyclerView: RecyclerView
   // private lateinit var channelsAdapter: ChannelAdapter
    private var selectedChannelId: String? = null
    private lateinit var messagesAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val neighborhoodId = intent.getStringExtra("neighborhoodId") ?: return
        val neighborhoodName = intent.getStringExtra("neighborhoodName") ?: "Neighborhood"

        setContentView(R.layout.activity_neighborhood_main)

        neighborhoodNameTextView = findViewById(R.id.neighborhoodName)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesAdapter = MessageAdapter(mutableListOf())
        messagesRecyclerView.adapter = messagesAdapter
        neighborhoodNameTextView.text = neighborhoodName

        channelsRecyclerView = findViewById(R.id.channelsRecyclerView)
        channelsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        loadChannels(neighborhoodId)

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                if (selectedChannelId != null) {
                    sendMessage(neighborhoodId, selectedChannelId!!, messageText)
                    messageInput.text.clear()
                } else {
                    Toast.makeText(this, "no channel selected", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessage(neighborhoodId: String, channelId: String, messageText: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Log.e("SendMessage", "User is not logged in")
            return
        }

        if (messageText.isBlank()) {
            Log.e("SendMessage", "Message text is empty")
            return
        }

        Log.d("SendMessage", "Sending message to: neighborhoods/$neighborhoodId/channels/$channelId/messages")

        val message = hashMapOf(
            "senderId" to currentUserId,
            "text" to messageText,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("neighborhoods")
            .document(neighborhoodId)
            .collection("channels")
            .document(channelId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                Log.d("SendMessage", "Message sent successfully to channel $channelId")
            }
            .addOnFailureListener { e ->
                Log.e("SendMessage", "Failed to send message", e)
            }
    }


    private var messagesListener: ListenerRegistration? = null

    private fun listenForMessages(neighborhoodId: String, channelId: String) {
        messagesListener?.remove()

        val db = FirebaseFirestore.getInstance()

        db.collection("neighborhoods")
            .document(neighborhoodId)
            .collection("channels")
            .document(channelId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Firestore", "listen failed", error)
                    return@addSnapshotListener
                }

                if (snapshots == null || snapshots.isEmpty) {
                    Log.d("Firestore", "no messages found")
                    return@addSnapshotListener
                }

                val messageList = mutableListOf<Message>()

                for (document in snapshots) {
                    val senderId = document.getString("senderId") ?: ""
                    val text = document.getString("text") ?: ""

                    val timestampValue = document.get("timestamp")
                    val timestamp = when (timestampValue) {
                        is Timestamp -> timestampValue
                        is Long -> Timestamp(timestampValue / 1000, ((timestampValue % 1000) * 1000000).toInt())
                        else -> null
                    }

                    val message = Message(senderId, text, timestamp)
                    messageList.add(message)
                }

                messagesAdapter.updateMessages(messageList)
                messagesRecyclerView.scrollToPosition(messageList.size - 1)
            }
    }

    private fun loadChannels(neighborhoodId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("neighborhoods")
            .document(neighborhoodId)
            .collection("channels")
            .get()
            .addOnSuccessListener { result ->
                val channelList = result.map { doc ->
                    Channel(
                        id = doc.id,
                        name = doc.getString("name") ?: "unnamed"
                    )
                }

                if (channelList.isEmpty()) {
                    val defaultChannelNames = listOf("Main", "General", "Events", "Dog Walking")
                    val defaultChannels = defaultChannelNames.map { name ->
                        hashMapOf("name" to name)
                    }

                    val batch = db.batch()
                    val channelsCollectionRef = db.collection("neighborhoods")
                        .document(neighborhoodId)
                        .collection("channels")

                    for (channel in defaultChannels) {
                        val newChannelRef = channelsCollectionRef.document()
                        batch.set(newChannelRef, channel)
                    }

                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("Firestore", "default channels added successfully")
                            loadChannels(neighborhoodId)
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreError", "error adding default channels", e)
                        }
                    return@addOnSuccessListener
                }

                val spinner: Spinner = findViewById(R.id.channelSpinner)
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    channelList.map { it.name }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedChannel = channelList[position]
                        selectedChannelId = selectedChannel.id
                        Toast.makeText(this@NeighborhoodMainActivity, "selected channel: ${selectedChannel.name}", Toast.LENGTH_SHORT).show()

                        listenForMessages(neighborhoodId, selectedChannelId!!)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // do nothing
                    }
                }

//                channelsAdapter = ChannelAdapter(channelList) { channel ->
//                    onChannelSelected(neighborhoodId, channel)
//                }
//                channelsRecyclerView.adapter = channelsAdapter
//                channelsAdapter.notifyDataSetChanged()

                if (channelList.isNotEmpty()) {
                    val firstChannel = channelList.first()
                    selectedChannelId = firstChannel.id
                    listenForMessages(neighborhoodId, firstChannel.id)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "error loading channels", e)
            }
    }

    private fun onChannelSelected(neighborhoodId: String, channel: Channel) {
        selectedChannelId = channel.id
        Toast.makeText(this, "selected channel: ${channel.name}", Toast.LENGTH_SHORT).show()
        listenForMessages(neighborhoodId, selectedChannelId!!)
    }

    private fun loadMessagesForChannel(channelId: String) {
        val db = FirebaseFirestore.getInstance()
        val neighborhoodId = intent.getStringExtra("neighborhoodId") ?: return
        val channelRef = db.collection("neighborhoods")
            .document(neighborhoodId)
            .collection("channels")
            .document(channelId)

        channelRef.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val messages = snapshot.toObjects(Message::class.java)
                messagesAdapter.updateMessages(messages)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "error loading messages", exception)
            }
    }
}