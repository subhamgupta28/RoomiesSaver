package com.subhamgupta.roomiesapp.adapter

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.SummaryItemBinding
import com.subhamgupta.roomiesapp.models.Detail

class SummaryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val details = mutableListOf<Detail>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MyViewHolder).onBind(details[position])
    }

    override fun getItemCount(): Int {
        return details.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(details: List<Detail>?) {
        this.details.clear()
        this.details.addAll(details?: emptyList())
        notifyDataSetChanged()
    }

    inner class MyViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.summary_item, parent, false)
    ) {
        private val binding = SummaryItemBinding.bind(itemView)
        fun onBind(detail: Detail) {
            binding.boughtBy.text = detail.BOUGHT_BY.toString()
            "₹${detail.AMOUNT_PAID}".also { binding.amount.text = it }
            binding.itemName.text = detail.ITEM_BOUGHT.toString()
            binding.fullDate.text = detail.TIME.toString()
            binding.date.text = DateUtils.getRelativeTimeSpanString(detail.TIME_STAMP.toString().toLong(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
            binding.editQuery.setOnClickListener {

            }
            binding.materialcard.setOnClickListener {
                binding.fullDate.visibility =  if (binding.fullDate.isVisible) View.GONE else View.VISIBLE
            }
        }
    }
}