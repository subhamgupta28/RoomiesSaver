package com.subhamgupta.roomiessaver.adapters

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.lottie.LottieAnimationView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.subhamgupta.roomiessaver.Contenst
import com.subhamgupta.roomiessaver.R
import com.subhamgupta.roomiessaver.adapters.PersonAdapter.PersonHolder
import com.subhamgupta.roomiessaver.models.Detail
import com.subhamgupta.roomiessaver.onClickPerson
import com.subhamgupta.roomiessaver.utility.SettingsStorage
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class PersonAdapter(
    var uuids: List<Map<String, String>>,
    var context: Context,
    var reference: FirebaseFirestore,
    var ref: DatabaseReference,
    var key: String,
    var onClickPerson: onClickPerson
) : RecyclerView.Adapter<PersonHolder>(), onClickPerson {
    var sum = 0
    var itemsAdapter: ItemsAdapter? = null


    @Volatile
    var sumMap: MutableMap<Int, Int>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.person_items, parent, false)
        return PersonHolder(view)
    }

    override fun onBindViewHolder(holder: PersonHolder, position: Int) {
        holder.user_name.text = uuids[position]["USER_NAME"].toString()
        holder.price_text.text = sum.toString()
        reference.collection(key)
            .addSnapshotListener { _: QuerySnapshot?, _: FirebaseFirestoreException? ->
                setData(
                    holder,
                    position
                )
            }
        holder.swipeRefreshLayout.setOnRefreshListener { setData(holder, position) }
        setData(holder, position)
    }

    fun setData(holder: PersonHolder, position: Int) {
        sumMap.clear()
        val query = reference.collection(key)
            .whereEqualTo("UUID", uuids[position]["UUID"])
            .orderBy("DATE", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<Detail>()
            .setQuery(query, Detail::class.java)
            .build()
        holder.price_text.text = sumMap[position].toString()
        itemsAdapter = ItemsAdapter(options, context, this@PersonAdapter, sumMap, position)
        holder.recyclerView.adapter = itemsAdapter
        itemsAdapter!!.startListening()
        if (options.snapshots.isEmpty()) {
            holder.lottieAnimationView.visibility = View.GONE
            holder.lottieAnimationView.pauseAnimation()
        } else {
            holder.lottieAnimationView.visibility = View.VISIBLE
            holder.lottieAnimationView.playAnimation()
        }
        holder.swipeRefreshLayout.isRefreshing = false
    }

    override fun getItemCount(): Int {
        return uuids.size
    }

    override fun onClick(position: Int) {}
    override fun onIssue() {}
    override fun sendSumMap(sumMap: MutableMap<Int, Int>) {
        var newMap = HashMap<String, String>()
        for (i in sumMap){
            newMap[i.key.toString()] = i.value.toString()
        }



    }


    val date: String
        get() {
            val date = Date()
            val sdm = SimpleDateFormat(Contenst.DATE_STRING, Locale.getDefault())
            return sdm.format(date)
        }

    override fun sendSum(sum: Int) {
//        Log.e("PSUM", sum.toString())
        this.sum = sum
    }

    override fun openEdit() {

    }

    inner class PersonHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var recyclerView: RecyclerView
        var swipeRefreshLayout: SwipeRefreshLayout
        var lottieAnimationView: LottieAnimationView
        var price_text: TextView
        var user_name: TextView

        init {
            recyclerView = itemView.findViewById(R.id.person_recycler)
            price_text = itemView.findViewById(R.id.price_text)
            user_name = itemView.findViewById(R.id.user_name)
            lottieAnimationView = itemView.findViewById(R.id.animationView)
            swipeRefreshLayout = itemView.findViewById(R.id.swipe)
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager =
                StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
        }
    }


    init {
        this.key = key
        this.onClickPerson = onClickPerson
        this.sumMap = HashMap()
    }
}