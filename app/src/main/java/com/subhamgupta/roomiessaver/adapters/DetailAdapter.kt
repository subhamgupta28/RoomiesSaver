package com.subhamgupta.roomiessaver.adapters

import com.firebase.ui.database.FirebaseRecyclerOptions
import com.subhamgupta.roomiessaver.models.RoomModel
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.subhamgupta.roomiessaver.adapters.DetailAdapter.DetailHolder
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.subhamgupta.roomiessaver.R
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView

class DetailAdapter(options: FirebaseRecyclerOptions<RoomModel?>) : FirebaseRecyclerAdapter<RoomModel, DetailHolder>(options) {
    override fun onBindViewHolder(holder: DetailHolder, position: Int, model: RoomModel) {
        holder.name.text = model.USER_NAME
        holder.amount.text = model.MONEY_PAID.toString() + ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.detail_item, parent, false)
        return DetailHolder(view)
    }

    inner class DetailHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView
        var amount: TextView

        init {
            name = itemView.findViewById(R.id.bought_by)
            amount = itemView.findViewById(R.id.item_name)
        }
    }
}