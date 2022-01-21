package com.subhamgupta.roomiessaver.adapters

import android.content.Context
import android.text.format.DateUtils
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.roomiessaver.adapters.SummaryAdapter.TestHolder
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.subhamgupta.roomiessaver.R
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.subhamgupta.roomiessaver.Contenst
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class SummaryAdapter(var data: List<Map<String, Any?>?>, var context: Context) : RecyclerView.Adapter<TestHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.items, parent, false)
        return TestHolder(view)
    }

    override fun onBindViewHolder(holder: TestHolder, position: Int) {
        holder.bought_by.text = data[position]?.get("BOUGHT_BY").toString()

        holder.amount_paid.text = "₹" + (data.get(position)?.get("AMOUNT_PAID") )
        val sdf = SimpleDateFormat(Contenst.DATE_STRING, Locale.getDefault())
        try {
            val time = sdf.parse(data[position]?.get("DATE").toString()).time
            val ago = DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
            holder.date.text = ago.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            holder.date.text = data[position]?.get("DATE").toString()
        }
        holder.item_name.text =  data[position]?.get("ITEM_BOUGHT").toString()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class TestHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var item_name: TextView
        var amount_paid: TextView
        var date: TextView
        var bought_by: TextView
        var materialCardView: MaterialCardView
        var timeout: ProgressBar
        var linearLayout: LinearLayout

        init {
            item_name = itemView.findViewById(R.id.item_name)
            amount_paid = itemView.findViewById(R.id.amount)
            date = itemView.findViewById(R.id.date)
            bought_by = itemView.findViewById(R.id.bought_by)
            materialCardView = itemView.findViewById(R.id.materialcard)
            timeout = itemView.findViewById(R.id.timeout)
            linearLayout = itemView.findViewById(R.id.line2)
            linearLayout.visibility = View.GONE
            timeout.visibility = View.INVISIBLE
        }
    }
}