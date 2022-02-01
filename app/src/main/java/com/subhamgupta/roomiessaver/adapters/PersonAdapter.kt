package com.subhamgupta.roomiessaver.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.lottie.LottieAnimationView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.subhamgupta.roomiessaver.R
import com.subhamgupta.roomiessaver.adapters.ItemsAdapter
import com.subhamgupta.roomiessaver.models.Detail
import com.subhamgupta.roomiessaver.models.PersonModel
import java.time.LocalDate
import java.time.ZoneId

class PersonAdapter(
    options: FirebaseRecyclerOptions<PersonModel>,
    var context: Context,
    var reference: FirebaseFirestore,
    var key: String
): FirebaseRecyclerAdapter<PersonModel, PersonAdapter.PersonHolder>(options) {
    var itemsAdapter: ItemsAdapter? = null
    lateinit var options: FirestoreRecyclerOptions<Detail?>
    inner class RoomHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var label: TextView

        var materialCardView: MaterialCardView

        init {
            label = itemView.findViewById(R.id.labeled)
            materialCardView = itemView.findViewById(R.id.materialcard)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.person_items, parent, false)
        return PersonHolder(view)
    }
    override fun onBindViewHolder(holder: PersonHolder, position: Int, model: PersonModel) {
        holder.user_name.text = model.USERNAME
        reference.collection(key)
            .addSnapshotListener { _: QuerySnapshot?, _: FirebaseFirestoreException? ->
                getData(holder, model, position)
            }
        holder.swipeRefreshLayout.setOnRefreshListener { getData(holder, model, position) }
    }
    private fun getData(holder: PersonHolder, model: PersonModel, position: Int){
        val sdom = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
        Log.e("MODEL","${model.KEY}")
        val query = reference.collection(key)
            .whereEqualTo("UUID", model.UUID)
            .orderBy("TIME_STAMP", Query.Direction.DESCENDING)
            .whereGreaterThanOrEqualTo("TIME_STAMP", sdom*1000)
            options = FirestoreRecyclerOptions.Builder<Detail>()
            .setQuery(query, Detail::class.java)
            .build()
        itemsAdapter = ItemsAdapter(options, context,reference.collection(key))
        holder.recyclerView.adapter = itemsAdapter
        itemsAdapter?.startListening()
        if (options.snapshots.isEmpty()) {
            holder.lottieAnimationView.visibility = View.GONE
            holder.lottieAnimationView.pauseAnimation()
        } else {
            holder.lottieAnimationView.visibility = View.VISIBLE
            holder.lottieAnimationView.playAnimation()
        }
        holder.swipeRefreshLayout.isRefreshing = false
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


}