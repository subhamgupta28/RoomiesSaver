package com.subhamgupta.roomiessaver.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.subhamgupta.roomiessaver.Contenst.Companion.DATE_STRING
import com.subhamgupta.roomiessaver.R
import com.subhamgupta.roomiessaver.adapters.ItemsAdapter.ItemsHolder
import com.subhamgupta.roomiessaver.models.Detail
import com.subhamgupta.roomiessaver.onClickPerson
import com.subhamgupta.roomiessaver.utility.SettingsStorage
import com.subhamgupta.roomiessaver.utility.TimeAgo
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.*


class ItemsAdapter(
    options: FirestoreRecyclerOptions<Detail?>,
    var context: Context,
    onClickPerson: onClickPerson,
    var sumMap: MutableMap<Int, Int>,
    var pos: Int
) : FirestoreRecyclerAdapter<Detail, ItemsHolder>(options) {
    var timeAgo: TimeAgo
    var settingsStorage: SettingsStorage
    var onClickPerson: onClickPerson
    var sum: Long = 0
    var ref: FirebaseFirestore
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.items, parent, false)
        sum = 0L
        return ItemsHolder(view)
    }

    override fun onBindViewHolder(holder: ItemsHolder, position: Int, model: Detail) {

        if (isCurrentMonth(model.DATE.toString())) {
            holder.itemView.visibility = View.VISIBLE

            holder.item_name.text = model.ITEM_BOUGHT
            ("₹${model.AMOUNT_PAID}").also { holder.amount_paid.text = it }
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
                    holder.date.text = ago.toString()
                    t = ago.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                    holder.date.text = model.DATE
                }
                val ch = t.toCharArray()
                var tim = 0L
                if (ch.isNotEmpty() && (ch[0] in '0'..'9'))
                    tim = (ch[0] + "" + ch[1]).replace(" ", "").toLong()
                val timestamp =
                    Timestamp(SimpleDateFormat(DATE_STRING, Locale.getDefault()).parse(model.DATE).time)
                if (ch[2] == 'h' || ch[3] == 'h')
                    tim = 10
                val uid = settingsStorage.uuid
                val maxt = 1000 * 60 * (5 - tim)
                val timer = object : CountDownTimer(maxt, 1000) {
                    override fun onTick(time: Long) {
                        val n = time / 1000
                        holder.timeout.progress = n.toInt()
                    }

                    override fun onFinish() {
                        holder.timeout.visibility = View.GONE
                        holder.edit.visibility = View.GONE
                    }
                }
                if (uid == model.UUID) {
                    try {
                        if (DateUtils.isToday(timestamp.time))
                            if (tim in 0..5L) {
                                holder.timeout.visibility = View.VISIBLE
                                holder.edit.visibility = View.VISIBLE
                                holder.timeout.isIndeterminate = false
                                holder.edit.setOnClickListener {
                                    editPopup(holder.edit.context, model)
                                }
                                timer.start()
                            } else {
                                holder.timeout.visibility = View.GONE
                                holder.edit.visibility = View.GONE
                                timer.cancel()
                            }
                    } catch (e: Exception) {
                        Log.e("ItemsAdapter ERROR", e.message.toString())
                    }
                }
            } catch (e: Exception) {

            }
            sum += model.AMOUNT_PAID
            if (sumMap[pos] != null)
                sumMap[pos] = sumMap[pos]!!.plus(sum.toInt())
            else
                sumMap[pos] = sum.toInt()
            onClickPerson.sendSumMap(sumMap)
        } else
            holder.itemView.visibility = View.GONE
    }

    fun isCurrentMonth(date: String): Boolean {

        val gDate = SimpleDateFormat(DATE_STRING, Locale.getDefault()).parse(date)
        val timeZone = ZoneOffset.UTC
        val localDate = LocalDateTime.ofInstant(gDate.toInstant(), timeZone)
        val currentMonth = YearMonth.now(timeZone)
        return currentMonth.equals(YearMonth.from(localDate))
    }

    private fun editPopup(context: Context, model: Detail) {
        val mcard = MaterialAlertDialogBuilder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.popup, null)
        val item = view.findViewById(R.id.item_bought) as TextInputEditText
        val amount = view.findViewById(R.id.amount_paid) as TextInputEditText
        val save = view.findViewById(R.id.save_btn) as Button
        save.text = "Edit & Save"
        item.setText(model.ITEM_BOUGHT)
        amount.setText(model.AMOUNT_PAID.toString())

        save.setOnClickListener {
            var item_name = item.text.toString()
            var amount_paid = amount.text.toString()
//            Log.e("MSG","${model.UUID}")
            Toast.makeText(context, "Saved", Toast.LENGTH_LONG).show()
        }
        mcard.setView(view)
        mcard.background = ColorDrawable(Color.TRANSPARENT)
        mcard.show()
    }

    inner class ItemsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var item_name: TextView
        var amount_paid: TextView
        var date: TextView
        var bought_by: TextView
        var timeout: ProgressBar
        var edit: Button

        init {
            item_name = itemView.findViewById(R.id.item_name)
            amount_paid = itemView.findViewById(R.id.amount)
            date = itemView.findViewById(R.id.date)
            bought_by = itemView.findViewById(R.id.bought_by)
            bought_by.visibility = View.GONE
            timeout = itemView.findViewById(R.id.timeout)
            edit = itemView.findViewById(R.id.edit_query)
            timeout.max = 300
            timeout.min = 0
        }
    }

    init {
        timeAgo = TimeAgo()
        settingsStorage = SettingsStorage(context)
        this.onClickPerson = onClickPerson
        ref = FirebaseFirestore.getInstance()
    }
}