package com.subhamgupta.roomiesapp.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.subhamgupta.roomiesapp.data.viewmodels.MainViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentAnalyticsBinding
import im.dacer.androidcharts.LineView


class AnalyticsFragment : Fragment() {

    private lateinit var binding: FragmentAnalyticsBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnalyticsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


}