package com.subhamgupta.roomiessaver.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.majorik.sparklinelibrary.SparkLineLayout

import com.subhamgupta.roomiessaver.R
import kotlin.random.Random


class ChartsFragment : Fragment() {

    lateinit var sparkLineLayout: SparkLineLayout
    lateinit var data: MutableList<Map<String, Any?>?>
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_charts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sparkLineLayout = view.findViewById(R.id.spark_view)

        val list: ArrayList<Int> = ArrayList()
        list.add(100)
        list.add(10)
        list.add(100)
        list.add(10)
        list.add(100)
        list.add(10)
        list.add(100)
        sparkLineLayout.setData(list)
    }
}