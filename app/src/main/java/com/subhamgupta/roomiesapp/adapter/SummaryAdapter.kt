package com.subhamgupta.roomiesapp.adapter

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.SummaryItemBinding
import com.subhamgupta.roomiesapp.models.Detail
import java.sql.Timestamp

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
        this.details.addAll(details ?: emptyList())
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
            binding.tags.removeAllViews()
            val value = if (detail.TAGS != null && detail.TAGS!!.isNotEmpty()) {
                View.VISIBLE
                detail.TAGS?.forEach {
                    val chip = Chip(binding.root.context)
                    chip.text = it
                    chip.isCheckable = true
                    chip.isCheckedIconVisible = true
                    chip.isChecked = true
                    binding.tags.addView(chip)
                }
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.tagText.visibility = value
            binding.tags.visibility = value

            binding.category.visibility = if (detail.CATEGORY != null) {
                binding.category.text = detail.CATEGORY
                View.VISIBLE
            } else
                View.GONE

            val vis = if (detail.NOTE != null && detail.NOTE.toString().isNotEmpty()) {
                binding.note.text = detail.NOTE
                View.VISIBLE
            } else
                View.GONE
            binding.note.visibility = vis
            binding.noteText.visibility = vis
            val ago = DateUtils.getRelativeTimeSpanString(
                detail.TIME_STAMP.toString().toLong(),
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS
            )
            binding.editQuery.setOnClickListener {

            }
            binding.date.text = ago
            val t = ago.toString()
            val ch = t.toCharArray()
            var tim = 0L
            if (ch.isNotEmpty() && (ch[0] in '0'..'9'))
                tim = (ch[0] + "" + ch[1]).replace(" ", "").toLong()
            val timestamp = Timestamp(detail.TIME_STAMP!!)
            if (DateUtils.isToday(timestamp.time))
                if (tim in 0..5L) {
                    binding.extra.visibility = View.VISIBLE
                }
            binding.materialcard.setOnClickListener {
                TransitionManager.beginDelayedTransition(binding.materialcard, ChangeBounds())
                binding.extra.visibility = if (binding.extra.isVisible) View.GONE else View.VISIBLE
            }
        }
    }
}