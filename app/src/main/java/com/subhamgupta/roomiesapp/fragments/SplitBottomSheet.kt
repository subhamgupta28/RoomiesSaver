package com.subhamgupta.roomiesapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.subhamgupta.roomiesapp.adapter.SplitPersonAdapter
import com.subhamgupta.roomiesapp.data.viewmodels.MainViewModel
import com.subhamgupta.roomiesapp.databinding.SplitBottomSheetBinding
import com.subhamgupta.roomiesapp.domain.model.ROOMMATES
import com.subhamgupta.roomiesapp.utils.Constant.Companion.DATE_STRING
import com.subhamgupta.roomiesapp.utils.Constant.Companion.TIME_STRING
import com.subhamgupta.roomiesapp.utils.SplitObserver
import kotlinx.coroutines.flow.buffer
import java.text.SimpleDateFormat
import java.util.*

class SplitBottomSheet : BottomSheetDialogFragment(), SplitObserver {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: SplitBottomSheetBinding
    private val roomieData = mutableMapOf<String, Any>()
    private val selectedEdit = mutableMapOf<String, Any>()
    private lateinit var roomAdapter :SplitPersonAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SplitBottomSheetBinding.inflate(layoutInflater)
        binding.splitPersonRecycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomAdapter = SplitPersonAdapter(selectedEdit, this, false)
        lifecycleScope.launchWhenStarted {
            viewModel.getRoomMates().buffer().collect {
                binding.splitPersonRecycler.adapter = roomAdapter
                if (it != null) {
                    it.forEach { mate ->
                        roomieData[mate.UUID.toString()] = 0.0
                    }
                    roomAdapter.setItem(it as ArrayList<ROOMMATES>, viewModel.getUser()?.uid)
                    roomAdapter.setRoomieData(roomieData)
                }
            }

        }
        lifecycleScope.launchWhenStarted {
            viewModel.splitExpenseSuccess.buffer().collect {
                if (it) {
//                    dismissAllowingStateLoss()
                }
            }
        }
        binding.splitPersonRecycler.visibility = View.GONE
        binding.message.visibility = View.GONE
        binding.splitAmount.addTextChangedListener {
            val amount = binding.splitAmount.text

            lifecycleScope.launchWhenStarted {
                Log.e("adapter", "$roomieData")
                if (!amount.isNullOrEmpty()) {
                    binding.splitPersonRecycler.visibility = View.VISIBLE
                    binding.message.visibility = View.VISIBLE
                    val eachPerson =
                        amount.toString().toDouble() / viewModel.getDataStore().getRoomSize()
                    roomieData.forEach {
                        roomieData[it.key] = eachPerson
                    }
                    roomAdapter.setRoomieData(roomieData)
                    selectedEdit.clear()
                }
            }
        }
        binding.doneButton.setOnClickListener {

        }
        binding.splitButton.setOnClickListener {
            val amount = binding.splitAmount.text.toString()
            val splitFor = binding.splitFor.text
            Log.e("adapter", "$roomieData")
            if (amount != null && amount.isNotEmpty()) {
                roomAdapter.setRoomieData(roomieData)
                viewModel.splitExpense(amount.trim().toString(), "$splitFor", roomieData)
            }

        }
    }

    val time: String
        get() {
            val date = Date()
            val sdm = SimpleDateFormat(TIME_STRING, Locale.getDefault())
            return sdm.format(date)
        }
    val date: String
        get() {
            val date = Date()
            val sdm = SimpleDateFormat(DATE_STRING, Locale.getDefault())
            return sdm.format(date)
        }

    override fun click() {
        val amount = binding.splitAmount.text.toString()
        if (amount.isNotEmpty()) {
            val edited = roomieData.filter { selectedEdit.containsKey(it.key) }
            val editedSum = selectedEdit.values.sumOf { it as Double }
            val newEach = (amount.toDouble() - editedSum) / (roomieData.size - selectedEdit.size)
            Log.e("splitExpense", "$edited\n$editedSum\n$newEach")
            val nonEdit = roomieData.filter {
                !selectedEdit.containsKey(it.key)
            }.forEach {
                roomieData[it.key] = newEach
            }
            roomAdapter.setRoomieData(roomieData)
        }
    }

    override fun close() {

    }

}