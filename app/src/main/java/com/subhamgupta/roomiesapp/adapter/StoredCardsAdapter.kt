package com.subhamgupta.roomiesapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.RationCardBinding

class StoredCardsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var storedCards = mutableListOf<MutableMap<String, Any>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return StoredCardsHolder(parent)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: MutableList<MutableMap<String, Any>>?){
        storedCards.clear()
        storedCards.addAll(data?: emptyList())
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as StoredCardsHolder).onBind(storedCards[position])
    }

    override fun getItemCount(): Int {
        return storedCards.size
    }

    inner class StoredCardsHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.ration_card, parent, false)

    ) {
        private val binding = RationCardBinding.bind(itemView)

        fun onBind(data: MutableMap<String, Any>){
            val requestOptions =
                RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            val glide = Glide.with(binding.rationImg.context)
                .load(data?.get("IMG_URL")?.toString())
                .apply(requestOptions)
            binding.rationProgress.visibility = View.GONE
            glide.into(binding.rationImg)
        }
    }
}