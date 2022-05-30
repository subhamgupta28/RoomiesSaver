package com.subhamgupta.roomiesapp.adapter


import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.roomiesapp.HAdapToHFrag
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.HomeItemBinding


class HomeAdapter(
    private val hAdapToHFrag: HAdapToHFrag
):RecyclerView.Adapter<HomeAdapter.HomeHolder>() {


    private val data = mutableListOf<MutableMap<String,String>>()
    private var amnt:Double = 0.0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeHolder {
        return HomeHolder(parent)
    }
    override fun onBindViewHolder(holder: HomeHolder, position: Int) {
        holder.onBind(data[position], position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: ArrayList<MutableMap<String, String>>?, amnt: String?){
        this.data.clear()
        this.data.addAll(data!!)
        if (amnt != null) {
            this.amnt = amnt.toDouble()
        }
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return data.size
    }
    inner class HomeHolder(parent: ViewGroup):RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.home_item, parent, false)
    ) {
        private val binding = HomeItemBinding.bind(itemView)
        fun onBind(data:MutableMap<String,String>, position: Int){
           try {
               val name = data["USER_NAME"].toString()
               val color = data["COLOR"]?.toLong()

               if (color != null) {
                   val hexColor = String.format("#%08X", 0xB3FFFFFF and color)
                   val newColor = Color.parseColor(hexColor)
                   binding.homeLayout.strokeWidth = 4
                   binding.homeLayout.strokeColor = (newColor)
               }
               val amount = data["AMOUNT"].toString().toInt()
               if (amnt<amount){
                   (" ↑${String.format("%.1f", amount.toDouble()-amnt)}").also { binding.eachAmt.text = it }
                   binding.eachAmt.setTextColor(Color.parseColor("#FF6D00"))
               }
               else{
                   (" ↓${String.format("%.1f", amnt-amount.toDouble())}").also { binding.eachAmt.text = it }
                   binding.eachAmt.setTextColor(Color.parseColor("#DB4437"))
               }
               ("₹$amount").also { binding.hamount.text = it }
               binding.root.setOnClickListener {
                   hAdapToHFrag.goToHome(position, data["UUID"].toString())
               }
               val n = name[0].uppercase()+name.substring(1)

               binding.hname.text = n
           }catch (e:Exception){

           }

        }
    }


}