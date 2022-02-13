package com.subhamgupta.roomiessaver.adapters

import android.content.Context
import android.graphics.Color
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.subhamgupta.roomiessaver.models.RoomModel
import com.google.firebase.database.DatabaseReference
import com.subhamgupta.roomiessaver.onClickPerson
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.subhamgupta.roomiessaver.adapters.RoomAdapter.RoomHolder
import com.google.firebase.database.DataSnapshot
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.subhamgupta.roomiessaver.R
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import java.util.HashMap

class RoomAdapter(options: FirebaseRecyclerOptions<RoomModel?>,
                  var context: Context,
                  var ref: DatabaseReference,
                  var user_name: String,
                  var onClickPerson: onClickPerson
                  ) : FirebaseRecyclerAdapter<RoomModel, RoomHolder>(options)
{
    override fun onBindViewHolder(holder: RoomHolder, position: Int, model: RoomModel) {
        ref.child(model.UUID!!).get().addOnSuccessListener { dataSnapshot: DataSnapshot ->
            val map: Map<String, Any>? = dataSnapshot.value as HashMap<String, Any>?
            val name = map!!["USER_NAME"].toString()
            val n = name[0].uppercase()+name.substring(1)
            holder.label.text = n
//            if (user_name == name) {
//                holder.materialCardView.setCardBackgroundColor(Color.parseColor("#814285F4"))
//            }
            holder.materialCardView.setOnClickListener { onClickPerson.onClick(position) }
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.room_item, parent, false)
        return RoomHolder(view)
    }

    inner class RoomHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var label: TextView
        var materialCardView: MaterialCardView

        init {
            label = itemView.findViewById(R.id.labeled)
            materialCardView = itemView.findViewById(R.id.materialcard)
        }
    }
}