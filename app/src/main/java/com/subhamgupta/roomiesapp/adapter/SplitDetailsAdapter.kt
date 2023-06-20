package com.subhamgupta.roomiesapp.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.SplitDetailItemBinding
import com.subhamgupta.roomiesapp.domain.model.SplitData
import com.subhamgupta.roomiesapp.utils.SplitDetailView

class SplitDetailsAdapter(
    var splitDetailView: SplitDetailView
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val data = mutableListOf<SplitData?>()
    private var uuid: String = ""
    private var context: Activity? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(parent)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItem(data: List<SplitData?>, uuid: String?, context: Activity) {
        this.data.clear()
        this.data.addAll(data)
        this.context = context
        uuid?.let {
            this.uuid = it
        }
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        data[position]?.let { (holder as MyViewHolder).onBind(it) }
    }

    inner class MyViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.split_detail_item, parent, false)
    ) {
        private val binding = SplitDetailItemBinding.bind(itemView)

        fun onBind(splitData: SplitData) {
            "₹${splitData.TOTAL_AMOUNT}".also { binding.amount.text = it }
            val item = splitData.ITEM
            "${if (splitData.UUID == uuid) "You" else splitData.BY_NAME} ${if (item.isEmpty()) "" else "requested for ${splitData.ITEM}"}".also {
                binding.boughtBy.text = it
            }
            binding.date.text = splitData.TIME
            binding.itemName.text = splitData.ITEM
            val present = splitData.FOR.any {
                it["UUID"] == uuid
            }
            val myAmount = splitData.FOR.filter { it["UUID"] == uuid }[0]["AMOUNT"]
            "Pay $myAmount".also { binding.payBtn.text = it }
            binding.payBtn.visibility =
                if (splitData.UUID == uuid || !present) View.GONE else View.VISIBLE

            binding.parent.setOnClickListener {
                splitDetailView.openView(splitData)
            }
            binding.payBtn.setOnClickListener {
                splitDetailView.pay(splitData)
                context?.let { it1 ->
//                    val easyUpiPayment = EasyUpiPayment(it1) {
//                        this.payeeVpa = "6370569356@apl"
//                        this.payeeName = "Subham"
//                        this.payeeMerchantCode = "12345"
//                        this.transactionId = "${System.currentTimeMillis()}"
//                        this.transactionRefId = "${System.currentTimeMillis()}"
//                        this.description = "paying for roomies app"
//                        this.amount = "5.00"
//                    }
//                    easyUpiPayment.startPayment()

                }

            }

        }

    }

    override fun getItemCount(): Int {
        return data.size
    }
}