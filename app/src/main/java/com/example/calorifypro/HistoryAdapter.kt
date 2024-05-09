package com.example.calorifypro

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val dataList: List<String>, private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(date: String)
        fun onItemButtonClick(date: String)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.textView)
        val button: ImageButton = itemView.findViewById(R.id.imageButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date = dataList[position]
        holder.dateTextView.text = date

        // Obsługa kliknięcia na cały element
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(date)
        }

        // Obsługa kliknięcia na przycisk
        holder.button.setOnClickListener {
            itemClickListener.onItemButtonClick(date)
        }
    }

    override fun getItemCount(): Int {
        val itemCount = dataList.size
        return itemCount
    }

}
