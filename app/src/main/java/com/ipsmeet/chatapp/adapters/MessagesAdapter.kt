package com.ipsmeet.chatapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.ipsmeet.chatapp.R
import com.ipsmeet.chatapp.dataclasses.MessagesDataClass

class MessagesAdapter(private val context: Context, private val messages: List<MessagesDataClass>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemSend = 1
    private val itemReceive = 2

    class SenderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val txtMsg: TextView = itemView.findViewById(R.id.senderMsg)
    }

    class ReceiverViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val txtMsg: TextView = itemView.findViewById(R.id.receiverMsg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == itemSend) {
            val view = LayoutInflater.from(context).inflate(R.layout.single_msg_sender, parent, false)
            SenderViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.single_msg_receiver, parent, false)
            ReceiverViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val msgs = messages[position]
        return if (FirebaseAuth.getInstance().currentUser!!.uid == msgs.senderID) {
            itemSend
        } else {
            itemReceive
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msgs = messages[position]
        if (holder.javaClass == SenderViewHolder::class.java) {
            val viewHolder: SenderViewHolder = holder as SenderViewHolder
            viewHolder.txtMsg.text = msgs.message
        }
        else {
            val viewHolder: ReceiverViewHolder = holder as ReceiverViewHolder
            viewHolder.txtMsg.text = msgs.message
        }
    }

}