//package com.example.closeby
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//
//class ChannelAdapter(
//    private val channels: List<Channel>,
//    private val onChannelClick: (Channel) -> Unit
//) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {
//
//    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val channelName: TextView = itemView.findViewById(R.id.channelNameText)
//
//        init {
//            itemView.setOnClickListener {
//                onChannelClick(channels[adapterPosition])
//            }
//        }
//    }
//
////    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
////        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false)
////        return ChannelViewHolder(view)
////    }
//
//    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
//        val channel = channels[position]
//        holder.channelName.text = channel.name
//    }
//
//    override fun getItemCount(): Int = channels.size
//}
