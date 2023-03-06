package com.subhamgupta.roomiesapp.adapter

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.type.DateTime
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.SpaceItemBinding
import com.subhamgupta.roomiesapp.domain.model.RoomDetail
import com.subhamgupta.roomiesapp.utils.Constant
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class AllRoomAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val allRoomDetail = mutableListOf<RoomDetail>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MyViewHolder).onBind(allRoomDetail[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(allRoomDetail: MutableList<RoomDetail>) {
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
            binding.roomMates.text = detail.ROOM_MATES.joinToString { it.USER_NAME.toString() }

            try {
                val df = DateTimeFormatter.ofPattern(Constant.DATE_STRING)
                val sdf = SimpleDateFormat(Constant.TIME_STRING, Locale.getDefault())
                val createDate = LocalDate.parse(detail.CREATED_ON.toString(), df)
                binding.updatedOn.text = sdf.format(
                    Date(
                        createDate.atStartOfDay(ZoneId.systemDefault())
                            .toInstant().epochSecond
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun getTimeAgo(time: String): String {
        var ago = ""
        try {
            ago = DateUtils.getRelativeTimeSpanString(
                time.toLong(),
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS
            ).toString()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ago
    }
}