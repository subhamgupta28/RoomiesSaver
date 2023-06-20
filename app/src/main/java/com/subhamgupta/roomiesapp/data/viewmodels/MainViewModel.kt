package com.subhamgupta.roomiesapp.data.viewmodels

//import app.futured.donut.DonutSection
import android.net.Uri
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.subhamgupta.roomiesapp.data.repositories.MainRepository
import com.subhamgupta.roomiesapp.domain.model.Alerts
import com.subhamgupta.roomiesapp.domain.model.Detail
import com.subhamgupta.roomiesapp.domain.model.HomeData
import com.subhamgupta.roomiesapp.domain.model.HomeUserMap
import com.subhamgupta.roomiesapp.domain.model.OwedSplit
import com.subhamgupta.roomiesapp.domain.model.RoomDetail
import com.subhamgupta.roomiesapp.domain.model.SavedHomeData
import com.subhamgupta.roomiesapp.domain.model.SplitData
import com.subhamgupta.roomiesapp.utils.AuthState
import com.subhamgupta.roomiesapp.utils.FirebaseService
import com.subhamgupta.roomiesapp.utils.FirebaseState
import com.subhamgupta.roomiesapp.utils.NetworkObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.LinkedList
import javax.inject.Inject

//Roomies@pass12345
@HiltViewModel
class MainViewModel @Inject constructor(
    private var repository: MainRepository
) : ViewModel() {

    @Inject
    lateinit var networkMainObserver: NetworkObserver

    @Inject
    lateinit var auth: FirebaseAuth

    private val _startDate = MutableLiveData<Boolean>()
    val startDate: LiveData<Boolean> = _startDate

    private val _leaveRoom = MutableLiveData<Boolean>()
    val leaveRoom: LiveData<Boolean> = _leaveRoom

    private val _homeUser = MutableStateFlow(HomeUserMap())
    val homeUserMap = _homeUser.asStateFlow()

    private val _homeDonut = MutableStateFlow<List<String>>(emptyList())
    val homeDonut = _homeDonut.asStateFlow()

    private val _homeDetails = MutableStateFlow<List<Detail>>(emptyList())
    val homeDetails = _homeDetails.asStateFlow()

    private val _sheetLoading = MutableStateFlow<FirebaseState<Boolean>>(FirebaseState.empty())
    val sheetLoading = _sheetLoading.asStateFlow()

    private val _authState = MutableStateFlow<AuthState<FirebaseUser?>>(AuthState.loading())
    val authState = _authState.asStateFlow()

    private val _editUser = MutableStateFlow<FirebaseState<Boolean>>(FirebaseState.empty())
    val editUser = _editUser.asStateFlow()

    private val _storedCards =
        MutableStateFlow<FirebaseState<MutableList<MutableMap<String, Any>>>>(FirebaseState.empty())
    val storedCards = _storedCards.asStateFlow()

    private val _uploadStoreCard = MutableStateFlow<FirebaseState<Boolean>>(FirebaseState.empty())
    val uploadStoreCard = _uploadStoreCard.asStateFlow()

    private val _predictions = MutableStateFlow<List<String>>(ArrayList())
    val predictions = _predictions.asStateFlow()

    private val _savedHomeData = MutableStateFlow<SavedHomeData?>(null)
    val saveHomeData = _savedHomeData.asStateFlow()

    private val _diffUserData =
        MutableStateFlow<MutableList<MutableMap<String, Any>>>(mutableListOf())
    val diffUserData = _diffUserData.asStateFlow()

    private val _alert = MutableStateFlow(Alerts())
    val alert = _alert.asStateFlow()

    private val _migrateCheck = MutableStateFlow<Int>(0)
    private val _migratePopup = MutableStateFlow(false)
    val migratePopup = _migratePopup.asStateFlow()

    val map = mutableMapOf(
        "IS_ROOM_JOINED" to true,
        "UUID" to "h"
    )
    private val _userDataLoading = MutableStateFlow(true)
    private val _roomDataLoading = MutableStateFlow(true)
    private val _homeDataLoading = MutableStateFlow(true)
    private val _summaryDataLoading = MutableStateFlow(false)

    private val _userData = MutableStateFlow(mutableMapOf<String, Any>())
    val userData = _userData.asStateFlow()

    private val _homeData = MutableStateFlow<FirebaseState<HomeData>>(FirebaseState.loading())
    val homeData = _homeData.asStateFlow()

    private val _summary = MutableStateFlow<FirebaseState<List<Detail?>>>(FirebaseState.loading())
    val summary = _summary.asStateFlow()

    private val _roomDetails = MutableStateFlow<FirebaseState<RoomDetail>>(FirebaseState.loading())
    val roomDetail = _roomDetails.asStateFlow()

    private val _addItem = MutableStateFlow(false)
    val addItem = _addItem.asStateFlow()

    private val _totalData = MutableStateFlow<List<Detail>>(LinkedList())
    val totalData = _totalData.asStateFlow()

    private val _monthlyData = MutableStateFlow<LinkedList<MutableMap<String, Any>>>(LinkedList())
    val monthlyData = _monthlyData.asStateFlow()

    private val _splitData = MutableStateFlow<List<SplitData?>>(ArrayList())
    val splitData = _splitData.asStateFlow()
    private val _splitExpenseSuccess = MutableStateFlow<Boolean>(false)
    val splitExpenseSuccess = _splitExpenseSuccess.asStateFlow()

    private val _splitExpenseOwed = MutableStateFlow<OwedSplit>(OwedSplit())
    val splitExpenseOwed = _splitExpenseOwed.asStateFlow()

    val splitDataTemp = MutableStateFlow<SplitData?>(null)

    private val _accountFeature = MutableStateFlow<MutableMap<String, Boolean>>(mutableMapOf())
    val accountFeature = _accountFeature.asStateFlow()

    private val handler = CoroutineExceptionHandler { _, throwable ->
        Log.e("MainViewModel", "$throwable")
    }

    fun clearStorage() {
        repository.clearStorage()
    }

    private fun setFCM() = viewModelScope.launch {
        val data = repository.getDataStore()
        FirebaseService.token = ""
        FirebaseService.uid = data.getUUID()
        FirebaseMessaging.getInstance()
            .subscribeToTopic("/topics/${data.getRoomKey()}")
            .addOnCompleteListener {
                Log.e("FCM", "${it.isSuccessful} ${it.exception} ${it.result}")
            }
    }

    fun getUserDataFromLocal(): Map<*, *>? {
        return repository.readUserDataFromRemote()
    }

    fun getRoomDataFromLocal(): Map<*, *>? {
        return repository.readDataFromRemote()
    }

    fun getNetworkObserver() = networkMainObserver

    fun getRoomMates() = repository.getRoomMates()
    val getLoading = _homeDataLoading

    fun fetchAlert() = viewModelScope.launch(Dispatchers.IO) {
        repository.fetchAlert(_alert)
    }

    fun generateSheet() = viewModelScope.launch(Dispatchers.IO) {
        repository.generateExcel(_sheetLoading)
    }

    fun leaveRoom(key: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.leaveRoom(_leaveRoom, key)
    }

    // onetime event
    fun initializeStore() {
        if (auth.currentUser != null) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.getUser(_userData, _userDataLoading, _accountFeature)
                _userDataLoading.collect {
                    if (!it) {
                        getData()
                        _userDataLoading.value = true
                    }
                }
            }
        }
        auth.addAuthStateListener {
            Log.e("auth state", "${it.currentUser}")
            if (it.currentUser != null) {
                _authState.value = AuthState.LoggedIn(it.currentUser)
            } else {
                _authState.value = AuthState.noUserOrError("No logged in user")
            }
        }
    }

    fun getData() = viewModelScope.launch(Dispatchers.IO) {
        fetchRoomData()
        _roomDataLoading.buffer().collectLatest { it1 ->
            if (!it1) {
                fetchHomeData()
                fetchSummary(true, "All")
                fetchAlert()
                getStoredCards()
                getDiffData()
                toggleMonthlyData(true)
                getSplitData()
//                repository.migrate()
                _roomDataLoading.value = true
            }
        }


        setFCM()
    }

    fun toggleMonthlyData(isMine: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        _totalData.collect {
            Log.e("toggleMonthlyData", "$isMine")
            if (it.isNotEmpty()) {
                repository.monthlyDataCal(_totalData, _monthlyData, isMine)
            }
        }
    }

    fun refreshData() = viewModelScope.launch(Dispatchers.IO) {
        repository.fetchUserRoomData(_userData, _roomDetails, _roomDataLoading)
    }


    fun editUser(uri: Uri, userName: String) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.uploadPic(uri, userName, _editUser)
        }

    private fun fetchRoomData() = viewModelScope.launch(Dispatchers.IO) {
        repository.forceInit()
        repository.prepareRoom(_userData, _roomDetails, _roomDataLoading)

    }

    fun getUuidLink(): MutableMap<String, Int> {
        return repository.getUuidLink()
    }

    fun getDataStore() = repository.getDataStore()


    fun logout() = viewModelScope.launch(Dispatchers.IO) {
        repository.signOut()
    }

    fun sendNotification(title: String, message: String) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.sendNotify("${repository.getDataStore().getUserName()} $title", message)
        }

    fun getRoomMap(): MutableLiveData<MutableMap<String, String>> {
        return repository.getRoomMaps()
    }

    fun getDiffData() {
        viewModelScope.launch(Dispatchers.IO) {
            _summary.collectLatest {
                when (it) {
                    is FirebaseState.Success -> {
                        repository.fetchDiffData(it.data, _diffUserData)
                    }

                    else -> {

                    }
                }
            }
        }
    }

    fun modifyStartDate(timeStamp: Long) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.modifyStartDate(timeStamp, _startDate)
            getDataStore().setStartDate(timeStamp.toString())
        }

    fun getTempRoomMaps(): MutableLiveData<MutableMap<String, String>> {
        return repository.getTempRoomMaps()
    }

    fun addItem(item: String?, amount: String, note: String, tags: List<String>, category: String) {
        repository.addItem(item, amount, _addItem, note, tags, category)

    }

    fun getUser(): FirebaseUser? {
        return repository.user
    }

    fun fetchSummary(currMonth: Boolean, category: String) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.fetchSummary(currMonth, category, _summary, _summaryDataLoading)
        }

    fun updateItem(
        item: String?,
        amount: String,
        timeStamp: String,
        note: String,
        tags: List<String>,
        category: String
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateItem(item, amount, timeStamp, _addItem, note, tags, category)
        }

    fun getAllRoomsDetails(): MutableLiveData<MutableList<RoomDetail>> {
        return repository.getAllRoomDetail()
    }

    private fun fetchHomeData() = viewModelScope.launch(Dispatchers.IO) {
        repository.generateHomeData(
            _homeData,
            _homeDataLoading,
            _homeUser,
            _homeDetails,
            _homeDonut,
            _migrateCheck,
            _totalData
        )
        launch {
            checkMigrate()
            doPredictions()
        }
//        repository.getSavedHomeData(
//            _homeData,
//            _homeDataLoading,
//            _homeUser,
//            _homeDetails,
//            _homeDonut
//        )
    }

    private fun checkMigrate() = viewModelScope.launch(Dispatchers.IO) {
        _migrateCheck.collect {
            if (it > 200) {
                _migratePopup.value = true
            }
        }
    }

    fun getTotalAmount(): MutableLiveData<Int> {
        return repository.getTotalAmount()
    }

    fun createAlert(msg: String) {
        repository.createAlert(msg)
    }

    fun getStoredCards() = viewModelScope.launch(Dispatchers.IO) {
        repository.getStoredItemCards(_storedCards)
    }

    fun uploadCard(
        date: String,
        note: String,
        byteArray: ByteArray
    ) = viewModelScope.launch(Dispatchers.IO) {
        repository.uploadCard(_uploadStoreCard, date, note, byteArray)
    }

    fun doPredictions() = viewModelScope.launch(Dispatchers.IO) {
        _monthlyData.collect{
            repository.doPredictions(it, _predictions)
        }

    }

    private val _migrationSchedule = MutableStateFlow(false)
    val migrationSchedule = _migrationSchedule.asStateFlow()

    fun setMigrationSchedule(time: Instant) = viewModelScope.launch(Dispatchers.IO) {
//        repository.setMigrationSchedule(time, _migrationSchedule)
        repository.migrate()
    }

    fun getTimeAgo(time: String): String {
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

    fun splitExpense(amount: String, splitFor: String?, roomieData: MutableMap<String, Any>) =
        viewModelScope.launch {
            repository.splitExpense(amount, splitFor, roomieData, _splitExpenseSuccess)
        }

    fun saveVPA(vpa: String) = viewModelScope.launch {
        val res = repository.saveVPA(vpa)
        if (res)
            repository.getUser(_userData, _userDataLoading, _accountFeature)
    }

    private fun getSplitData() = viewModelScope.launch {
        repository.getSplitData(_splitData, _splitExpenseOwed)
    }

    fun enableFeature(featureKey:String) = viewModelScope.launch{
        repository.enableFeature(featureKey)
    }
}