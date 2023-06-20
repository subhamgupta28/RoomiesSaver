package com.subhamgupta.roomiesapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.MonthlyItemBinding
import java.util.*

class MonthlyAdapter : RecyclerView.Adapter<MonthlyAdapter.MonthlyHolder>() {
    private var data = LinkedList<MutableMap<String, Any>>()

    inner class MonthlyHolder(parent: ViewGroup):RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.monthly_item, parent, false)
    ) {
        private val binding = MonthlyItemBinding.bind(itemView)
        fun onBind(pair: MutableMap<String, Any>){
            binding.totalSpends.text = "₹${pair["AMOUNT"]}"
            binding.monthName.text = pair["MONTH"].toString()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: LinkedList<MutableMap<String, Any>>){
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthlyHolder {
        return MonthlyHolder(parent)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MonthlyHolder, position: Int) {
        (holder as MonthlyHolder).onBind(data[position])
    }
}