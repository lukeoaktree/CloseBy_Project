package com.example.closeby

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private var messages: MutableList<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    fun updateMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    // Generate a light color based on a unique user string (email or UID)
    fun generateLightColorFromString(input: String): Int {
        val hash = input.hashCode()
        val r = (hash shr 16 and 0xFF) % 128 + 127
        val g = (hash shr 8 and 0xFF) % 128 + 127
        val b = (hash and 0xFF) % 128 + 127
        return Color.rgb(r, g, b)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTextView.text = message.text

        val userColor = generateLightColorFromString(message.senderId)
        holder.messageCard.setCardBackgroundColor(userColor)
        holder.messageTextView.setTextColor(Color.BLACK)
    }


    override fun getItemCount(): Int = messages.size

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageTextView: TextView = view.findViewById(R.id.messageTextView)
        val messageCard: CardView = view.findViewById(R.id.messageCard)
    }

}
