package com.subhamgupta.roomiesapp.data.repositories


//import app.futured.donut.DonutSection
import android.app.Application
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.subhamgupta.roomiesapp.data.AndroidAlarmScheduler
import com.subhamgupta.roomiesapp.domain.model.*
import com.subhamgupta.roomiesapp.utils.Constant.Companion.DATE_STRING
import com.subhamgupta.roomiesapp.utils.Constant.Companion.TIME_STRING
import com.subhamgupta.roomiesapp.utils.FirebaseState
import com.subhamgupta.roomiesapp.utils.NotificationSender
import com.subhamgupta.roomiesapp.utils.SettingDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.time.*
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs
import kotlin.system.measureTimeMillis


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
    private val roomMates = MutableStateFlow<MutableList<ROOMMATES>?>(mutableListOf())
    private var userName: String = ""
    private var deviceId: String = ""

    private val userData = MutableStateFlow<MutableMap<String, Any>>(mutableMapOf())

    var user: FirebaseUser? = auth.currentUser
    var uuid: String? = user?.uid
    private var query: Query? = null

    init {
        deviceId =
            Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
        Log.e("ID Email uuid", "$uuid")
//        MyApp.uuid = uuid!!
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
            notification.put("to", "/topics/$roomKey")
            notification.put("data", notifyBody)
        } catch (e: Exception) {
            Log.e("TAG", "sendNotify: ", e)
        }
        NotificationSender().sendNotification(notification, application)
    }


    fun getAllRoomDetail(): MutableLiveData<MutableList<RoomDetail>> {
        return allRoomData
    }

    fun getDataStore() = dataStore

    suspend fun getUser(
        data: MutableStateFlow<MutableMap<String, Any>>,
        userDataLoading: MutableStateFlow<Boolean>,
        accountFeature: MutableStateFlow<MutableMap<String, Boolean>>
    ) {
        Log.e("getUser: ", "fired ${data.value} ${userDataLoading.value}")
        var flag = true
        fun setFeature(u: MutableMap<String, Any>) {
            val feature = mutableMapOf<String, Boolean>()
            u.forEach {
                if (it.key.contains("FEAT")) {
                    feature[it.key] = it.value as Boolean
                }
            }
            accountFeature.value = feature
        }

        val offlineUserData = readUserDataFromRemote()
        if (!dataStore.isUpdate() && dataStore.isLoggedIn() && offlineUserData != null && offlineUserData.isNotEmpty()) {
            val u = offlineUserData as MutableMap<String, Any>
            data.value = u
            Log.e("getUser: OFFLINE", "$user")
            flag = false
            setFeature(u)
            userDataLoading.value = false
        }
        coroutineScope {
            uuid = auth.currentUser?.uid
            databaseReference.child(uuid!!).addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val user = dataSnapshot.value as MutableMap<String, Any>
                            val onlineUserData = user
                            data.value = onlineUserData
                            saveUserToLocal(onlineUserData)
                            setFeature(onlineUserData)
                            userData.value = onlineUserData
                            Log.e("USER DATA cloud", "$user")
                            if (flag) {
                                userDataLoading.value = false
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                }
            )
        }

