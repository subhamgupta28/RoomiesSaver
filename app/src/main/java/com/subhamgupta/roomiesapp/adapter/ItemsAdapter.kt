package com.subhamgupta.roomiesapp.adapter

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.roomiesapp.EditPopLink
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.SummaryItemBinding
import com.subhamgupta.roomiesapp.domain.model.Detail
import com.subhamgupta.roomiesapp.utils.Constant.Companion.DATE_STRING
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


class ItemsAdapter(
    var editPopLink: EditPopLink,
    var uuid: String
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val details = mutableListOf<Detail>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsHolder {
        return ItemsHolder(parent)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun setItems(details: List<Detail>?) {
        this.details.clear()
        this.details.addAll(details?: emptyList())
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        ( holder as ItemsHolder).onBind(details[position])

    }



    inner class ItemsHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.summary_item, parent, false)

    ) {
        private val binding = SummaryItemBinding.bind(itemView)

        fun onBind(model: Detail){
            binding.tags.visibility = View.GONE
            binding.category.visibility = View.GONE
            binding.tagText.visibility = View.GONE
            binding.note.visibility = View.GONE
            binding.noteText.visibility = View.GONE
            binding.boughtBy.visibility = View.GONE
            binding.itemName.text = model.ITEM_BOUGHT
            binding.boughtBy.text = model.BOUGHT_BY
            ("₹${model.AMOUNT_PAID}").also { binding.amount.text = it }
            binding.root.setOnClickListener {
                binding.fullDate.text = model.TIME
                binding.extra.visibility =  if (binding.extra.isVisible) View.GONE else View.VISIBLE
            }
            try {
                val sdf = SimpleDateFormat(DATE_STRING, Locale.getDefault())
                var t = ""
                try {
                    val time = sdf.parse(model.DATE).time
                    val ago = DateUtils.getRelativeTimeSpanString(
                        time,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    )
                    binding.date.text = ago.toString()
                    t = ago.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.date.text = model.DATE
                }
                val ch = t.toCharArray()
                var tim = 0L
                if (ch.isNotEmpty() && (ch[0] in '0'..'9'))
                    tim = (ch[0] + "" + ch[1]).replace(" ", "").toLong()
                val timestamp =  Timestamp(model.TIME_STAMP!!)
                if (ch[2] == 'h' || ch[3] == 'h')
                    tim = 10
                val uid = uuid
                val maxt = 1000 * 60 * (5 - tim)
                val timer = object : CountDownTimer(maxt, 1000) {
                    override fun onTick(time: Long) {
                        val n = time / 1000
                        binding.timeout.progress = n.toInt()
                    }

                    override fun onFinish() {
                        binding.line2.visibility = View.GONE
                    }
                }
//                Log.e("uuid","$uid, ${model.UUID}")
                if (uid == model.UUID) {
                    try {
                        if (DateUtils.isToday(timestamp.time))
                            if (tim in 0..5L) {
//                                Log.e("uuid","$uid, ${model.UUID}")

                                binding.line2.visibility = View.VISIBLE
                                binding.timeout.isIndeterminate = false
                                binding.editQuery.setOnClickListener {
                                    editPopLink.onClick(model)
                                }
                                timer.start()
                            } else {
                                binding.line2.visibility = View.GONE
                                timer.cancel()
                            }
                    } catch (e: Exception) {
                        Log.e("ItemsAdapter ERROR", e.message.toString())
                    }
                }
            } catch (e: Exception) {

            }
        }
    }



    override fun getItemCount(): Int {
        return details.size
    }
}