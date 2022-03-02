package com.subhamgupta.roomiesapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.interfaces.HAdapToHFrag

class HomeAdapter(
    var data: ArrayList<MutableMap<String, String>>,
    var totalAmount:Int,
    var context: Context,
    var hAdapToHFrag: HAdapToHFrag
):RecyclerView.Adapter<HomeAdapter.HomeHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_item, parent, false)
        return HomeHolder(view)
    }
    override fun onBindViewHolder(holder: HomeHolder, position: Int) {
        val name = data[position]["USER_NAME"].toString()
        val uuid = data[position]["UUID"].toString()
        val n = name[0].uppercase()+name.substring(1)
        holder.name.text = n
        ("₹"+data[position]["AMOUNT"]).also { holder.amount.text = it }
        holder.itemView.setOnClickListener {
            hAdapToHFrag.goToHome(position, uuid)
        }
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