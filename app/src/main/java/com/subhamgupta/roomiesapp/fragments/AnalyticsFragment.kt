package com.subhamgupta.roomiesapp.fragments


import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.adapter.SplitDetailsAdapter
import com.subhamgupta.roomiesapp.data.viewmodels.MainViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentAnalyticsBinding
import com.subhamgupta.roomiesapp.databinding.PaymentVpaPopupBinding
import com.subhamgupta.roomiesapp.domain.model.SplitData
import com.subhamgupta.roomiesapp.utils.SplitDetailView
import com.subhamgupta.roomiesapp.utils.SplitObserver
import kotlinx.coroutines.flow.buffer

class AnalyticsFragment(
    private val splitObserver: SplitObserver
) : Fragment(), SplitDetailView, SplitObserver {

    private lateinit var binding: FragmentAnalyticsBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnalyticsBinding.inflate(layoutInflater)
        binding.splitRecycler.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.splitButton.setOnClickListener {
            openSheet()
        }

        val adapter = SplitDetailsAdapter(this)
        binding.splitRecycler.adapter = adapter
        lifecycleScope.launchWhenStarted {
            viewModel.splitData.collect {
                Log.e("splitExpense data", "$it")
                adapter.setItem(it, viewModel.getUser()?.uid, requireActivity())
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.splitExpenseOwed.collect {
                binding.owedByYou.text = String.format("%.2f", it.owedByYou.toString().toDouble())
                binding.owedToYou.text = String.format("%.2f", it.owedToYou.toString().toDouble())
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.splitExpenseSuccess.buffer().collect {
                if (it) {
                    showSnackBar("Split created")
                }
            }
        }
    }

    private fun showSnackBar(msg: String) {
        val snackBarView = Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
        val view = snackBarView.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snackBarView.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
        snackBarView.setBackgroundTint(resources.getColor(R.color.colorSecondary))
            .setTextColor(resources.getColor(R.color.colorOnSecondary)).show()
    }

    fun openSheet() {
        val bottomFragment = SplitBottomSheet()
        fragmentManager?.let { it1 -> bottomFragment.show(it1, "Enter new ration") }
    }


    override fun openView(splitData: SplitData) {
//        val splitViewFragment = SplitViewFragment()
        viewModel.splitDataTemp.value = splitData
        splitObserver.click()
//        fragmentManager?.let { it1 -> splitViewFragment.show(it1, "Enter new ration") }
    }

    override fun click() {

    }

    override fun close() {
        TODO("Not yet implemented")
    }

    private fun openVPASavePopup() {
        val dialog = MaterialAlertDialogBuilder(requireActivity())
        val binding = PaymentVpaPopupBinding.inflate(layoutInflater)
        val vpa = binding.vpa.text
        binding.save.setOnClickListener {
            if (vpa != null && vpa.toString().isNotEmpty()) {
                viewModel.saveVPA(vpa.toString())
            }
        }
        dialog.setView(binding.root)

        dialog.background = ColorDrawable(Color.TRANSPARENT)
        dialog.show()
    }

    override fun pay(splitData: SplitData) {
        val GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user"
        val GOOGLE_PAY_REQUEST_CODE = 123
        val myAmount =
            splitData.FOR.filter { it["UUID"] == viewModel.getUser()?.uid.toString() }[0]["AMOUNT"]
        val payeeName = viewModel.getUserDataFromLocal()?.get("USER_NAME")
        val vpa = viewModel.getUserDataFromLocal()?.get("VPA").toString()
        if (vpa.isBlank()) {
            openVPASavePopup()
        } else {
            val uri = Uri.Builder().scheme("upi").authority("pay")
                .appendQueryParameter("pa", "subham2880@ibl")
                .appendQueryParameter("pn", "shubham gupta")
//                .appendQueryParameter("mc", "your-merchant-code")
                .appendQueryParameter("tr", "${System.currentTimeMillis()}")
                .appendQueryParameter("tn", "your-transaction-note")
                .appendQueryParameter("am", "$myAmount")
                .appendQueryParameter("cu", "INR")
                .appendQueryParameter("url", "").build()
            val intent = Intent(Intent.ACTION_VIEW)
            Log.e("upi initiated", "$uri")
            intent.data = uri
            intent.setPackage(GOOGLE_PAY_PACKAGE_NAME)
            try {
                startActivityForResult(intent, GOOGLE_PAY_REQUEST_CODE)
            }catch (e:Exception){
                showSnackBar("No payment app available on this device")
            }
        }

    }
}