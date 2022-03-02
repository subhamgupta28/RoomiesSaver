package com.subhamgupta.roomiesapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.majorik.sparklinelibrary.SparkLineLayout

import com.subhamgupta.roomiesapp.R


class MyNotesFragment : Fragment() {

    lateinit var sparkLineLayout: SparkLineLayout
    lateinit var data: MutableList<Map<String, Any?>?>
    lateinit var db: FirebaseFirestore
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_mynotes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.myrecycler)
        sparkLineLayout = view.findViewById(R.id.myspark)
        val list: ArrayList<Int> = ArrayList()
        list.add(100)
        list.add(10)
        list.add(400)
        list.add(80)
        list.add(1000)
        list.add(10)
        list.add(900)
        sparkLineLayout.setData(list)
    }
}