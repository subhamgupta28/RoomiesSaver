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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.subhamgupta.roomiessaver.Contenst.Companion.DATE_STRING
import com.subhamgupta.roomiessaver.R
import com.subhamgupta.roomiessaver.adapters.ItemsAdapter.ItemsHolder
import com.subhamgupta.roomiessaver.models.Detail
import com.subhamgupta.roomiessaver.utility.SettingsStorage
import com.subhamgupta.roomiessaver.utility.TimeAgo
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


class ItemsAdapter(
    options: FirestoreRecyclerOptions<Detail?>,
    var context: Context,
    var ref: CollectionReference,
) : FirestoreRecyclerAdapter<Detail, ItemsHolder>(options) {
    var settingsStorage: SettingsStorage
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.items, parent, false)
        return ItemsHolder(view)
    }

    override fun onBindViewHolder(holder: ItemsHolder, position: Int, model: Detail) {
            holder.itemView.visibility = View.VISIBLE
            holder.item_name.text = model.ITEM_BOUGHT
            ("₹${model.AMOUNT_PAID}").also { holder.amount_paid.text = it }
            holder.itemView.setOnClickListener {
                infoCard(holder, model)
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
                val timestamp =  Timestamp(model.TIME_STAMP!!)
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

    }

    private fun infoCard(holder: ItemsHolder, model: Detail){
        holder.fullDate.text = model.TIME
        holder.fullDate.visibility = if (holder.fullDate.isVisible) View.GONE else View.VISIBLE
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
            ref.document(model.TIME_STAMP.toString()).update("ITEM_BOUGHT", item_name)
            ref.document(model.TIME_STAMP.toString()).update("AMOUNT_PAID", amount_paid.toInt())
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
        var fullDate:TextView
        var edit: Button

        init {
            item_name = itemView.findViewById(R.id.item_name)
            amount_paid = itemView.findViewById(R.id.amount)
            date = itemView.findViewById(R.id.date)
            bought_by = itemView.findViewById(R.id.bought_by)
            fullDate = itemView.findViewById(R.id.full_date)
            bought_by.visibility = View.GONE
            timeout = itemView.findViewById(R.id.timeout)
            edit = itemView.findViewById(R.id.edit_query)
            timeout.max = 300
            timeout.min = 0
        }
    }

    init {
        settingsStorage = SettingsStorage(context)
    }
}