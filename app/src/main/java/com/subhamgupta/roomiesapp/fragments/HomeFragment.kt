package com.subhamgupta.roomiesapp.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextPaint
import android.text.format.DateUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
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
import com.subhamgupta.roomiesapp.databinding.LoadingPopupBinding
import com.subhamgupta.roomiesapp.utils.ConnectivityObserver
import com.subhamgupta.roomiesapp.utils.FirebaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs


class HomeFragment(private val homeToMainLink: HomeToMainLink? = null) : Fragment(), HAdapToHFrag {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var loadingDialog: AlertDialog

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


//        netStat()
        return binding.root
    }

    private fun netStat() {
        viewModel.getNetworkObserver().observe().onEach {
//            if (it.name == "Lost")
//            showSnackBar("Network ${it.name}")

        }.launchIn(lifecycleScope)
    }


    private fun isTablet(): Boolean {
        val xlarge = (this.resources
            .configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) == 4
        val large = this.resources
            .configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE
        return xlarge || large
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireActivity())
        val loadingView = LoadingPopupBinding.inflate(layoutInflater)
        materialAlertDialogBuilder.setView(loadingView.root)
        materialAlertDialogBuilder.background = ColorDrawable(Color.TRANSPARENT)
        loadingDialog = materialAlertDialogBuilder.create()
        if (!loadingDialog.isShowing)
            loadingDialog.show()
        lifecycleScope.launchWhenStarted {
            viewModel.userData.collectLatest {
                it["IS_ROOM_JOINED"]?.let { b ->
                    if (!b.toString().toBoolean()) {
                        if (loadingDialog.isShowing)
                            loadingDialog.dismiss()
                    }
                }
            }
        }

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
        lifecycleScope.launchWhenStarted {
            viewModel.homeDetails.buffer().collect {
                Log.e("home1", "$it")
                adapter.setItems(it)
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.homeDonut.buffer().collect {
                Log.e("home2", "$it")
                try {
                    binding.donutView.submitData(it)
                } catch (e: Exception) {

                }

            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.homeUserMap.buffer().collect {
                Log.e("home3", "$it")
                if (it.eachPersonAmount != null && it.userMap != null)
                    homeAdapter.setData(it.userMap, it.eachPersonAmount)
                binding.eachAmt.text = it.eachPersonAmount.toString()
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.homeData.buffer().collect {
                withContext(Main) {
                    when (it) {
                        is FirebaseState.Loading -> {

                        }
                        is FirebaseState.Empty -> {
                            loadingDialog.dismiss()
                            visible()
                        }
                        is FirebaseState.Failed -> {
                            loadingDialog.dismiss()
                            visible()
                        }
                        is FirebaseState.Success -> {
                            loadingDialog.dismiss()
                            val res = it.data
                            if (res.isEmpty)
                                visible()
                            else gone()

                            try {

                                binding.spark.visibility = View.VISIBLE
//
                                binding.swipe.isRefreshing = false
//

                                "₹${res.todayTotal}".also { binding.todayAmount.text = it }
                                "₹${res.allTotal}".also { binding.totalSpends.text = it }
                                "Updated ${viewModel.getTimeAgo(res.updatedOn!!)}".also {
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
                    barcodeEncoder.encodeBitmap(roomKey + "ID", BarcodeFormat.QR_CODE, 700, 700)
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
        val snackBarView = Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
        val view = snackBarView.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snackBarView.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
        snackBarView.setBackgroundTint(resources.getColor(R.color.colorSecondary))
            .setTextColor(resources.getColor(R.color.colorOnSecondary))
            .show()
    }



    override fun goToHome(position: Int, uuid: String) {
        homeToMainLink?.goToMain(position, uuid)
    }


}