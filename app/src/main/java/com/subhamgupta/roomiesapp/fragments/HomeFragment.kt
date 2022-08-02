package com.subhamgupta.roomiesapp.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.text.TextPaint
import android.text.format.DateUtils
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
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.subhamgupta.roomiesapp.HAdapToHFrag
import com.subhamgupta.roomiesapp.HomeToMainLink
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.adapter.HomeAdapter
import com.subhamgupta.roomiesapp.adapter.SummaryAdapter
import com.subhamgupta.roomiesapp.data.viewmodels.MainViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentHomeBinding
import com.subhamgupta.roomiesapp.utils.FirebaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment(private val homeToMainLink: HomeToMainLink? = null) : Fragment(), HAdapToHFrag {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.pRecycle.setHasFixedSize(true)
        binding.itemRecycle.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
        binding.pRecycle.layoutManager = if (!isTablet())
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        else
            StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)


        return binding.root
    }

    private fun isTablet(): Boolean {
        val xlarge = (this.getResources()
            .getConfiguration().screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) === 4
        val large = this.getResources()
            .getConfiguration().screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK === Configuration.SCREENLAYOUT_SIZE_LARGE
        return xlarge || large
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val homeAdapter = HomeAdapter(this)
        val adapter = SummaryAdapter()
        binding.pRecycle.adapter = homeAdapter
        binding.itemRecycle.adapter = adapter
        binding.refresh.setOnClickListener {
            viewModel.refreshData()
        }
        binding.swipe.setOnRefreshListener {
            viewModel.refreshData()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.homeData.buffer().collect {
                withContext(Main) {
                    when (it) {
                        is FirebaseState.Loading -> {
//                            binding.progress.visibility = View.VISIBLE

                        }
                        is FirebaseState.Empty -> {
                        }
                        is FirebaseState.Failed -> {
                            visible()
                        }
                        is FirebaseState.Success -> {

                            val res = it.data
                            if (res.isEmpty)
                                visible()
                            else gone()

                            try {
                                binding.spark.visibility = View.VISIBLE
                                val data = res.todayData
                                adapter.setItems(data)
                                binding.swipe.isRefreshing = false
                                homeAdapter.setData(res.userMap, res.eachPersonAmount)
                                binding.donutView.submitData(res.donutList!!)
                                binding.eachAmt.text = res.eachPersonAmount.toString()
                                "₹${res.todayTotal}".also { binding.todayAmount.text = it }
                                "₹${res.allTotal}".also { binding.totalSpends.text = it }
                                "Updated ${getTimeAgo(res.updatedOn!!)}".also {
                                    binding.updatedOn.text = it
                                }
                                binding.spark.setData(mutableListOf(1, 0, 1) as ArrayList<Int>)
                                binding.spark.setData(res.chartData!!)
                                val sdf = SimpleDateFormat("dd MMM yy", Locale.getDefault())
                                val sd = Date(res.startDate!!.toLong())
                                val ed = Date(System.currentTimeMillis())
                                "${sdf.format(sd)} -- ".also { binding.startsOn.text = it }
                                binding.today.text = sdf.format(ed)
//                                Toast.makeText(requireContext(),  "Room data fetched", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Log.e("onViewCreated: Home Fragment", e.message.toString())
                            }
                        }
                        else -> Unit
                    }

                }

            }

        }
        binding.goToAllExpBtn.setOnClickListener {
            homeToMainLink?.goToAllExpenses()
        }
        binding.goToDiffUser.setOnClickListener {
            homeToMainLink?.goToDiffUser()
        }

    }

    private fun visible() {
        binding.progress.visibility = View.GONE
        binding.emptytext.visibility = View.VISIBLE
        binding.line1.visibility = View.GONE
        binding.k.visibility = View.GONE
        binding.kt.visibility = View.GONE
        lifecycleScope.launchWhenStarted {
            val roomKey = viewModel.getDataStore().getRoomKey()
            try {
                val barcodeEncoder = BarcodeEncoder()
                val bitmap =
                    barcodeEncoder.encodeBitmap(  roomKey+"ID", BarcodeFormat.QR_CODE, 700, 700)
                binding.qrImage.setImageBitmap(bitmap)
            } catch (e: java.lang.Exception) {
            }
        }

    }

    private fun gone() {
        binding.progress.visibility = View.GONE
        binding.emptytext.visibility = View.GONE
        binding.line1.visibility = View.VISIBLE
        binding.k.visibility = View.VISIBLE
        binding.kt.visibility = View.VISIBLE
    }

    private fun showSnackBar(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.md_theme_dark_primary))
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .show()
    }

    private fun getTimeAgo(time: String): String {
        var ago = ""
        try {
            ago = DateUtils.getRelativeTimeSpanString(
                time.toLong(),
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS
            ).toString()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ago
    }

    override fun goToHome(position: Int, uuid: String) {
        homeToMainLink?.goToMain(position, uuid)
    }


}