package com.ipsmeet.chatapp.adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.ipsmeet.chatapp.R
import com.ipsmeet.chatapp.activities.ChatActivity
import com.ipsmeet.chatapp.databinding.LayoutDialogBinding
import com.ipsmeet.chatapp.databinding.PopupViewProfileBinding
import com.ipsmeet.chatapp.dataclasses.UserDataClass
import java.io.File

class ChatAdapter(private val context: Context, private val chatList: List<UserDataClass>, private val listener: OnClick)
    : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

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
        holder.apply {
            //  FETCHING USER'S PROFILE IMAGES
            val localFile = File.createTempFile("tempfile", "jpeg")
            FirebaseStorage.getInstance()
                .getReference("Images/*${chatList[position].key}").getFile(localFile).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    Glide.with(context.applicationContext).load(bitmap).into(profileImg)
                }
                .addOnFailureListener {
                    Log.d("Fail to load chat profiles", it.message.toString())
                }
            //  FETCHING USER'S NAMES
            name.text = chatList[position].userName

            //  ITEM-CLICK LISTENER
            itemView.setOnClickListener {
                listener.openChat(chatList[position].key, chatList[position].token)
            }

            //  VIEW PROFILE-IMAGE POPUP
            profileImg.setOnClickListener {
                listener.viewProfilePopup(chatList[position].key, chatList[position].token)
            }
        }
    }

    interface OnClick {
        fun openChat(key: String, token: String)   //  passing user's ID, on ItemClickListener
        fun viewProfilePopup(key: String, token: String)
    }

}