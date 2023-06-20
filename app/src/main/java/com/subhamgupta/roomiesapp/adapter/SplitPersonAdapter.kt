package com.subhamgupta.roomiesapp.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.SplitPersonItemBinding
import com.subhamgupta.roomiesapp.domain.model.ROOMMATES
import com.subhamgupta.roomiesapp.utils.SplitObserver

class SplitPersonAdapter(
    private val selectedEdit: MutableMap<String, Any>,
    private val splitObserver: SplitObserver,
    private val editable:Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val roomMates = mutableListOf<ROOMMATES>()
    private var roomieData = mutableMapOf<String, Any>()

    private var uuid: String = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(parent)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItem(roommates: ArrayList<ROOMMATES>, uuid: String?) {
        this.roomMates.clear()
        this.roomMates.addAll(roommates)
        uuid?.let {
            this.uuid = it
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setRoomieData(roomieData: MutableMap<String, Any>) {
        this.roomieData = roomieData
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MyViewHolder).onBind(roomMates[position])
    }

    inner class MyViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.split_person_item, parent, false)
    ) {
        private val binding = SplitPersonItemBinding.bind(itemView)

        fun onBind(roommates: ROOMMATES) {

            binding.personName.text =
                if (uuid == roommates.UUID) "You" else roommates.USER_NAME.toString()
            val amt = roomieData[roommates.UUID.toString()]
            if (amt != null) {
                binding.amount.setText(String.format("%.2f", amt.toString().toDouble()))
                binding.amountText.text = String.format("%.2f", amt.toString().toDouble())
            } else {
                binding.amount.setText(roommates.MONEY_PAID.toString())
                binding.amountText.text = String.format("%.2f", roommates.MONEY_PAID)
            }
            if (binding.checkBox.isChecked) {
                if (binding.amount.text != null && binding.amount.text.toString().isNotEmpty()) {
                    val amount = binding.amount.text.toString().toDouble()
                    roomieData[roommates.UUID.toString()] = amount
                }
            }
            if (editable) {
                binding.checkBox.visibility = View.GONE
                binding.doneBtn.visibility = View.GONE
                binding.amount.visibility = View.GONE
                binding.amountText.visibility = View.VISIBLE
            } else {
                binding.doneBtn.visibility = View.VISIBLE
                binding.amountText.visibility = View.GONE
            }
            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                Log.e("checked b", "$roomieData\n$selectedEdit")
                if (!isChecked) {
                    roomieData.remove(roommates.UUID.toString())
                    selectedEdit.remove(roommates.UUID.toString())
                } else
                    roomieData[roommates.UUID.toString()] =
                        roommates.MONEY_PAID.toString().toDouble()
                splitObserver.click()
                Log.e("checked a", "$roomieData\n$selectedEdit")
            }
//            binding.checkBox.isChecked = roomieData[roommates.UUID.toString()] == 0.0


            binding.doneBtn.setOnClickListener {
                if (binding.amount.text != null && binding.amount.text.toString().isNotEmpty()) {
                    val amount = binding.amount.text.toString().toDouble()
                    roomieData[roommates.UUID.toString()] = amount
                    selectedEdit[roommates.UUID.toString()] = amount
                    binding.doneBtn.visibility = View.GONE
                    splitObserver.click()
                }
            }
            Log.e("adapter", "$roomieData")
        }

    }

    override fun getItemCount(): Int {
        return roomMates.size
    }

}