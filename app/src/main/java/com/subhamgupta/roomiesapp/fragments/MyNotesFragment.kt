package com.subhamgupta.roomiesapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.subhamgupta.roomiesapp.adapter.AllRoomAdapter
import com.subhamgupta.roomiesapp.data.viewmodels.MainViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentMynotesBinding


class MyNotesFragment : Fragment() {
    private lateinit var binding: FragmentMynotesBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMynotesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.myrecycler.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
        val adapter = AllRoomAdapter()
        binding.myrecycler.adapter = adapter
        viewModel.getAllRoomsDetails().observe(viewLifecycleOwner, Observer {
            adapter.setItems(it)
        })
        binding.fetch.setOnClickListener {
            viewModel.getAllRoomsDetails().value?.let { it1 -> adapter.setItems(it1) }
        }
    }
    private fun addItem(){

    }
}