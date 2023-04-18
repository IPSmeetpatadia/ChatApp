package com.ipsmeet.chatapp.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.ipsmeet.chatapp.R
import com.ipsmeet.chatapp.dataclasses.UserDataClass
import java.io.File

class ChatAdapter(private val context: Context, private val chatList: List<UserDataClass>, val listener: OnClick): RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val profileImg: ImageView = itemView.findViewById(R.id.chat_imgView)
        val name: TextView = itemView.findViewById(R.id.chat_personName)
        val lastMsg: TextView = itemView.findViewById(R.id.chat_lastMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.single_view_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val localFile = File.createTempFile("tempfile", "jpeg")

        holder.apply {
            FirebaseStorage.getInstance()
                .getReference("Images/*${chatList[position].key}").getFile(localFile).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    Glide.with(context).load(bitmap).into(profileImg)
                }
                .addOnFailureListener {
                    Log.d("Fail to load chat profiles", it.message.toString())
                }
            name.text = chatList[position].userName

            itemView.setOnClickListener {
                listener.openChat(chatList[position].key)
            }
        }
    }

    interface OnClick {
        fun openChat(key: String)
    }

}