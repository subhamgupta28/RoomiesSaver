package com.subhamgupta.roomiesapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.chip.Chip
import com.subhamgupta.roomiesapp.adapter.SummaryAdapter
import com.subhamgupta.roomiesapp.data.viewmodels.FirebaseViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentSummaryBinding
import com.subhamgupta.roomiesapp.models.Detail
import com.subhamgupta.roomiesapp.utils.FirebaseState
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch

class Summary : Fragment() {

    lateinit var binding: FragmentSummaryBinding
    private val viewModel: FirebaseViewModel by activityViewModels()
    lateinit var adapter: SummaryAdapter
    private val currMonth = MutableLiveData(true)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSummaryBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onStart() {
        super.onStart()
        binding.recycler.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
        viewModel.getTotalAmount().observe(viewLifecycleOwner) { it1 ->
            "₹$it1".also { binding.totalSpends.text = it }
        }


        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            binding.switch1.text = if (isChecked) "This Month's" else "All Time"
            currMonth.postValue(!isChecked)
            viewModel.fetchSummary(isChecked, "All")
        }
        adapter = SummaryAdapter()
        binding.recycler.adapter = adapter
        setData()
        binding.category.children.forEach { chip ->
            (chip as Chip).setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val text = chip.text.toString()
                    viewModel.fetchSummary(binding.switch1.isChecked, text)
                    binding.searchLayout.visibility = if (text == "Other") {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
            }
        }
    }

    private fun setData() {
        lifecycleScope.launchWhenStarted {
            val it = viewModel.getDataStore().isMonth()
            binding.switch1.text = if (it) "This Month's" else "All Time"
            binding.switch1.isChecked = it
            viewModel.summary.buffer().collect {
                when (it) {
                    is FirebaseState.Loading -> {
                        binding.progress.isVisible = true
                    }
                    is FirebaseState.Empty -> {
                        binding.progress.isVisible = false
                        binding.emptytext.isVisible = true
                        adapter.setItems(ArrayList())
                    }
                    is FirebaseState.Failed -> {
                        binding.progress.isVisible = false

                    }
                    is FirebaseState.Success -> {
                        adapter.setItems(it.data as List<Detail>?)
                        binding.progress.isVisible = false
                        binding.emptytext.isVisible = false
                    }
                }
            }
        }
    }


}