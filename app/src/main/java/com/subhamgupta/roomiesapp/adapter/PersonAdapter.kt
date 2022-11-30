package com.subhamgupta.roomiesapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.subhamgupta.roomiesapp.EditPopLink
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.PersonItemsBinding
import com.subhamgupta.roomiesapp.domain.model.Detail


class PersonAdapter(
    var editPopLink: EditPopLink,
    var uuid: String
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var personModels = mutableListOf<MutableMap<String, Any>>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonHolder {
        return PersonHolder(parent)
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PersonHolder).onBind(personModels[position], position)

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: MutableList<MutableMap<String, Any>>?){
        personModels.clear()
        personModels.addAll(data?: emptyList())
        notifyDataSetChanged()
    }
    inner class PersonHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.person_items, parent, false)

    ) {
        private val binding = PersonItemsBinding.bind(itemView)

        fun onBind(model: MutableMap<String, Any>,  position: Int){
            binding.personRecycler.layoutManager =
                StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
            val adapter = ItemsAdapter(editPopLink, uuid)
            binding.personRecycler.adapter = adapter

            val md = model["DATA"] as List<Detail>
            try {
                adapter.setItems(md)
                binding.userName.text = md[position].BOUGHT_BY.toString()
            }catch (e:Exception){
                e.printStackTrace()
            }


        }
    }




    override fun getItemCount(): Int {
        return personModels.size
    }


}