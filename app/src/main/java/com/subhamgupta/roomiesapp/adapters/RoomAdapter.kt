package com.subhamgupta.roomiesapp.adapters

import android.content.Context
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.subhamgupta.roomiesapp.models.RoomModel
import com.google.firebase.database.DatabaseReference
import com.subhamgupta.roomiesapp.onClickPerson
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.subhamgupta.roomiesapp.adapters.RoomAdapter.RoomHolder
import com.google.firebase.database.DataSnapshot
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.subhamgupta.roomiesapp.R
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
            val uuid = map["UUID"].toString()
            val n = name[0].uppercase()+name.substring(1)

//            if (model.UUID == uuid) {
//                holder.label.text = "You"
////                holder.materialCardView.setCardBackgroundColor(Color.parseColor("#814285F4"))
//            }
//            else
                holder.label.text = n
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