package com.subhamgupta.roomiesapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.RoomItemBinding
import com.subhamgupta.roomiesapp.domain.model.ROOMMATES
import com.subhamgupta.roomiesapp.onClickPerson

class RoomieAdapter(
    var onClickPerson: onClickPerson
) :RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val roomMates = mutableListOf<ROOMMATES>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(parent)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun setItem(roommates: ArrayList<ROOMMATES>){
        this.roomMates.clear()
        this.roomMates.addAll(roommates)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MyViewHolder).onBind(roomMates[position])
    }
    inner class MyViewHolder(parent: ViewGroup):RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.room_item, parent, false)
    ){
        private val binding = RoomItemBinding.bind(itemView)

        fun onBind(roommates: ROOMMATES){
            binding.labeled.text = roommates.USER_NAME
            binding.materialcard.setOnClickListener {
                onClickPerson.onClick(position)
            }
        }

    }

    override fun getItemCount(): Int {
        return roomMates.size
    }
}