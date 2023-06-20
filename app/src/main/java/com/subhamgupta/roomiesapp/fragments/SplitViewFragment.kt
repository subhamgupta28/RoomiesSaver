package com.subhamgupta.roomiesapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.subhamgupta.roomiesapp.adapter.SplitPersonAdapter
import com.subhamgupta.roomiesapp.data.viewmodels.MainViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentSplitViewBinding
import com.subhamgupta.roomiesapp.domain.model.ROOMMATES
import com.subhamgupta.roomiesapp.utils.SplitObserver
import kotlinx.coroutines.flow.buffer

class SplitViewFragment(
    private val splitObserver: SplitObserver
) : Fragment(), SplitObserver {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentSplitViewBinding
    private val userNameVs = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSplitViewBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.getRoomMates().buffer().collect {
                it?.let { it1 ->
                    it1.forEach {
                        userNameVs[it.UUID.toString()] = it.USER_NAME.toString()
                    }
                }
            }
        }
        binding.close.setOnClickListener {
            splitObserver.close()
        }
        lifecycleScope.launchWhenStarted {
            viewModel.splitDataTemp.collect { splitData ->
                binding.amount.text = splitData?.TOTAL_AMOUNT.toString()
                val item = splitData?.ITEM
                "${splitData?.BY_NAME} ${if (!item.isNullOrEmpty()) "has requested for split bill" else ""}".also {
                    binding.message.text = it
                }
                binding.splitPersonRecycler.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                val roomData = mutableMapOf<String, Any>()
                val adapter = SplitPersonAdapter(roomData, this@SplitViewFragment, true)
                binding.splitPersonRecycler.adapter = adapter
                val roommates = ArrayList<ROOMMATES>()
                splitData?.FOR?.forEach {
                    val roommate = ROOMMATES()
                    roommate.USER_NAME = userNameVs[it["UUID"]]
                    roommate.MONEY_PAID = it["AMOUNT"].toString().toDouble()
                    roommates.add(roommate)
                }
                adapter.setItem(roommates, viewModel.getUser()?.uid)
            }
        }
    }

    override fun click() {

    }

    override fun close() {
        TODO("Not yet implemented")
    }
}