package com.subhamgupta.roomiesapp.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.subhamgupta.roomiesapp.EditPopLink
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.adapter.PersonAdapter
import com.subhamgupta.roomiesapp.adapter.RoomieAdapter
import com.subhamgupta.roomiesapp.data.viewmodels.FirebaseViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentDiffUserBinding
import com.subhamgupta.roomiesapp.databinding.PopupBinding
import com.subhamgupta.roomiesapp.models.Detail
import com.subhamgupta.roomiesapp.onClickPerson
import kotlin.math.abs

class DiffUser: Fragment(), EditPopLink, onClickPerson {

    private lateinit var binding: FragmentDiffUserBinding
    private val viewModel: FirebaseViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDiffUserBinding.inflate(layoutInflater)

        binding.personRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)




        return binding.root


    }
    fun refresh(){
        viewModel.getDiffData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = PersonAdapter(this, viewModel.getUser()?.uid.toString())
        binding.viewpager.adapter = adapter
        binding.viewpager.clipToPadding = false
        binding.viewpager.clipChildren = false
        binding.viewpager.offscreenPageLimit = 10
        viewModel.getDiffData().observe(viewLifecycleOwner) {
            adapter.setData(it)
            binding.swipe.isRefreshing = false
        }
        binding.swipe.setOnRefreshListener {
            viewModel.getDiffData()

        }
        val roomAdapter = RoomieAdapter(this)
        viewModel.getRoomMates().observe(viewLifecycleOwner){

            binding.personRecycler.adapter = roomAdapter
            if (it != null) {
                roomAdapter.setItem(it)
            }
        }

        val transformer = CompositePageTransformer()
        transformer.addTransformer { page: View, position: Float ->
            val a = 1 - abs(position)
            page.scaleY = 0.85f + a * 0.15f

        }

        binding.viewpager.setPageTransformer(transformer)

        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                try {
                    for (i in 0..roomAdapter.itemCount) {
                        if (
                            i == position
                        ) {
                            recP(position, true)

                        } else {
                            recP(i, false)
                        }
                    }
                } catch (e: java.lang.Exception) {

                }

            }
        })
    }
    fun recP(position: Int, boolean: Boolean){
        val temp = binding.personRecycler[position].findViewById<MaterialCardView>(R.id.materialcard)
        if (boolean){
            temp.setCardBackgroundColor(Color.parseColor("#814285F4"))
            temp.scaleX = 1.05F
            temp.scaleY = 1.05F
        }else{
            temp.setCardBackgroundColor(Color.parseColor("#284285F4"))
            temp.scaleX = 0.95F
            temp.scaleY = 0.95F
        }
    }

    override fun onClick(model: Detail) {
        val mcard = MaterialAlertDialogBuilder(requireContext())
        val view = PopupBinding.inflate(layoutInflater)
        view.saveBtn.text = "Edit & Save"
        view.itemBought.setText(model.ITEM_BOUGHT)
        view.amountPaid.setText(model.AMOUNT_PAID.toString())
        view.saveBtn.setOnClickListener {
            val itemName = view.itemBought.text.toString()
            val amountPaid = view.amountPaid.text.toString()
            viewModel.updateItem(itemName, amountPaid, model.TIME_STAMP.toString())
            Toast.makeText(context, "Saved", Toast.LENGTH_LONG).show()
        }
        mcard.setView(view.root)
        mcard.background = ColorDrawable(Color.TRANSPARENT)
        mcard.show()
    }

    fun goToUser(position: Int, uuid: String){
        viewModel.getUuidLink()[uuid]?.let { binding.viewpager.setCurrentItem(it, true) }
    }

    override fun onClick(position: Int) {
        binding.viewpager.currentItem = position
    }

}