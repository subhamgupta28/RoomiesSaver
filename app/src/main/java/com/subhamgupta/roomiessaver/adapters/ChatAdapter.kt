package com.subhamgupta.roomiessaver.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.subhamgupta.roomiessaver.R
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import java.util.ArrayList

class ChatAdapter(private val mapList: List<Map<String, Any?>>?, private val user_name: String, private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val sender_user = 1
    private val receiver_user = 2
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == sender_user) {
            SenderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false))
        } else {
            ReceiverViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == sender_user) {
            val lp = (holder as SenderViewHolder).mdc.layoutParams as RelativeLayout.LayoutParams
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0)
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            holder.mdc.layoutParams = lp
            mapList?.get(position)?.let { holder.setData(it) }
        } else {
            val lp = (holder as ReceiverViewHolder).mdc.layoutParams as RelativeLayout.LayoutParams
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0)
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            holder.mdc.layoutParams = lp
            mapList?.get(position)?.let { holder.setData(it) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val u = mapList?.get(position)?.get("PERSON_FROM").toString()
        return if (u == user_name) sender_user else receiver_user
    }

    override fun getItemCount(): Int {
        return mapList!!.size
    }

    internal class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var user_name: TextView
        var msg: TextView
        var c_time: TextView
        var mdc: MaterialCardView
        fun setData(data: Map<String, Any?>) {
            msg.text = data["ISSUE"].toString()
            val t = data["TIME"].toString()
//            Log.e("TIME_F", t.substring(17)) //Sat, 06 Nov 2021 01:15 PM
            c_time.text = "\t\t\t" + t.substring(17)
        }

        init {
            user_name = itemView.findViewById(R.id.user_name)
            mdc = itemView.findViewById(R.id.chat_mdc)
            msg = itemView.findViewById(R.id.msg)
            c_time = itemView.findViewById(R.id.time_c)
            user_name.visibility = View.GONE
            mdc.setBackgroundResource(R.drawable.right_bg)
            mdc.setCardBackgroundColor(Color.parseColor("#0b5c35"))
        }
    }

    internal class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var user_name: TextView
        var msg: TextView
        var c_time: TextView
        var mdc: MaterialCardView
        fun setData(data: Map<String, Any?>) {
            user_name.text = data["PERSON_FROM"].toString()
            msg.text = data["ISSUE"].toString()
            val t = data["TIME"].toString()
//            Log.e("TIME_F", t.substring(17)) //Sat, 06 Nov 2021 01:15 PM
            c_time.setText(t.substring(17))
        }

        init {
            user_name = itemView.findViewById(R.id.user_name)
            mdc = itemView.findViewById(R.id.chat_mdc)
            msg = itemView.findViewById(R.id.msg)
            c_time = itemView.findViewById(R.id.time_c)
            mdc.setBackgroundResource(R.drawable.left_bg)
            mdc.setCardBackgroundColor(Color.parseColor("#162c4f"))
        }
    }
}