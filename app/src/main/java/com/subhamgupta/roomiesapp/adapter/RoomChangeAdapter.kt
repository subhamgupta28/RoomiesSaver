package com.subhamgupta.roomiesapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.RoomChangeItemBinding

class RoomChangeAdapter : RecyclerView.Adapter<RoomChangeAdapter.ViewHolder>() {
    var map = mutableListOf<String>()
    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.room_change_item, parent, false)
    ) {
        private val binding = RoomChangeItemBinding.bind(itemView)

        fun onBind(name: String){
            binding.roomName.text = name
            binding.checkBox.setOnCheckedChangeListener{i,j->

            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: MutableList<String>?){
        map.clear()
        map.addAll(data?: emptyList())
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (holder as ViewHolder).onBind(map[position])
    }

    override fun getItemCount(): Int {
        return map.size
    }
}