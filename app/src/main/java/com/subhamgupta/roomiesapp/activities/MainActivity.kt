package com.subhamgupta.roomiesapp.activities

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.format.DateUtils
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging
import com.subhamgupta.roomiesapp.Contenst.Companion.DATE_STRING
import com.subhamgupta.roomiesapp.Contenst.Companion.TIME_STRING
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.fragments.*
import com.subhamgupta.roomiesapp.interfaces.HomeToMain
import com.subhamgupta.roomiesapp.services.FirebaseService
import com.subhamgupta.roomiesapp.utility.NotificationSender
import com.subhamgupta.roomiesapp.utility.SettingsStorage
import com.subhamgupta.roomiesapp.utility.ViewPagerAdapter
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), HomeToMain {
    lateinit var progressBar: ProgressBar
    lateinit var add_item: FloatingActionButton

    lateinit var id_text: TextView
    lateinit var save_btn: Button
    lateinit var alert_btn: Button
    lateinit var alert_cancel: Button
    lateinit var amount_layout: TextInputLayout
    lateinit var connectivityManager: ConnectivityManager
    lateinit var ref: DatabaseReference
    lateinit var user_ref: DatabaseReference
    lateinit var item_bought: TextInputEditText
    lateinit var roomRef: String
    lateinit var amount_paid: TextInputEditText
    lateinit var alert_et: TextInputEditText
    lateinit var msg_t: TextInputEditText
    lateinit var materialCardView: MaterialCardView
    lateinit var alert_text: TextView
    lateinit var room_mates: ArrayList<HashMap<String, Any>>
    lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
    lateinit var viewPager2: ViewPager
    lateinit var bottomAppBar: BottomAppBar
    lateinit var settingsStorage: SettingsStorage
    lateinit var user: FirebaseUser
    var key: String? = null
    lateinit var user_name: String
    lateinit var map: MutableMap<String?, String>
    lateinit var mAuth: FirebaseAuth
    lateinit var recyclerView: RecyclerView
    lateinit var db: FirebaseFirestore
    lateinit var diffUser: DiffUser
    var uuid: String? = null
    lateinit var summary: Summary
    lateinit var rationFragment: RationFragment
    lateinit var tabLayout: TabLayout

    //    lateinit var issuesFragment: IssuesFragment
    lateinit var myNotesFragment: MyNotesFragment
    lateinit var homeFragment: HomeFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewPager2 = findViewById(R.id.viewpager1)
        add_item = findViewById(R.id.floatingbtn)
        bottomAppBar = findViewById(R.id.bottombar)
        id_text = findViewById(R.id.id_text)
        tabLayout = findViewById(R.id.tablayout)
        materialCardView = findViewById(R.id.mdc)
        alert_text = findViewById(R.id.alert_text)
        alert_cancel = findViewById(R.id.alert_cancel)
        progressBar = findViewById(R.id.progress)

        materialCardView.visibility = View.GONE
        setSupportActionBar(bottomAppBar)
        progressBar.visibility = View.VISIBLE

        settingsStorage = SettingsStorage(this)
        if (!settingsStorage.isRoom_joined)
            supportFinishAfterTransition()
        roomRef = settingsStorage.roomRef.toString()
        Handler().postDelayed({
            init()
        }, 1000)
    }

    private fun setFCM() {
        FirebaseService.sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE)
        FirebaseService.uid = uuid
        FirebaseMessaging.getInstance()
            .subscribeToTopic("/topics/${settingsStorage.room_id.toString()}")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                }
            }


    }

    private fun shareRoomId() {
        val shareIntent = ShareCompat.IntentBuilder.from(this)
            .setType("text/plain")
            .setText("${settingsStorage.username} has invited you to join the room. Click on link to join. https://roomies.app/${settingsStorage.room_id}")
            .intent
        if (shareIntent.resolveActivity(packageManager) != null) {
            startActivity(shareIntent)
        }
    }

    private fun init() {
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser!!
        db = FirebaseFirestore.getInstance()
        ref = FirebaseDatabase.getInstance().reference.child("ROOMIES")
        progressBar.visibility = View.GONE
        user_ref = ref.child(user.uid)
        uuid = user.uid
        settingsStorage.uuid = uuid.toString()
        netStat()
        tabLayout.setupWithViewPager(viewPager2)
        homeFragment = HomeFragment(this)
        diffUser = DiffUser()
        summary = Summary()
        //issuesFragment = IssuesFragment()
        rationFragment = RationFragment()
        myNotesFragment = MyNotesFragment()

        updateChanges()
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, 0)
        viewPagerAdapter.addFragments(homeFragment, "Home")
        viewPagerAdapter.addFragments(diffUser, "Roomie Expenses")
        viewPagerAdapter.addFragments(summary, "All Expenses")
        viewPagerAdapter.addFragments(rationFragment, "Items")
        viewPager2.offscreenPageLimit = 3
        settingsStorage.isRoom_joined = true
        viewPager2.adapter = viewPagerAdapter
        id_text.setOnClickListener {
            shareRoomId()
        }
        val bottomBarBackground = bottomAppBar.background as MaterialShapeDrawable
        bottomBarBackground.shapeAppearanceModel = bottomBarBackground.shapeAppearanceModel
            .toBuilder()
            .setTopRightCorner(CornerFamily.ROUNDED, 30f)
            .setTopLeftCorner(CornerFamily.ROUNDED, 30f)
            .build()
        try {
            addItem()
        } catch (e: Exception) {

        }
        viewPager2.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (position == 0) {
                    runTransition(tabLayout, false)
                } else {
                    runTransition(tabLayout, true)
                }
            }

            override fun onPageSelected(position: Int) {

            }

        })
        add_item.setOnClickListener {
            if (viewPager2.currentItem == 3)
                rationFragment.openSheet()
            else
                addItems()
        }
    }
    private fun runTransition(view: ViewGroup, isVisible: Boolean) {
        TransitionManager.beginDelayedTransition(view, ChangeBounds())
        view.visibility = if (isVisible)
            View.VISIBLE
        else
            View.GONE
    }
    private fun updateChanges() {
        ref.child(user.uid).get().addOnCompleteListener {
            val res = it.result.value as MutableMap<*,*>
            if (res.keys.contains("ROOM_ID")){
                for (i in res){
                    if (i.key.toString()=="ROOM_ID"){
                        ref.child(user.uid).child("ROOM_ID1").setValue(i.value)
                        ref.child(user.uid).child("ROOM_ID").removeValue()
                    }
                }
                addItem()
            }
        }
    }

    private fun addItem() {
        map = HashMap()
        user_ref.get().addOnCompleteListener { task: Task<DataSnapshot> ->
            if (task.isSuccessful) {
                for (ds in task.result!!.children) (map as HashMap<String?, String>)[ds.key] =
                    ds.value.toString()
                key = (map as HashMap<String?, String>)[roomRef].toString()
                settingsStorage.room_id = key
                setFCM()
                user_name = (map as HashMap<String?, String>)["USER_NAME"].toString()
                "ID: $key".also { id_text.text = it }
                settingsStorage.username = user_name
                val query = db.collection(key + "_ALERT")
                    .whereEqualTo("IS_COMPLETED", false)
                query.addSnapshotListener { value: QuerySnapshot?, _: FirebaseFirestoreException? ->
                    materialCardView.visibility = View.GONE
                    if (value != null) {
                        for (ds in value) {
                            alert_cancel.setOnClickListener {
                                if (ds["UUID"].toString() == user.uid)
                                    db.collection(key + "_ALERT").document(ds.id)
                                        .update("IS_COMPLETED", true)
                                else
                                    materialCardView.visibility = View.GONE
                            }
                            if (DateUtils.isToday(ds["TIME_STAMP"].toString().toLong()))
                                materialCardView.visibility = View.VISIBLE
                            else
                                materialCardView.visibility = View.GONE
                            if (ds["UUID"].toString() == user.uid)
                                "You have announced ${ds["ALERT"]}".also {alert_text.text = it}
                            else
                                "${ds["BY"].toString()} has announced ${ds["ALERT"]}".also {alert_text.text = it}
                        }
                    }
                }
                setData()
                try {
                    ref.child("ROOM").child(key!!)
                        .child("START_DATE_MONTH").get()
                        .addOnCompleteListener { task: Task<DataSnapshot> ->
                            try {
                                if (task.isSuccessful) {
                                    val res = task.result
                                    Log.e("START", "${res.value}")
                                    settingsStorage.startDateMillis = res.value.toString().toLong()
                                }
                            } catch (e: Exception) {
                                Log.e("MAIN A ERROR", "$e")
                            }
                        }
                } catch (e: Exception) {
                    Log.e("MAIN A ERROR", "$e")
                }
            }
        }
    }


    private fun logOut() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        materialAlertDialogBuilder.setTitle("Do you want to logout?")
        materialAlertDialogBuilder.setNegativeButton("Cancel") { _, _ ->

        }
        materialAlertDialogBuilder.setPositiveButton("Logout") { _, _ ->
            mAuth.signOut()
            FirebaseMessaging.getInstance()
                .unsubscribeFromTopic("/topics/${settingsStorage.room_id.toString()}")
            settingsStorage.clear()

            supportFinishAfterTransition()
            goToRoomCreation(2)
        }
        materialAlertDialogBuilder.show()
    }

    private fun goToRoomCreation(int: Int) {
        val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        val intent = Intent(applicationContext, AccountCreation::class.java)
        intent.putExtra("CODE", int)
        startActivity(intent, bundle)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bottomappbar, menu)
        val person_search = menu.findItem(R.id.search)
        val logout_btn = menu.findItem(R.id.logout)
        val room_details = menu.findItem(R.id.info)
        val issue = menu.findItem(R.id.issue)
        issue.isVisible = false
        person_search.isVisible = false
        val alert = menu.findItem(R.id.alert)
        logout_btn.setOnMenuItemClickListener {
            logOut()
            false
        }
        person_search.setOnMenuItemClickListener { false }
        issue.setOnMenuItemClickListener {
            false
        }
        room_details.setOnMenuItemClickListener {
            val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            startActivity(Intent(applicationContext, RoomDetails::class.java), bundle)
            false
        }
        alert.setOnMenuItemClickListener {
            newAlert()
            false
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun addItems() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        val contactPopupView = layoutInflater.inflate(R.layout.popup, null)
        amount_paid = contactPopupView.findViewById(R.id.amount_paid)
        item_bought = contactPopupView.findViewById(R.id.item_bought)
        save_btn = contactPopupView.findViewById(R.id.save_btn)
        amount_layout = contactPopupView.findViewById(R.id.amount_layout)
        amount_paid.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                amount_layout.error =
                    if (!TextUtils.isDigitsOnly(charSequence)) "Only numbers are allowed" else ""
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        save_btn.setOnClickListener {
            if (item_bought.text.toString().isEmpty() || amount_paid.text.toString()
                    .isEmpty()
            ) showSnackBar("Enter all details") else {
                try {
                    if (!TextUtils.isDigitsOnly(amount_paid.text.toString())) {
                        amount_layout.error = "Only numbers are allowed"
                    } else {
                        saveItems(item_bought.text.toString(), amount_paid.text.toString())
                    }
                } catch (e: Exception) {
                }
            }
        }

        materialAlertDialogBuilder.setView(contactPopupView)
        materialAlertDialogBuilder.background = ColorDrawable(Color.TRANSPARENT)
        materialAlertDialogBuilder.show()
    }

    private fun saveItems(item: String?, amount: String) {
        val timeStamp = time
        val ts = System.currentTimeMillis()
        val map: MutableMap<String, Any?> = HashMap()
        val uid = user.uid
        map["UUID"] = uid
        map["TIME"] = timeStamp
        map["DATE"] = date
        map["TIME_STAMP"] = ts
        map["AMOUNT_PAID"] = amount.toInt()
        map["ITEM_BOUGHT"] = item
        map["BOUGHT_BY"] = user_name
        key?.let {
            db.collection(it).document(ts.toString()).set(map)
                .addOnFailureListener { e: Exception -> Log.e("ERROR", e.message!!) }
                .addOnSuccessListener {

                    sendNotify("New Buying", "$user_name has bought $item for $amount ")
                    showSnackBar("Item Saved")
                    updateThings()
                    item_bought.text = null
                    amount_paid.text = null
                }
        }
    }

    private fun showSnackBar(msg: String) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .show()
    }


    private fun newAlert() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        val contactPopupView = layoutInflater.inflate(R.layout.alert_popup, null)
        alert_et = contactPopupView.findViewById(R.id.alert_et)
        alert_btn = contactPopupView.findViewById(R.id.alert_btn)
        alert_btn.setOnClickListener { if (alert_et.text.toString() != "") createAlert(alert_et.text.toString()) }
        materialAlertDialogBuilder.setView(contactPopupView)
        materialAlertDialogBuilder.background = ColorDrawable(Color.TRANSPARENT)
        materialAlertDialogBuilder.show()
    }

    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.GONE
    }

    private fun setData() {
        room_mates = ArrayList()
        key?.let {
            ref.child("ROOM").child(it).child("ROOM_MATES")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
//                        val list = snapshot.value as List<Map<String, Any>>?
//                        val uid: ArrayList<Map<String, Any>> = ArrayList()
//                        for (i in list!!.indices) {
//                            val g: HashMap<String, Any> = HashMap()
//                            g["UUID"] = list[i]["UUID"].toString()
//                            g["USER_NAME"] = list[i]["USER_NAME"].toString()
//                            room_mates.add(g)
//                            uid.add(g)
//                        }
//                        progressBar.visibility = View.GONE
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun netStat() {
        connectivityManager = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
        } else {
            showSnackBar("No Internet")
        }
    }

//    private fun createIssue() {
//        val map1: MutableMap<String, String?> = HashMap()
//        val ts = System.currentTimeMillis()
//        val issue = msg_t.text.toString()
//        if (issue.isNotEmpty()) {
//            val person = user_name //issue_person.getText().toString();
//            val timeStamp = time
//            map1["ISSUE"] = issue
//            map1["DATE"] = date
//            map1["TIME"] = timeStamp
//            map1["TIME_STAMP"] = ts.toString()
//            map1["PERSON_TO"] = person
//            map1["PERSON_FROM"] = user_name
//            sendNotify(user_name, "New message\n$issue")
//            db.collection(key + "_ISSUES")
//                .add(map1)
//                .addOnSuccessListener { documentReference: DocumentReference ->
//
//                    msg_t.text = null
////                    Log.e("ISSUE_CREATED", documentReference.id)
//                }
//        }
//    }

    private fun createAlert(text: String?) {
        val time = time
        val ts = System.currentTimeMillis()
        val map1: MutableMap<String, Any?> = HashMap()
        map1["ALERT"] = text
        map1["DATE"] = date
        map1["UUID"] = user.uid
        map1["TIME"] = time
        map1["TIME_STAMP"] = ts
        map1["BY"] = user_name
        map1["IS_COMPLETED"] = false
        if (text != null) {
            sendNotify("Alert", text)
        }
        db.collection(key + "_ALERT")
            .add(map1)
            .addOnSuccessListener {
                showSnackBar("Alert created")

            }
    }

    val time: String
        get() {
            val date = Date()
            val sdm = SimpleDateFormat(TIME_STRING, Locale.getDefault())
            return sdm.format(date)
        }
    val date: String
        get() {
            val date = Date()
            val sdm = SimpleDateFormat(DATE_STRING, Locale.getDefault())
            return sdm.format(date)
        }

    private fun sendNotify(title: String, msg: String) {
        val notification = JSONObject()
        val notifyBody = JSONObject()
        try {
            notifyBody.put("title", title)
            notifyBody.put("message", msg)
            notifyBody.put("uid", uuid.toString())
            notifyBody.put("time", time)
//            Log.e("NOTIFY_UID", uuid.toString())
            notification.put("to", "/topics/${settingsStorage.room_id}")
            notification.put("data", notifyBody)
        } catch (e: Exception) {

        }
        NotificationSender().sendNotification(notification, applicationContext)
        //sendNotification(notification)
    }

    fun JSONObject.toMap(): Map<String, *> = keys().asSequence().associateWith {
        when (val value = this[it]) {
            is JSONArray -> {
                val map = (0 until value.length()).associate { Pair(it.toString(), value[it]) }
                JSONObject(map).toMap().values.toList()
            }
            is JSONObject -> value.toMap()
            JSONObject.NULL -> null
            else -> value
        }
    }

    private fun updateThings() {
        val key = key.toString()
        ref.child("ROOM").child(key).child("LAST_UPDATED").setValue(date)
    }

    override fun goToMain(position: Int, uuid: String) {
//        Log.e("FROM_MAIN", uuid)
        viewPager2.setCurrentItem(1, true)
        diffUser.goToUser(position, uuid)
    }


}

