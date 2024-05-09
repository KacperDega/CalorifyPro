package com.example.calorifypro

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(private val context: Context, private var userList: MutableList<User>) :
    RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    data class User(var rank: String, val username: String, val avatar: String, val value: String)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankTextView: TextView = itemView.findViewById(R.id.text_rank)
        val avatarImageView: ImageView = itemView.findViewById(R.id.image_avatar)
        val usernameTextView: TextView = itemView.findViewById(R.id.text_username)
        val valueTextView: TextView = itemView.findViewById(R.id.text_value)
    }

    fun setUsers(users: List<User>) {
        userList.clear()
        userList.addAll(users)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.leaderboard_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]

        holder.rankTextView.text = user.rank

        val avatarID = context.resources.getIdentifier(user.avatar, "drawable", context.packageName)
        holder.avatarImageView.setImageResource(avatarID)
        holder.usernameTextView.text = user.username
        holder.valueTextView.text = user.value
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}