//        dataStore.setDeviceId(deviceId)

    }

    private fun saveUserToLocal(
        userData: MutableMap<String, Any>
    ) {
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
            if (file.exists()) {
                val json = file.readText(Charset.defaultCharset())
                val map = Gson().fromJson(json, Map::class.java)
                map
            } else
                null
        } catch (e: Exception) {

//            dataStore.setUpdate(false)
            Log.e("READ USER FILE ERROR", "$e")
            null
        }

    }


    suspend fun fetchUserRoomData(
        userData: MutableStateFlow<MutableMap<String, Any>>,
        roomData: MutableStateFlow<FirebaseState<RoomDetail>>,
        roomDataLoading: MutableStateFlow<Boolean>
    ) {

        Log.e("fetchUserRoomData", "ONLINE")

        roomData.value = FirebaseState.loading()
        val listOfRooms = ArrayList<String>()
        val roomMapWithId = mutableMapOf<String, RoomDetail?>()
        if (uuid != null) {
            for (i in userData.value) {
                if (i.key.contains("ROOM_ID")) {
                    listOfRooms.add(i.value.toString())
                    val result = suspendCancellableCoroutine { cont ->
                        databaseReference.child("ROOM").child(i.value.toString()).get()
                            .addOnCompleteListener { it1 ->
                                val result = it1.result?.getValue(RoomDetail::class.java)
                                cont.resumeWith(Result.success(result))
                            }
                            .addOnFailureListener {
                                Log.e("fetchUserRoomData", "$it")
                                cont.resumeWith(Result.failure(it))
                            }
                    }
                    Log.e("fetchUserRoomData", "$result")
                    roomMapWithId[i.value.toString()] = result
                    // roomId to it's data
                } else
                    continue
            }
            dataStore.setUpdate(false)
            saveDataToRemote(roomMapWithId)
            supervisorScope {
                launch {
                    update(userData.value, roomMapWithId, roomDataLoading)
                    dataStore.setLoggedIn(true)
                }
                launch {
                    try {
                        var room = roomMapWithId[roomMapWithId.keys.first()]
                        if (roomMapWithId[dataStore.getRoomKey()] != null)
                            room = roomMapWithId[dataStore.getRoomKey()]
                        Log.e("room vs id map", "$room")
                        if (roomMapWithId.isNotEmpty())
                            roomData.value = FirebaseState.success(room!!)
                    } catch (e: Exception) {
                        Log.e("fetchUserRoomData error", "${e.message}")
                    }
                }
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

        val d = userData.filter { it.key.contains("ROOM_ID") }
        d.forEach {
            try {
                val room = roomMapWithId[it.value.toString()]
                val roomName = room?.ROOM_NAME.toString()
                roomIDToRef[it.value.toString()] = it.key
                val roomId = it.key
                roomMap[roomName] = roomId
                tempRoomMap[roomId] = roomName
                allRoom.add(room!!)
            } catch (e: Exception) {
                Log.e("update :: error", "$e")
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
                roomMates.value = room
                dataStore.setRoomSize(room.size)
                roomDataLoading.value = false
                Log.e("update: room", "$room")
            } else {
                Log.e("update: else", "empty")
            }
        } catch (e: Exception) {
            Log.e("Update ERROR update", "${e.message}")
        }
    }

    suspend fun forceInit() {
        user = auth.currentUser
        uuid = user?.uid
    }

    private var room: RoomDetail? = null
    suspend fun prepareRoom(
        userData: MutableStateFlow<MutableMap<String, Any>>,
        roomData: MutableStateFlow<FirebaseState<RoomDetail>>,
        roomDataLoading: MutableStateFlow<Boolean>
    ) = coroutineScope {
        roomData.value = FirebaseState.loading()
        val user = userData.value
        val roomDataLocal = readDataFromRemote()
        Log.e("prepareRoom: ", "$user \n $roomDataLocal")
        if (!dataStore.isUpdate() && dataStore.isLoggedIn() && roomDataLocal != null && roomDataLocal.isNotEmpty()) {
            Log.e("IS", "OFFLINE")
            val roomMapWithId = mutableMapOf<String, RoomDetail?>()
            for (i in user) {
                if (i.key.contains("ROOM_ID")) {
                    val d = Gson().fromJson(
                        Gson().toJson(roomDataLocal[i.value.toString()]),
                        RoomDetail::class.java
                    )
                    roomMapWithId[i.value.toString()] = d
                }
            }

            update(user, roomMapWithId, roomDataLoading)

            try {
                room = roomMapWithId[roomKey]!!
//                migrate()
                roomData.value = FirebaseState.success(room!!)
            } catch (e: Exception) {
                roomData.value = FirebaseState.failed("Something went wrong")
                Log.e("prepareRoom", "${e.message}")
            }
        } else {
            fetchUserRoomData(userData, roomData, roomDataLoading)
        }
    }

    suspend fun getUsersRoom(
        userData: MutableStateFlow<MutableMap<String, Any>>,
        roomDataLoading: MutableStateFlow<Boolean>
    ) {
        val user = userData.value
        val uuid = user["UUID"].toString()
        val time = measureTimeMillis {
            db.collection(uuid + "_MAP").get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val result = it.result?.toObjects(MapUserRoom::class.java)
                        Log.e("rooms", "$result")
                    }
                }
        }
        Log.e("rooms", "$time")


    }

    suspend fun signOut() {
        auth.signOut()
        dataStore.clear()
        user = null
        dataStore.setUpdate(false)
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

        val query = db.collection(roomKey + "_ALERT")
            .whereGreaterThanOrEqualTo("TIME_STAMP", som)
            .orderBy("TIME_STAMP", Query.Direction.DESCENDING)

//            .whereEqualTo("IS_COMPLETED", false)
        query.addSnapshotListener { value, _ ->
            if (value != null && !value.isEmpty) {
                val res = value.toObjects(Alerts::class.java)
                try {
                    alert.value = res[0] ?: Alerts()
                    Log.e("alert $roomKey", "$res")
                } catch (e: Exception) {
                    Log.e("ERROR REPO", "${e.message}")
                }
            }
        }
    }


    private val time: String
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
        liveData: MutableStateFlow<Boolean>,
        note: String,
        tags: List<String>,
        category: String
    ) {
//        Log.e("Update","$roomKey $item $amount $timeStamp")
        db.collection(roomKey).document(timeStamp).update("ITEM_BOUGHT", item)
        db.collection(roomKey).document(timeStamp).update("DELETED", false)
        db.collection(roomKey).document(timeStamp).update("TAGS", tags)
        db.collection(roomKey).document(timeStamp).update("NOTE", note)
        db.collection(roomKey).document(timeStamp).update("CATEGORY", category)
        db.collection(roomKey).document(timeStamp).update("AMOUNT_PAID", amount.toInt())
            .addOnCompleteListener {
                liveData.value = true
            }
        db.collection(roomKey + "_BACKUP").document(timeStamp).update("ITEM_BOUGHT", item)
        db.collection(roomKey + "_BACKUP").document(timeStamp).update("DELETED", false)
        db.collection(roomKey + "_BACKUP").document(timeStamp).update("TAGS", tags)
        db.collection(roomKey + "_BACKUP").document(timeStamp).update("NOTE", note)
        db.collection(roomKey + "_BACKUP").document(timeStamp).update("CATEGORY", category)
        db.collection(roomKey + "_BACKUP").document(timeStamp).update("AMOUNT_PAID", amount.toInt())
            .addOnCompleteListener {
            }

    }

    fun addItem(
        item: String?,
        amount: String,
        liveData: MutableStateFlow<Boolean>,
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
            "NOTE" to note,
            "DELETED" to false
        )
        db.collection(roomKey).document(ts.toString()).set(detail)
            .addOnFailureListener {
                Toast.makeText(application, "Error occurred", Toast.LENGTH_LONG).show()
                liveData.value = false
            }
            .addOnSuccessListener {
                liveData.value = true

            }
        db.collection(roomKey + "_BACKUP").document(ts.toString()).set(detail)
            .addOnFailureListener {

            }
            .addOnSuccessListener {

            }

    }


    suspend fun generateHomeData(
        liveData: MutableStateFlow<FirebaseState<HomeData>>,
        loadingHome: MutableStateFlow<Boolean>,
        homeUser: MutableStateFlow<HomeUserMap>,
        homeDetail: MutableStateFlow<List<Detail>>,
        homeDonut: MutableStateFlow<List<String>>,
        migrateCheck: MutableStateFlow<Int>,
        totalData: MutableStateFlow<List<Detail>>,
    ) = coroutineScope {
        liveData.value = FirebaseState.loading()
        roomKey = dataStore.getRoomKey()
        val startDate = dataStore.getStartDate()
        val roomSize = dataStore.getRoomSize()
        val som =
            if (startDate == "0") LocalDate.now().withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond * 1000
            else startDate.toLong()
        db.collection(roomKey).orderBy("TIME_STAMP", Query.Direction.DESCENDING)
            .whereGreaterThanOrEqualTo("TIME_STAMP", som)
            .addSnapshotListener { query, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (query != null) {
                    var todayTotalAmount = 0
                    var monthTotalAmount = 0
                    val chartList = ArrayList<Int>()
                    val allUserAmount = ArrayList<MutableMap<String, String>>()
                    val todayDetail = ArrayList<Detail>()
//                    val donutList = ArrayList<DonutSection>()
                    val userMap = mutableMapOf<String, HashMap<String, Any?>>()
                    val homeDetails = query.toObjects(Detail::class.java).filter { !it.DELETED }
                    totalData.value = homeDetails
                    migrateCheck.value = homeDetails.size
                    chartList.add(1)
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
//                            val section1 = DonutSection(
//                                name = mj["USER"].toString(),
//                                color = color,
//                                amount = mj["AMOUNT"].toString().toFloat()
//                            )
//                            donutList.add(section1)
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
                        todayTotal = todayTotalAmount,
                        allTotal = monthTotalAmount,
                        startDate = startDate,
                        updatedOn = updatedOn.toString(),
                        chartData = chartList,
                        isEmpty = homeDetails.isEmpty(),
                        count = homeDetails.size,
                        roomSize = roomSize,
                        eachPersonAmount = round
                    )


                    val homeUserMap = HomeUserMap(round, allUserAmount)
//                    saveHomeData(
//                        home,
//                        donutList,
//                        todayDetail,
//                        homeUserMap,
//                        monthTotalAmount,
//                        todayTotalAmount
//                    )

                    liveData.value = FirebaseState.success(home)
//                    homeDonut.value = donutList
                    homeDetail.value = todayDetail
                    homeUser.value = homeUserMap
                    loadingHome.value = false
                    Log.e("home", "end")

                }
            }
    }

    suspend fun monthlyDataCal(
        homeDetails: MutableStateFlow<List<Detail>>,
        monthlyData: MutableStateFlow<LinkedList<MutableMap<String, Any>>>,
        isMine: Boolean
    ) {
        homeDetails.collect {
            val monthVsStamp = LinkedHashMap<Long, Month>()
            val out = if (isMine) {
                it.filter { it.UUID == uuid }
            } else it
            val group = out.parallelStream()
                .collect(Collectors.groupingBy {
                    val calendar = Calendar.Builder().setInstant(it.TIME_STAMP!!)
                        .setLocale(Locale.getDefault()).build()
                    calendar.set(
                        Calendar.DAY_OF_MONTH,
                        calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    );
                    calendar.time
                    val localDate =
                        LocalDate.of(
                            calendar[Calendar.YEAR],
                            calendar[Calendar.MONTH] + 1,
                            1
                        )
                    val timeStamp =
                        localDate.atStartOfDay(ZoneId.systemDefault())
                            .toInstant().epochSecond
                    monthVsStamp[timeStamp] = localDate.month
                    timeStamp
                }).toSortedMap(compareByDescending { it })
            val result = LinkedList<MutableMap<String, Any>>()
            group.forEach {
                val amount = it.value.sumOf { d -> d.AMOUNT_PAID }
                result.add(
                    mutableMapOf(
                        "AMOUNT" to amount,
                        "MONTH" to monthVsStamp[it.key].toString()
                    )
                )
            }
//                        val s = soundex("dd")
            Log.e("group start", "$result")
            monthlyData.value = result
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

    suspend fun migrate() = coroutineScope {
        val details = ArrayList<Any>()
        val uuidSet = ArrayList<String>()
        Log.e("migrate", "$room")
        if (room != null) {
            val roomUser = room!!.ROOM_MATES
            roomUser.forEach { mate ->
                val uuid = mate.UUID
                uuidSet.add(uuid!!)
            }
            Log.e("migrate", "$uuidSet")
            val query = db.collection(roomKey).whereEqualTo("DELETED", false)
            val result = suspendCoroutine { cont ->
                query.get().addOnCompleteListener { value ->
                    if (value.isSuccessful) {
                        val res = value.result?.toObjects(Detail::class.java)
                        cont.resume(res)
                    }
                }
            }
            result?.let {
                if (it.isEmpty()) {
                    val map = mutableMapOf<String, Long>()
                    uuidSet.forEach { uuid ->
                        val amount = result.filter { it!!.UUID == uuid }.sumOf { it.AMOUNT_PAID }
                        map[uuid] = amount
                    }
                    Log.e("migrate", "$map")
                    map.forEach { m ->
                        val user = roomUser.filter { it.UUID == m.key }[0]
                        val detail = mutableMapOf(
                            "UUID" to m.key,
                            "TIME" to time,
                            "DATE" to date,
                            "TIME_STAMP" to System.currentTimeMillis(),
                            "AMOUNT_PAID" to m.value,
                            "ITEM_BOUGHT" to "Migration",
                            "BOUGHT_BY" to user.USER_NAME,
                            "TAGS" to emptyList<String>(),
                            "CATEGORY" to "All",
                            "NOTE" to "Migration",
                            "DELETED" to false
                        )
                        delay(200)
                        details.add(detail)
                    }
                    Log.e("migrate", "$details")

                    result.forEach { detail ->
                        val migratedCreate = suspendCoroutine { cont ->
                            db.collection(roomKey + "_MIGRATED")
                                .document(detail.TIME_STAMP.toString())
                                .set(detail)
                                .addOnCompleteListener {
                                    Log.e("migrate", "${it.isSuccessful}")
                                    cont.resume(true)
                                }.addOnFailureListener {
                                    Log.e("migrate error", "${it.message}")
                                }
                        }
                        Log.e("Migrate", "$detail")
                        if (migratedCreate) {
                            suspendCoroutine { con ->
                                db.collection(roomKey)
                                    .document(detail.TIME_STAMP.toString())
                                    .update("DELETED", true)
                                    .addOnCompleteListener {
                                        con.resume(Unit)
                                    }
                            }
                        }
                    }
                    Log.e("migrate done", "setting")
                    details.parallelStream().forEach {
                        val tempMap = it as Map<String, Any>
                        Log.e("migrate", "$tempMap")
                        db.collection(roomKey)
                            .document(tempMap["TIME_STAMP"].toString())
                            .set(tempMap)
                            .addOnCompleteListener {
                                Log.e("migrate", "completed $tempMap")
                            }
                    }
                }
            }
        }
    }

    suspend fun fetchDiffData(
        data: List<Detail?>,
        _diffUserData: MutableStateFlow<MutableList<MutableMap<String, Any>>>
    ) = coroutineScope {
        val result = mutableListOf<MutableMap<String, Any>>()
        val uuidSet = ArrayList<String>()

        try {
            Log.e("ROOM2", "$room")
            val roomUser = room!!.ROOM_MATES
            roomUser.forEach { mate ->
                val uuid = mate.UUID
                uuidSet.add(uuid!!)
            }
            Log.e("ROOM2", "$uuidSet")

            uuidSet.forEach { uuid ->
                val map = mutableMapOf<String, Any>()
                map["UUID"] = uuid
                val list = data.filter { it!!.UUID == uuid }
                list.sortedBy { it!!.TIME_STAMP }
                map["DATA"] = list
                result.add(map)
            }
            _diffUserData.value = result
        } catch (e: Exception) {
            Log.e("fetchDiffData error", "$e")
        }
    }


    @Deprecated("new version made")
    suspend fun fetchDiffData(roomMates: ArrayList<ROOMMATES>) {
        roomKey = dataStore.getRoomKey()
        val detailMap = mutableListOf<MutableMap<String, Any>>()
        val rmp =
            if (roomMates.isEmpty())
                this.roomMates.value
            else
                roomMates
        val startDate = dataStore.getStartDate()
        Log.e("DIFF DATA", "$roomKey $startDate")
        val som =
            if (startDate == "0") LocalDate.now().withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond * 1000
            else
                startDate.toLong()
        val query = db.collection(roomKey)
            .orderBy("TIME_STAMP", Query.Direction.DESCENDING)
            .whereGreaterThanOrEqualTo("TIME_STAMP", som)

        supervisorScope {
            var i = 0

            rmp?.forEach { roommates ->
                uuidLink[roommates.UUID.toString()] = i
                i++
                val job = suspendCoroutine<List<Detail>> {
                    query.whereEqualTo("UUID", roommates.UUID.toString())
                        .addSnapshotListener { value, _ ->
                            if (value != null) {
                                try {
                                    val v = value.toObjects(Detail::class.java)
                                    it.resume(v)
                                } catch (e: Exception) {
                                    Log.e("fetchDiffData", "${e.message}")
                                }
                            }
                        }
                }
                detailMap.add(
                    mutableMapOf(
                        "UUID" to roommates.UUID.toString(),
                        "DATA" to job
                    )
                )
            }
            Log.e("fetchDiffData", "$detailMap")
//            _diffData.postValue(detailMap)
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
            Log.e("generateExcel: ", "$jsonTree")
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
    ) = coroutineScope {
        editUser.value = FirebaseState.loading()
        val ref = storage.getReference(uuid!! + "/profile_pic.jpg")
        ref.putFile(uri)
            .addOnCompleteListener { it ->
                if (it.isComplete) {
                    it.result?.storage?.downloadUrl?.addOnCompleteListener {
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
        state: MutableStateFlow<FirebaseState<List<Detail?>>>,
        summaryDataLoading: MutableStateFlow<Boolean>
    ) = coroutineScope {
        roomKey = dataStore.getRoomKey()
        val pageSize = 50L
        var sdom = 0L
        query = db.collection(roomKey).orderBy("TIME_STAMP", Query.Direction.DESCENDING)
        val startDate = dataStore.getStartDate()
        sdom = if (isMonth) {
            if (startDate == "0") LocalDate.now().withDayOfMonth(1)
                .atStartOfDay(
                    ZoneId.systemDefault()
                ).toInstant().epochSecond * 1000
            else startDate.toLong()
        } else {
            LocalDate.now().withDayOfMonth(1)
                .atStartOfDay(
                    ZoneId.systemDefault()
                ).toInstant().epochSecond * 1000
        }
        state.value = FirebaseState.loading()
        var query = db.collection(roomKey).orderBy("TIME_STAMP", Query.Direction.DESCENDING)
            .whereGreaterThanOrEqualTo("TIME_STAMP", sdom).limit(pageSize)
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
                value.documents.let { snapshots ->
                    if (snapshots.isEmpty()) {
                        state.value = FirebaseState.empty()
                    } else {
                        val docs = snapshots.map {
                            it.toObject(Detail::class.java)
                        }.filter { it?.DELETED == false }
                        details.postValue(docs)
                        state.value = FirebaseState.success(docs)
                        summaryDataLoading.value = true
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

    suspend fun getStoredItemCards(
        data: MutableStateFlow<FirebaseState<MutableList<MutableMap<String, Any>>>>
    ) = coroutineScope {
        val result = ArrayList<MutableMap<String, Any>>()
        val query: Query = db.collection(roomKey + "_RATION")
        query.orderBy("TIME_STAMP", Query.Direction.DESCENDING)
            .addSnapshotListener { value, _ ->
                data.value = FirebaseState.loading()
                if (value != null && !value.isEmpty) {
                    for (ds in value) {
                        val d = ds.data as MutableMap<String, Any>
                        result.add(d)
                        Log.e("getStoredItemCards: ", "$d")
                    }
                    data.value = FirebaseState.success(result)
                } else {
                    data.value = FirebaseState.empty()
                }

            }

    }

    fun uploadCard(
        data: MutableStateFlow<FirebaseState<Boolean>>,
        date: String,
        note: String,
        byt: ByteArray
    ) {
        val n = note.ifEmpty { "" }
        val ts = System.currentTimeMillis()
        val ref = storage.getReference("ration_images/$roomKey")
        val task = ref.child("$ts.jpg").putBytes(byt)
        data.value = FirebaseState.loading()
        task.addOnCompleteListener { t ->
            if (t.isSuccessful)
                ref.child("$ts.jpg").downloadUrl.addOnSuccessListener {
                    val map = HashMap<String, Any>()
                    println(Uri.parse(it.toString()))
                    map["DATE"] = date
                    map["NOTE"] = n
                    map["UUID"] = uuid.toString()
                    map["TIME_STAMP"] = ts
                    map["IMG_NAME"] = "$ts.jpg"
                    map["IMG_URL"] = Uri.parse(it.toString()).toString()
                    db.collection(roomKey + "_RATION").document(ts.toString()).set(map)
                        .addOnCompleteListener {
                            data.value = FirebaseState.success(true)
                        }.addOnFailureListener {
                            Log.e("uploadCard: ", "${it.message}")
                        }
                }.addOnFailureListener {
                    Log.e("uploadCard: ", "${it.message}")
                }


        }.addOnProgressListener {
            val p: Double =
                100.0 * it.bytesTransferred / it.totalByteCount

        }.addOnFailureListener {
            data.value = FirebaseState.failed(it.message)
        }


    }

    fun doPredictions(
        monthlyData: LinkedList<MutableMap<String, Any>>,
        predictions: MutableStateFlow<List<String>>
    ) {
        val data = ArrayList<String>()
        if(monthlyData.isNotEmpty()) {
            val month1 = monthlyData[0]["AMOUNT"] as Long
            val month2 = monthlyData[1]["AMOUNT"] as Long
            Log.e("predictions", "$monthlyData")
            val out = month1 - month2
            val ab = abs(out)
            val str = "${if (out > 0) "Increased by $ab" else "Decreased by $ab"} from past month!"
            data.add(str)
            predictions.value = data
        }
    }

    private fun saveHomeData(
        home: HomeData,
//        donutList: ArrayList<DonutSection>,
        todayDetail: ArrayList<Detail>,
        homeUserMap: HomeUserMap,
        monthTotalAmount: Int,
        todayTotalAmount: Int
    ) {
        val data = SavedHomeData(
            UUID = uuid,
            home = home,
//            donutList = donutList,
            homeUserMap = homeUserMap,
            detailList = todayDetail,
            monthTotalAmount = monthTotalAmount,
            todayTotalAmount = todayTotalAmount
        )
        db.collection(roomKey + "_HOME")
            .document("HOME")
            .set(data)
            .addOnSuccessListener {

                Log.e("HOME", "Saved $data")
            }.addOnFailureListener {
                Log.e("HOME", "$it")
            }
    }

    private fun saveHomeDataLocal(data: SavedHomeData) {
        try {
            val userFile = File(application.filesDir, "/$roomKey.json")
            if (!userFile.exists())
                userFile.createNewFile()
            userFile.writeText(Gson().toJson(data))
        } catch (e: Exception) {
            Log.e("saveHomeDataLocal", "$e")
        }
    }

    private fun readHomeDataLocal(): SavedHomeData? {
        return try {
            val file = File(application.filesDir, "/$roomKey.json")
            if (file.exists()) {
                val json = file.readText(Charset.defaultCharset())
                val result = Gson().fromJson(json, SavedHomeData::class.java)
                Log.e("savedLocalHomeData", "$result")
                result
            } else
                null
        } catch (e: Exception) {
            Log.e("savedLocalHomeData", "$e")
            null
        }
    }

    suspend fun getSavedHomeData(
        liveData: MutableStateFlow<FirebaseState<HomeData>>,
        loadingHome: MutableStateFlow<Boolean>,
        homeUser: MutableStateFlow<HomeUserMap>,
        homeDetail: MutableStateFlow<List<Detail>>,
//        homeDonut: MutableStateFlow<List<DonutSection>>,
    ) = coroutineScope {
        loadingHome.value = true
        liveData.value = FirebaseState.loading()
        val query = db.collection(roomKey + "_HOME").document("HOME")
        query.addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("HOME", "$error")
            }
            if (value == null) {

            } else {
                value.let { snapshot ->
                    val result = snapshot.toObject(SavedHomeData::class.java)
                    if (result != null) {
                        Log.e("getSavedHomeData", "$result")
                        liveData.value = FirebaseState.success(result!!.home!!)
                        loadingHome.value = false
                        if (result.home != null) {
                            totalAmount.postValue(result.home!!.allTotal)
                            todayAmount.postValue(result.home!!.todayTotal)
                        }
//                    homeDonut.value = result.donutList as List<DonutSection>
                        homeDetail.value = result.detailList
                        homeUser.value = result.homeUserMap!!
//                        saveHomeDataLocal(result)
                    } else {
                        liveData.value = FirebaseState.empty()
                    }
                }
            }
        }
    }

    fun setMigrationSchedule(time: Instant, migrationSchedule: MutableStateFlow<Boolean>) {
        val alarmScheduler = AndroidAlarmScheduler(application)
        val localDateTime = LocalDateTime.ofInstant(time, ZoneId.systemDefault())
        val map = mapOf(
            "ROOM_ID" to roomKey,
            "TIME" to time.epochSecond
        )
        databaseReference.child(uuid!!)
            .child("IS_MIGRATE")
            .setValue(map)
            .addOnCompleteListener {

//                alarmScheduler.schedule(
//                    ScheduleItem(time = localDateTime, "Migration")
//                )
                migrationSchedule.value = true
            }
    }

    suspend fun saveVPA(vpa: String): Boolean {
        val job = suspendCoroutine<Boolean> { cont ->
            databaseReference.child(uuid!!)
                .child("VPA")
                .setValue(vpa)
                .addOnCompleteListener {
                    Log.e("vpa", "saved ${it.isSuccessful}")
                    cont.resume(true)
                }
        }
        return job
    }

    suspend fun splitExpense(
        amount: String,
        splitFor: String?,
        roomieData: MutableMap<String, Any>,
        splitExpenseSuccess: MutableStateFlow<Boolean>
    ) {
        val split2 = mutableMapOf<String, Any>()
        val timeStamp = System.currentTimeMillis()
        split2["UUID"] = user?.uid.toString()
        split2["BY_NAME"] = userName
        split2["TOTAL_AMOUNT"] = amount.toDouble()
        split2["ITEM"] = splitFor.toString()
        split2["NOTE"] = ""
        split2["TIME_STAMP"] = timeStamp
        split2["DATE"] = date
        split2["TIME"] = time
        split2["COMPLETED"] = false
        split2["DELETED"] = false
        val listData = mutableListOf<MutableMap<String, Any>>()
        roomieData.filter { it.value != 0.0 }.forEach {
            listData.add(mutableMapOf("UUID" to it.key, "AMOUNT" to it.value))
        }

        split2["FOR"] = listData
        roomKey = dataStore.getRoomKey()
        db.collection(roomKey + "_SPLIT").document(timeStamp.toString()).set(split2)
            .addOnCompleteListener {
                Log.e("splitExpense", "${it.isSuccessful}")
                splitExpenseSuccess.value = it.isSuccessful
            }.addOnFailureListener {
                Log.e("splitExpense error", "$it")
                splitExpenseSuccess.value = false
            }
    }

    fun getSplitData(
        splitData: MutableStateFlow<List<SplitData?>>,
        splitExpenseOwed: MutableStateFlow<OwedSplit>
    ) {
        val query =
            db.collection(roomKey + "_SPLIT").orderBy("TIME_STAMP", Query.Direction.DESCENDING)
        query.addSnapshotListener { value, error ->
            Log.e("splitExpense", "$value $error")
            if (error != null) {
                Log.e("Split ERROR", "$error")
            }
            if (value == null) {

            } else {
                value.documents.let { snapshots ->
                    if (snapshots.isEmpty()) {

                    } else {
                        val docs = snapshots.map {
                            it.toObject(SplitData::class.java)
                        }.filter { it?.DELETED == false && it.FOR.any { it["UUID"] == uuid } }

                        Log.e("splitExpense", "$docs")
                        var owedByYou = 0.0
                        var owedToYou = 0.0
                        docs.forEach {
                            val FOR = it?.FOR as MutableList<MutableMap<String, Any>>
                            if (it.UUID != uuid) {
                                owedByYou += FOR.filter {
                                    it["UUID"] == uuid
                                }.sumOf { it["AMOUNT"] as Double }
                            } else {
                                owedToYou += FOR.filter {
                                    it["UUID"] != uuid
                                }.sumOf { it["AMOUNT"] as Double }
                            }
                        }
                        splitExpenseOwed.value = OwedSplit(owedByYou, owedToYou)
                        splitData.value = docs
                    }
                }
            }
        }
    }

    private val featureMap: MutableList<String> =
        mutableListOf("FEAT_SPLIT_EXPENSE", "FEAT_ANALYTICS")

    fun enableFeature(featureKey: String) {
        val uid = user?.uid
        if (featureMap.contains(featureKey)) {
            databaseReference.child(uid!!).child(featureKey).setValue(true).addOnCompleteListener {

            }
        }

    }
}