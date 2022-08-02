package com.subhamgupta.roomiesapp.data.repositories


import android.app.Application
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.futured.donut.DonutSection
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.subhamgupta.roomiesapp.utils.SettingDataStore
import com.subhamgupta.roomiesapp.domain.model.*
import com.subhamgupta.roomiesapp.domain.use_case.GetUserUseCase
import com.subhamgupta.roomiesapp.utils.Constant.Companion.DATE_STRING
import com.subhamgupta.roomiesapp.utils.Constant.Companion.TIME_STRING
import com.subhamgupta.roomiesapp.utils.FirebaseService
import com.subhamgupta.roomiesapp.utils.FirebaseState
import com.subhamgupta.roomiesapp.utils.NotificationSender
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@Singleton
class MainRepository @Inject constructor(
    private val storage: FirebaseStorage,
    private val databaseReference: DatabaseReference,
    private val db: FirebaseFirestore,
    val auth: FirebaseAuth,
    private val dataStore: SettingDataStore,
    private val application: Application,
) {
    private var roomKey: String = ""

//    val ff = db.useEmulator("10.0.2.2", 8080)
//    val a = auth.useEmulator("10.0.2.2", 9099)
//
//    @Inject
//    lateinit var database:FirebaseDatabase
//    val d = database.useEmulator("10.0.2.2", 9000)

//    val fg = AppModule.firebaseStorage.useEmulator("10.0.2.2", 9199)

    private val updatedOn = MutableLiveData<String>()
    private val allRoomData = MutableLiveData<MutableList<RoomDetail>>()
    private val details = MutableLiveData<List<Detail?>>()
    private val totalAmount = MutableLiveData<Int>()
    private val todayAmount = MutableLiveData<Int>()

    private val _diffData = MutableLiveData<MutableList<MutableMap<String, Any>>>()
    val diffData: LiveData<MutableList<MutableMap<String, Any>>> = _diffData

    private val roomMap = MutableLiveData<MutableMap<String, String>>()
    private val tempRoomMap = MutableLiveData<MutableMap<String, String>>()
    private val roomIDToRef = HashMap<String, String>()
    private val uuidLink = mutableMapOf<String, Int>()
    private val roomMates = MutableLiveData<ArrayList<ROOMMATES>?>()
    private var userName: String = ""


    var user: FirebaseUser? = auth.currentUser
    var uuid: String? = user?.uid
    private var query: Query? = null

    init {
        Log.i("USER-", "${user?.email}")
    }

    fun getRoomMaps(): MutableLiveData<MutableMap<String, String>> {
        return roomMap
    }

    fun getUuidLink(): MutableMap<String, Int> {
        return uuidLink
    }

    fun getTotalAmount(): MutableLiveData<Int> {
        return totalAmount
    }

    fun getTodayAmount(): MutableLiveData<Int> {
        return todayAmount
    }

    fun getTempRoomMaps(): MutableLiveData<MutableMap<String, String>> {
        return tempRoomMap
    }


    fun getRoomMates() = roomMates


    fun sendNotify(title: String, msg: String) {
        val notification = JSONObject()
        val notifyBody = JSONObject()
        try {
            notifyBody.put("title", title)
            notifyBody.put("message", msg)
            notifyBody.put("uid", uuid.toString())
            notifyBody.put("time", time)
            notification.put("to", "/topics/${roomKey}")
            notification.put("data", notifyBody)
        } catch (e: Exception) {

        }
        NotificationSender().sendNotification(notification, application)
    }





    fun getAllRoomDetail(): MutableLiveData<MutableList<RoomDetail>> {
        return allRoomData
    }

    fun getDataStore() = dataStore

    suspend fun getUser(
        data: MutableStateFlow<MutableMap<String, Any>>,
        userDataLoading: MutableStateFlow<Boolean>
    ) {
        Log.e("Called","getUser")
        var userData = readUserDataFromRemote()
        data.value =
            if (!dataStore.isUpdate() && dataStore.isLoggedIn() && userData != null && userData.isNotEmpty()) {
                Log.e("USER DATA", "local")

                userData as MutableMap<String, Any>
            } else {
                userData = GetUserUseCase()(databaseReference, uuid!!)
                Log.e("USER DATA", "remote")
                saveUserToLocal(userData)
                userData
            }
        userDataLoading.value = false
    }

    private fun saveUserToLocal(
        userData: MutableMap<String, Any>
    ){
        try {
            val userFile = File(application.filesDir, "/user.json")
            if (!userFile.exists())
                userFile.createNewFile()
            userFile.writeText(Gson().toJson(userData))

        } catch (e: Exception) {
            Log.e("ERROR", "Saving user data ${e.message}")
        }
    }

    private fun saveDataToRemote(
        map: MutableMap<String, RoomDetail?>,
    ) {
        try {
            val file = File(application.filesDir, "/data.json")
            if (!file.exists())
                file.createNewFile()
            file.writeText(Gson().toJson(map))
            Log.e("file save", "$map")
        } catch (e: Exception) {
            Log.e("SAVE DATA FILE ERROR", "$e")
        }
    }


     fun readDataFromRemote(): Map<*, *>? {
        return try {
            val file = File(application.filesDir, "/data.json")
            val json = file.readText(Charset.defaultCharset())
            val map = Gson().fromJson(json, Map::class.java)
//            Log.e("file", "$map \n $json")
            map
        } catch (e: Exception) {
//            dataStore.setUpdate(false)
            Log.e("READ ROOM FILE ERROR", "$e")
            null
        }
    }

     fun readUserDataFromRemote(): Map<*, *>? {
        return try {
            val file = File(application.filesDir, "/user.json")
            if(file.exists()){
                val json = file.readText(Charset.defaultCharset())
                val map = Gson().fromJson(json, Map::class.java)
                map
            }
            else
                null
        } catch (e: Exception) {

//            dataStore.setUpdate(false)
            Log.e("READ USER FILE ERROR", "$e")
            null
        }

    }


    suspend fun fetchUserRoomData(
        liveData: MutableStateFlow<MutableMap<String, Any>>,
        roomData: MutableStateFlow<FirebaseState<RoomDetail>>,
        roomDataLoading: MutableStateFlow<Boolean>
    ) {
        Log.e("IS", "ONLINE")
        Log.e("Called","fetchUserRoomData")
        roomData.value = FirebaseState.loading()
        val listOfRooms = ArrayList<String>()
        val roomMapWithId = mutableMapOf<String, RoomDetail?>()
        if (uuid != null) {
            //fetching user's data
            val userData = GetUserUseCase()(databaseReference, uuid!!)
            saveUserToLocal(userData)
            liveData.value = userData
            //fetching rooms data which user has joined
            for (i in userData) {
                if (i.key.contains("ROOM_ID")) {
                    listOfRooms.add(i.value.toString())
                    val result = suspendCoroutine<RoomDetail?> { cont ->
                        databaseReference.child("ROOM").child(i.value.toString()).get()
                            .addOnCompleteListener { it1 ->
                                val result = it1.result.getValue(RoomDetail::class.java)
                                cont.resumeWith(Result.success(result))
                            }
                    }
                    roomMapWithId[i.value.toString()] = result
                    // roomId to it's data
                }
            }
            dataStore.setUpdate(false)
            saveDataToRemote(roomMapWithId)
            update(userData, roomMapWithId, roomDataLoading)
            dataStore.setLoggedIn(true)
            try {
                roomData.value = FirebaseState.success(roomMapWithId[dataStore.getRoomKey()]!!)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


    private suspend fun update(
        userData: MutableMap<String, Any>,
        roomMapWithId: MutableMap<String, RoomDetail?>,
        roomDataLoading: MutableStateFlow<Boolean>
    ) {
        val roomMap = mutableMapOf<String, String>()
        val tempRoomMap = mutableMapOf<String, String>()
        val allRoom = ArrayList<RoomDetail>()

        for (i in userData) {
            if (i.key.contains("ROOM_ID")) {
                val room = roomMapWithId[i.value.toString()]
                val roomName = room?.ROOM_NAME.toString()
                roomIDToRef[i.value.toString()] = i.key
                val roomId = i.key
                roomMap[roomName] = roomId
                tempRoomMap[roomId] = roomName
                allRoom.add(room!!)
            }
        }
        this.roomMap.postValue(roomMap)
        this.tempRoomMap.postValue(tempRoomMap)
        this.allRoomData.postValue(allRoom)

        try {
            val key = userData[dataStore.getRoomRef()].toString()
            dataStore.setRoomKey(key)
            roomKey = key
            val d = roomMapWithId[roomKey]
            userName = userData["USER_NAME"].toString()
            dataStore.setRoomJoined(userData["IS_ROOM_JOINED"].toString().toBoolean())
            updatedOn.postValue(d?.LAST_UPDATED.toString())
            val sd = d?.START_DATE_MONTH ?: 0
            dataStore.setStartDate(sd.toString())
            dataStore.setUUID(userData["UUID"].toString())
            dataStore.setEmail(userData["USER_EMAIL"].toString())
            dataStore.setUserName(userName)
            dataStore.setRoomCount(roomMapWithId.size)

            val room = d?.ROOM_MATES
            if (room != null) {
                roomMates.postValue(room)
                dataStore.setRoomSize(room.size)
                roomDataLoading.value = false
                fetchDiffData(room)
            }
        } catch (e: Exception) {
            Log.e("Update ERROR", "${e.message}")
        }
    }

    suspend fun forceInit() {
        user = auth.currentUser
        uuid = user?.uid
    }

    suspend fun prepareRoom(
        userData: MutableStateFlow<MutableMap<String, Any>>,
        roomData: MutableStateFlow<FirebaseState<RoomDetail>>,
        roomDataLoading: MutableStateFlow<Boolean>
    ) = coroutineScope {
        roomData.value = FirebaseState.loading()
        val user = readUserDataFromRemote()
        val map = readDataFromRemote()
//        Log.e("prepare room","$user \n $map")
        if (!dataStore.isUpdate() && dataStore.isLoggedIn() && user != null && map != null && map.isNotEmpty()) {
            Log.e("IS", "OFFLINE")
            val roomMapWithId = mutableMapOf<String, RoomDetail?>()
            val us = user as MutableMap<String, Any>
            for (i in user) {
                if (i.key.contains("ROOM_ID")) {
                    val d = Gson().fromJson(
                        Gson().toJson(map[i.value.toString()]),
                        RoomDetail::class.java
                    )
                    roomMapWithId[i.value.toString()] = d
                }
            }
            update(us, roomMapWithId, roomDataLoading)
            val room = roomMapWithId[roomKey]
            try {
                roomData.value = FirebaseState.success(room!!)
            } catch (e: Exception) {

            }
        } else {
            fetchUserRoomData(userData, roomData, roomDataLoading)
        }
    }

    suspend fun signOut() {
        auth.signOut()
        dataStore.clear()
        user = null
        dataStore.setUpdate(false)
//        MyApp.instance.workManager.cancelAllWork()
        dataStore.setLoggedIn(false)
        clearStorage()
    }

    fun clearStorage() {
        File(application.filesDir, "/user.json").delete()
        File(application.filesDir, "/data.json").delete()
    }

    fun createAlert(text: String?) {
        val time = time
        val ts = System.currentTimeMillis()
        val map1: MutableMap<String, Any?> = HashMap()
        map1["ALERT"] = text
        map1["DATE"] = date
        map1["UUID"] = uuid
        map1["TIME"] = time
        map1["TIME_STAMP"] = ts
        map1["BY"] = userName
        map1["IS_COMPLETED"] = false
        if (text != null) {
            sendNotify("Alert", text)
        }
        db.collection(roomKey + "_ALERT")
            .add(map1)
            .addOnSuccessListener {


            }
    }

    fun fetchAlert(alert: MutableStateFlow<Alerts>) {
        val som =
            LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond * 1000

        db.collection(roomKey + "_ALERT")
            .orderBy("TIME_STAMP", Query.Direction.DESCENDING)
            .whereGreaterThanOrEqualTo("TIME_STAMP", som)
            .whereEqualTo("IS_COMPLETED", false)
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    val res = value.toObjects(Alerts::class.java)
                    try {
                        alert.value = res[0] ?: Alerts()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }
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

    fun updateItem(
        item: String?,
        amount: String,
        timeStamp: String,
        liveData: MutableLiveData<Boolean>,
        note: String,
        tags: List<String>,
        category: String
    ) {
//        Log.e("Update","$roomKey $item $amount $timeStamp")
        db.collection(roomKey).document(timeStamp).update("ITEM_BOUGHT", item)
        db.collection(roomKey).document(timeStamp).update("TAGS", tags)
        db.collection(roomKey).document(timeStamp).update("NOTE", note)
        db.collection(roomKey).document(timeStamp).update("CATEGORY", category)
        db.collection(roomKey).document(timeStamp).update("AMOUNT_PAID", amount.toInt())
            .addOnCompleteListener {
                liveData.postValue(true)
            }
    }

    fun addItem(
        item: String?,
        amount: String,
        liveData: MutableLiveData<Boolean>,
        note: String,
        tags: List<String>,
        category: String
    ) {
        val ts = System.currentTimeMillis()
        val detail = mutableMapOf(
            "UUID" to uuid,
            "TIME" to time,
            "DATE" to date,
            "TIME_STAMP" to ts,
            "AMOUNT_PAID" to amount.toLong(),
            "ITEM_BOUGHT" to item,
            "BOUGHT_BY" to userName,
            "TAGS" to tags,
            "CATEGORY" to category,
            "NOTE" to note
        )
        db.collection(roomKey).document(ts.toString()).set(detail)
            .addOnFailureListener {
                Toast.makeText(application, "Error occurred", Toast.LENGTH_LONG).show()
                liveData.postValue(false)
            }
            .addOnSuccessListener {
                liveData.postValue(true)
            }

    }


    suspend fun fetchHomeData(
        liveData: MutableStateFlow<FirebaseState<HomeData>>,
        loadingHome: MutableStateFlow<Boolean>,
    ) = coroutineScope {
        roomKey = dataStore.getRoomKey()
        loadingHome.value = true
        liveData.value = FirebaseState.loading()
        val startDate = dataStore.getStartDate()
        Log.e("Called","fetchHomeData")
        val roomSize = dataStore.getRoomSize()
        val som =
            if (startDate == "0") LocalDate.now().withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond * 1000
            else startDate.toLong()
        db.collection(roomKey).orderBy("TIME_STAMP", Query.Direction.DESCENDING)
            .whereGreaterThanOrEqualTo("TIME_STAMP", som)
            .addSnapshotListener { query, error ->
                if (error != null) {
                    liveData.value = FirebaseState.failed(error.message)
                    return@addSnapshotListener
                }
                if (query != null) {
                    var todayTotalAmount = 0
                    var monthTotalAmount = 0
                    val chartList = ArrayList<Int>()
                    val allUserAmount = ArrayList<MutableMap<String, String>>()
                    val todayDetail = ArrayList<Detail>()
                    val donutList = ArrayList<DonutSection>()
                    val userMap = mutableMapOf<String, HashMap<String, Any?>>()
                    val homeDetails = query.toObjects(Detail::class.java)

                    homeDetails.forEach { detail ->
                        if (DateUtils.isToday(detail.TIME_STAMP ?: 0)) {
                            todayTotalAmount += detail.AMOUNT_PAID.toInt()
                            todayDetail.add(detail)
                        }
                        monthTotalAmount += detail.AMOUNT_PAID.toInt()
                        chartList.add(detail.AMOUNT_PAID.toInt())
                        val uid = detail.UUID.toString()
                        if (userMap.containsKey(uid)) {
                            userMap[uid]?.set("USER", detail.BOUGHT_BY)
                            userMap[uid]?.set(
                                "AMOUNT",
                                userMap[uid]?.get("AMOUNT").toString().toInt()
                                    .plus(detail.AMOUNT_PAID)
                            )
                        } else {
                            userMap[uid] = mutableMapOf(
                                "USER" to detail.BOUGHT_BY,
                                "AMOUNT" to detail.AMOUNT_PAID
                            ) as HashMap<String, Any?>
                        }
                    }
                    for (i in userMap) {
                        val mj = i.value as HashMap<*, *>
                        val color = getColor(i.key)
                        val mp = mutableMapOf(
                            "USER_NAME" to mj["USER"].toString(),
                            "AMOUNT" to mj["AMOUNT"].toString(),
                            "UUID" to i.key,
                            "COLOR" to color.toString()
                        )
                        try {
                            val section1 = DonutSection(
                                name = mj["USER"].toString(),
                                color = color,
                                amount = mj["AMOUNT"].toString().toFloat()
                            )
                            donutList.add(section1)
                        } catch (e: Exception) {
                            Log.e("HOME_ERROR", "$e")
                        }
                        allUserAmount.add(mp)
                    }
                    val re = (monthTotalAmount.toDouble() / roomSize.toDouble())
                    val round = String.format("%.1f", re)

                    val updatedOn =
                        if (todayDetail.size != 0) todayDetail[0].TIME_STAMP else System.currentTimeMillis()
                    totalAmount.postValue(monthTotalAmount)
                    todayAmount.postValue(todayTotalAmount)
                    val home = HomeData(
                        todayTotalAmount,
                        monthTotalAmount,
                        round,
                        todayDetail,
                        donutList,
                        allUserAmount,
                        startDate,
                        updatedOn.toString(),
                        chartList,
                        homeDetails.isEmpty()
                    )
                    loadingHome.value = false
                    liveData.value = FirebaseState.success(home)
                }
            }
    }

    fun modifyStartDate(timeStamp: Long, liveData: MutableLiveData<Boolean>) {
        databaseReference.child("ROOM").child(roomKey).child("START_DATE_MONTH").setValue(timeStamp)
            .addOnCompleteListener {
                liveData.postValue(true)
            }
            .addOnFailureListener {
                liveData.postValue(false)
            }
    }

    suspend fun fetchDiffData(roomMates: ArrayList<ROOMMATES>) {
        roomKey = dataStore.getRoomKey()
        Log.e("Called","fetchDiffData")
        val detailMap = mutableListOf<MutableMap<String, Any>>()
        val rmp =
            if (roomMates.isEmpty())
                this.roomMates.value
            else
                roomMates
        val startDate = dataStore.getStartDate()
//        Log.e("DIFF DATA", "$roomKey $startDate")
        val som =
            if (startDate == "0") LocalDate.now().withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond * 1000
            else
                startDate.toLong()
        val query = db.collection(roomKey)
            .orderBy("TIME_STAMP", Query.Direction.DESCENDING)
            .whereGreaterThanOrEqualTo("TIME_STAMP", som)
        coroutineScope {
            var i = 0
            rmp?.forEach { roommates ->
                uuidLink[roommates.UUID.toString()] = i
                i++
                query.whereEqualTo("UUID", roommates.UUID.toString())
                    .addSnapshotListener { value, _ ->
                        if (value != null) {
                            val v = value.toObjects(Detail::class.java)
                            detailMap.add(
                                mutableMapOf(
                                    "UUID" to roommates.UUID.toString(),
                                    "DATA" to v
                                )
                            )
                            _diffData.postValue(detailMap)
                        }
                    }
            }
        }
    }

    private fun getColor(v: String): Int {
        return Color.parseColor(randColors.random())
    }

    private val randColors = arrayOf(
        "#FFB300", "#803E75", "#FF6800", "#A6BDD7",
        "#C10020", "#CEA262", "#817066", "#007D34",
        "#F6768E", "#00538A", "#FF7A5C", "#53377A",
        "#FF8E00", "#B32851", "#F4C800", "#7F180D",
        "#93AA00", "#593315", "#F13A13", "#232C16"
    )

    suspend fun generateExcel(
        loading: MutableStateFlow<FirebaseState<Boolean>>
    ) = coroutineScope {
        try {
            loading.value = FirebaseState.loading()
            val json = Gson().toJson(details.value)
            val jsonTree = ObjectMapper().readTree(json)
            val builder = CsvSchema.builder()
            val fst = jsonTree.elements().next()
            fst.fieldNames().forEachRemaining {
                builder.addColumn(it)
            }
            val csvSchema = builder.build().withHeader()
            val csvMapper = CsvMapper()
            csvMapper.writerFor(JsonNode::class.java)
                .with(csvSchema)
                .writeValue(File(application.filesDir, "sheet.csv"), jsonTree)
            loading.value = FirebaseState.success(true)
        } catch (e: Exception) {
            Log.e("SAVE DATA FILE ERROR", "$e")
        }
    }

    suspend fun uploadPic(
        uri: Uri,
        userName: String,
        editUser: MutableStateFlow<FirebaseState<Boolean>>
    ) =
        coroutineScope {
            editUser.value = FirebaseState.loading()
            val ref = storage.getReference(uuid!! + "/profile_pic.jpg")
            ref.putFile(uri)
                .addOnCompleteListener {
                    if (it.isComplete) {
                        it.result.storage.downloadUrl.addOnCompleteListener {
                            databaseReference.child(uuid!!).child("USER_NAME").setValue(userName)
                            databaseReference.child(uuid!!).child("IMG_URL")
                                .setValue(it.result.toString())
                            editUser.value = FirebaseState.success(true)
                        }
                    }
                }
                .addOnFailureListener {
                    editUser.value = FirebaseState.failed(it.message)
                }
        }

    suspend fun fetchSummary(
        isMonth: Boolean,
        category: String,
        state: MutableStateFlow<FirebaseState<List<Detail?>>>
    ) = coroutineScope {
        roomKey = dataStore.getRoomKey()
        var sdom = 0L
        Log.e("Called","fetchSummary")
        query = db.collection(roomKey).orderBy("TIME_STAMP", Query.Direction.DESCENDING)
        val startDate = dataStore.getStartDate()
        if (isMonth) {
            sdom = if (startDate == "0") LocalDate.now().withDayOfMonth(1)
                .atStartOfDay(
                    ZoneId.systemDefault()
                ).toInstant().epochSecond * 1000
            else startDate.toLong()
        }
        state.value = FirebaseState.loading()
        var query = db.collection(roomKey).orderBy("TIME_STAMP", Query.Direction.DESCENDING)
            .whereGreaterThanOrEqualTo("TIME_STAMP", sdom)
        if (category != "All")
            query = query.whereEqualTo("CATEGORY", category)
        query.addSnapshotListener { value, error ->
            if (error != null) {
                state.value = FirebaseState.failed(error.message)
                Log.e("SUMMARY ERROR", "$error")
            }
            if (value == null) {
//                if (value?.documents == null)
//                    state.value = FirebaseState.empty()

            } else {
                value.documents.let {
                    if (it.isEmpty()) {
                        state.value = FirebaseState.empty()
                    } else {
                        val docs = it.map {
                            it.toObject(Detail::class.java)
                        }
                        details.postValue(docs)
                        state.value = FirebaseState.success(docs)
                    }
                }
            }
        }
    }

    suspend fun leaveRoom(leaveRoom: MutableLiveData<Boolean>, key: String) = coroutineScope {
        val ref = roomIDToRef[key]
        roomIDToRef.remove(key)
        if (ref != null) {
            val res = suspendCoroutine<Task<Void>> { con ->
                databaseReference.child(uuid!!).child(ref).removeValue().addOnCompleteListener {
                    con.resume(it)
                }
            }
            if (res.isSuccessful) {
                val r = roomIDToRef.keys
                roomIDToRef[r.first()]?.let { dataStore.setRoomRef(it) }
            }
            leaveRoom.postValue(res.isSuccessful)
        }
    }

}