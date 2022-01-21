package com.subhamgupta.roomiessaver.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.roomiessaver.R

class HomeAdapter(
    var data: ArrayList<MutableMap<String, String>>,
    var totalAmount:Int,
    var context: Context
):RecyclerView.Adapter<HomeAdapter.HomeHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_item, parent, false)
        return HomeHolder(view)
    }
    override fun onBindViewHolder(holder: HomeHolder, position: Int) {
        holder.name.setText(data[position]["USER_NAME"])
        holder.amount.setText("₹"+data[position]["AMOUNT"])
    }

    override fun getItemCount(): Int {
        return data.size
    }
    class HomeHolder(itemView: View):RecyclerView.ViewHolder(itemView) {

        var name: TextView
        var amount:TextView
        init {
            name = itemView.findViewById(R.id.hname)
            amount = itemView.findViewById(R.id.hamount)
        }
    }


}