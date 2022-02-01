package com.subhamgupta.roomiessaver.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.format.DateUtils
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.roomiessaver.adapters.SummaryAdapter.TestHolder
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.subhamgupta.roomiessaver.R
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.subhamgupta.roomiessaver.Contenst
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class SummaryAdapter(/*var data: List<Map<String, Any?>?>,*/ var context: Context) : RecyclerView.Adapter<TestHolder>() {
    var data = emptyList<Map<String, Any?>?>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.items, parent, false)
        return TestHolder(view)
    }

    override fun onBindViewHolder(holder: TestHolder, position: Int) {
        val boughtBy = data[position]?.get("BOUGHT_BY").toString()
        val price = "₹" + (data[position]?.get("AMOUNT_PAID") )
        val time = data[position]?.get("TIME").toString()
        val item = data[position]?.get("ITEM_BOUGHT").toString()
        val n = boughtBy[0].uppercase()+boughtBy.substring(1)
        //Log.e("NEW_DATA", boughtBy)
        holder.bought_by.text = n
        holder.amount_paid.text = price
        holder.item_name.text =  item
        holder.itemView.setOnClickListener {
            infoCard(holder.amount_paid.context, boughtBy, price, time, item)
        }
        val sdf = SimpleDateFormat(Contenst.DATE_STRING, Locale.getDefault())
        try {
//            val time = sdf.parse(data[position]?.get("DATE").toString()).time
            val time = data[position]?.get("TIME_STAMP").toString().toLong()

            val ago = DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
            holder.date.text = ago.toString()
        } catch (e: Exception) {
            Log.e("ERROR", e.localizedMessage)
        }

    }
    fun setDataTo( newData: List<Map<String, Any?>?>){
        val diffUtil = MyDiffUtil(newData, data)
        val result = DiffUtil.calculateDiff(diffUtil)
        data = newData
        result.dispatchUpdatesTo(this)
    }
    private fun infoCard(context: Context, bought:String, priceP:String, time:String, itemN:String){
        val mcard = MaterialAlertDialogBuilder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.item_info, null)
        val boughtBy = view.findViewById(R.id.ii_bought_by) as TextView
        val item = view.findViewById(R.id.ii_item_name) as TextView
        val price = view.findViewById(R.id.ii_price) as TextView
        val date = view.findViewById(R.id.ii_date) as TextView
        boughtBy.text = bought
        item.text = itemN
        price.text = priceP
        date.text = time
        mcard.setView(view)
        mcard.background = ColorDrawable(Color.TRANSPARENT)
        mcard.show()
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