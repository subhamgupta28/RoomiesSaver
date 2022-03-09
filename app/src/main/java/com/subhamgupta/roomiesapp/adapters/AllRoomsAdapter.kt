package com.subhamgupta.roomiesapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.models.AllRooms
import com.subhamgupta.roomiesapp.utility.SettingsStorage


class AllRoomsAdapter(
    var data: List<String>,
    var settingsStorage: SettingsStorage,
): RecyclerView.Adapter<AllRoomsAdapter.AllRoomHolder>() {

    inner class AllRoomHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val room_id: TextView
        init {
            room_id = itemView.findViewById(R.id.rooms_name)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllRoomHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rooms_item, parent, false)
        return AllRoomHolder(view)
    }

    override fun onBindViewHolder(holder: AllRoomHolder, position: Int) {
        "${position+1}:- ${data[position]}".also { holder.room_id.text = it }
        holder.room_id.setOnClickListener {
            Toast.makeText(holder.room_id.context, "Wait entering selected room", Toast.LENGTH_LONG).show()
            settingsStorage.roomRef = "ROOM_ID${position+1}"
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

}