package com.subhamgupta.roomiesapp.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.SpaceItemBinding
import com.subhamgupta.roomiesapp.models.RoomDetail

class AllRoomAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val allRoomDetail = mutableListOf<RoomDetail>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MyViewHolder).onBind(allRoomDetail[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(allRoomDetail: MutableList<RoomDetail>){
        this.allRoomDetail.clear()
        this.allRoomDetail.addAll(allRoomDetail)
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return allRoomDetail.size
    }
    inner class MyViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.space_item, parent, false)
    ) {
        private val binding = SpaceItemBinding.bind(itemView)
        fun onBind(detail: RoomDetail) {
           binding.sRoomName.text = detail.ROOM_NAME
            binding.sTotalSpends.text = detail.JOINED_PERSON.toString()
            binding.sUpdatedOn.text = detail.CREATED_ON
            Log.e("d","h")
            binding.sSpark.setData(arrayListOf(1,12,0,0,4,100))
        }
    }
}