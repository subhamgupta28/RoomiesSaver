package com.subhamgupta.roomiesapp.fragments


import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.subhamgupta.roomiesapp.data.viewmodels.FirebaseViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentAnalyticsBinding
import com.subhamgupta.roomiesapp.utils.Constant.Companion.DATE_STRING
import com.subhamgupta.roomiesapp.utils.FirebaseState
import im.dacer.androidcharts.LineView
import kotlinx.coroutines.flow.buffer
import java.text.SimpleDateFormat


class AnalyticsFragment : Fragment() {

    private lateinit var binding: FragmentAnalyticsBinding
    private val viewModel: FirebaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnalyticsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lineView = binding.lineView
        lineView.setDrawDotLine(false) //optional

        lineView.setShowPopup(LineView.SHOW_POPUPS_MAXMIN_ONLY) //optional
        val strList = ArrayList<String>()
//        lifecycleScope.launchWhenStarted {
//            viewModel.summary.buffer().collect {
//                when (it) {
//                    is FirebaseState.Loading -> {
//                    }
//                    is FirebaseState.Empty -> {
//                    }
//                    is FirebaseState.Failed -> {
//                    }
//                    is FirebaseState.Success -> {
//                        val details = it.data
//                        val map = mutableMapOf<String,HashMap<String, Int>>()
//                        details.forEach { detail ->
//                            val sd = SimpleDateFormat(DATE_STRING)
//                            val df = SimpleDateFormat("dd")
//                            val date = df.format(sd.parse(detail!!.DATE.toString())!!)
//                            val uuid = detail.UUID.toString()
//                            val amount = detail.AMOUNT_PAID.toInt()
//                            if (map.containsKey(uuid)) {
//                                if (strList.contains(date)){
//                                    val mp = map[uuid]!!
//                                    if (mp.containsKey(date)){
//                                        mp[date] = mp[date]!!.plus(amount)
//                                    }else
//                                        mp[date] = amount
//                                }
//                            } else{
//                                map[uuid] = HashMap()
//                            }
//                            if (!strList.contains(date))
//                                strList.add(date)
//                        }
//                        Log.e("DATA", "$map $strList")
//                        lineView.setBottomTextList(strList)
//                        lineView.setColorArray(
//                            intArrayOf(
//                                Color.BLUE,
//                                Color.RED,
//                                Color.GRAY,
//                                Color.CYAN
//                            )
//                        )
//                        val dataLists = ArrayList<ArrayList<Int>>()
//                        map.forEach { (k, v) ->
//                            val list = ArrayList<Int>()
//                            strList.forEach { i->
//                                if (v.containsKey(i)){
//                                    list.add(v[i]!!)
//                                }
//                                else
//                                    list.add(0)
//                            }
//                            dataLists.add(list)
//                        }
//                        Log.e("Data","$dataLists")
//                        lineView.setDataList(dataLists) //or lineView.setFloatDataList(floatDataLists)
//                    }
//                }
//            }
//        }


    }


}