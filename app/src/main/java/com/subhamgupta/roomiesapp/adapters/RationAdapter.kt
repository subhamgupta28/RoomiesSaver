package com.subhamgupta.roomiesapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.fragments.RationCardFragment
import com.subhamgupta.roomiesapp.utility.TimeAgo
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class RationAdapter(
    var datas: MutableList<Map<String, Any?>?>,
    var context: Context,
    var fm:FragmentManager
) : RecyclerView.Adapter<RationAdapter.RationHolder>() {

    lateinit var rationCardFragment: RationCardFragment
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RationHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ration_card, parent, false)
        rationCardFragment = RationCardFragment()
        return RationHolder(view)
    }

    override fun onBindViewHolder(holder: RationHolder, position: Int) {
//        Log.e("RADAPTER",datas[position]?.get("DATE").toString())
        val l = LocalDate.parse(datas[position]?.get("DATE").toString(), DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss"))

        val unix = l.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy hh:mm a", Locale.getDefault())
        val netDate = Date(unix * 1000)

        Glide.with(holder.imageView.context)
            .load(datas[position]?.get("IMG_URL")?.toString())
            .centerCrop()
            .thumbnail(0.5f)
            .into(holder.imageView)


        val g = TimeAgo().getTimeDur(datas[position]?.get("DATE").toString())
        holder.date.text = datas[position]?.get("DATE").toString()
        //holder.date.text = sdf.format(netDate)

        holder.imageView.setOnClickListener {

            rationCardFragment.show(fm,"image show")
            datas[position]?.get("IMG_URL")?.toString()
                ?.let { it1 -> rationCardFragment.setUrl(it1, holder.imageView.context, datas[position]) }

        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    inner class RationHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var imageView: ImageView
        lateinit var date: TextView
        lateinit var share: Button

        init {
            imageView = itemView.findViewById(R.id.ration_img)
            date = itemView.findViewById(R.id.ration_date)
            share = itemView.findViewById(R.id.ration_share)
        }
    }
}