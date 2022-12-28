package com.subhamgupta.roomiesapp.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.adapter.StoredCardsAdapter
import com.subhamgupta.roomiesapp.data.viewmodels.MainViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentRationBinding
import com.subhamgupta.roomiesapp.utils.FirebaseState
import kotlinx.coroutines.flow.buffer

class RationFragment: Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentRationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRationBinding.inflate(layoutInflater)
        binding.rationRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rationSwipe.setOnRefreshListener {
            viewModel.getStoredCards()

        }

        val adapter = StoredCardsAdapter()
        adapter.setContext(requireActivity().supportFragmentManager)
        binding.rationRecycler.adapter = adapter

        lifecycleScope.launchWhenStarted {
            viewModel.storedCards.buffer().collect{
                when (it) {
                    is FirebaseState.Loading -> {
                    }
                    is FirebaseState.Empty -> {
                        binding.rationSwipe.isRefreshing = false
                        adapter.setData(mutableListOf())
                    }
                    is FirebaseState.Failed -> {
                        binding.rationSwipe.isRefreshing = false

                    }
                    is FirebaseState.Success -> {
                        binding.rationSwipe.isRefreshing = false
                        Log.e("onViewCreated: ", "${it.data}")
                        adapter.setData(it.data)
                    }
                }
            }
        }

    }

    fun openSheet() {
        val bottomFragment = BottomFragment()
        fragmentManager?.let { it1 -> bottomFragment.show(it1, "Enter new ration") }
    }

}


