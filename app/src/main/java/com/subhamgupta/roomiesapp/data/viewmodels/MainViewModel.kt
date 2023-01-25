package com.subhamgupta.roomiesapp.data.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.futured.donut.DonutSection
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.subhamgupta.roomiesapp.data.repositories.MainRepository
import com.subhamgupta.roomiesapp.domain.model.*
import com.subhamgupta.roomiesapp.utils.AuthState
import com.subhamgupta.roomiesapp.utils.FirebaseService
import com.subhamgupta.roomiesapp.utils.FirebaseState
import com.subhamgupta.roomiesapp.utils.NetworkObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private var repository: MainRepository
) : ViewModel() {

    @Inject
    lateinit var networkMainObserver: NetworkObserver

    private val _startDate = MutableLiveData<Boolean>()
    val startDate: LiveData<Boolean> = _startDate

    private val _leaveRoom = MutableLiveData<Boolean>()
    val leaveRoom: LiveData<Boolean> = _leaveRoom

    private val _homeUser = MutableStateFlow(HomeUserMap())
    val homeUserMap = _homeUser.asStateFlow()

    private val _homeDonut = MutableStateFlow<List<DonutSection>>(emptyList())
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

    private val _predictions = MutableStateFlow<FirebaseState<List<String>>>(FirebaseState.empty())
    val predictions = _predictions.asStateFlow()

    private val _alert = MutableStateFlow(Alerts())
    val alert = _alert.asStateFlow()

    val map = mutableMapOf(
        "IS_ROOM_JOINED" to true,
        "UUID" to "h"
    )
    private val _userDataLoading = MutableStateFlow(true)

    private val _roomDataLoading = MutableStateFlow(true)
    private val _homeDataLoading = MutableStateFlow(true)
    private val _summaryDataLoading = MutableStateFlow(true)

    private val _userData = MutableStateFlow(mutableMapOf<String, Any>())
    val userData = _userData.asStateFlow()


    private val _homeData = MutableStateFlow<FirebaseState<HomeData>>(FirebaseState.loading())
    val homeData = _homeData.asStateFlow()

    private val _summary = MutableStateFlow<FirebaseState<List<Detail?>>>(FirebaseState.loading())
    val summary = _summary.asStateFlow()

    private val _roomDetails = MutableStateFlow<FirebaseState<RoomDetail>>(FirebaseState.loading())
    val roomDetail = _roomDetails.asStateFlow()

    private val _addItem = MutableLiveData<Boolean>(false)
    val addItem: LiveData<Boolean> = _addItem

    private val supervisor = SupervisorJob()

    private val handler = CoroutineExceptionHandler { _, throwable ->
        Log.e("MainViewModel", "$throwable")
    }

    fun clearStorage() {
        repository.clearStorage()
    }

    private fun setFCM() = viewModelScope.launch(Dispatchers.IO) {
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

    fun fetchAlert() = viewModelScope.launch(supervisor + Dispatchers.IO) {
        repository.fetchAlert(_alert)
    }

    fun generateSheet() = viewModelScope.launch {
        repository.generateExcel(_sheetLoading)
    }

    fun leaveRoom(key: String) = viewModelScope.launch(supervisor + Dispatchers.IO) {
        repository.leaveRoom(_leaveRoom, key)
    }

    fun initializeStore(){
        repository.auth.addAuthStateListener {
            Log.e("auth state","${it.currentUser}")
            if (it.currentUser!=null) {
                _authState.value = AuthState.LoggedIn(it.currentUser)
                viewModelScope.launch(supervisor + Dispatchers.IO) {
                    repository.getUser(_userData, _userDataLoading)
                    delay(1000)
                    _userDataLoading.collectLatest {flag->
                        if (!flag) {
                            getData()
                            _userDataLoading.value = true
                        }
                    }
                }
            }else{
                _authState.value = AuthState.noUserOrError("No logged in user")
            }
        }
    }

    fun getData() = viewModelScope.launch(supervisor + Dispatchers.IO) {
        fetchRoomData()
        setFCM()
        _roomDataLoading.collectLatest { it1 ->
            if (!it1) {
                fetchHomeData()
                fetchSummary(true, "All")
                _roomDataLoading.value = true
                fetchAlert()
                getStoredCards()
            }
        }
    }

    fun refreshData() = viewModelScope.launch(supervisor + Dispatchers.IO) {
        repository.fetchUserRoomData(_userData, _roomDetails, _roomDataLoading)
    }


    fun editUser(uri: Uri, userName: String) =
        viewModelScope.launch(supervisor + Dispatchers.IO) {
            repository.uploadPic(uri, userName, _editUser)
        }

    private fun fetchRoomData() = viewModelScope.launch(supervisor + Dispatchers.IO) {
        repository.forceInit()
        repository.prepareRoom(_userData, _roomDetails, _roomDataLoading)
    }

    fun getUuidLink(): MutableMap<String, Int> {
        return repository.getUuidLink()
    }

    fun getDataStore() = repository.getDataStore()


    fun logout() = viewModelScope.launch(supervisor + Dispatchers.IO) {
        repository.signOut()
    }

    fun sendNotification(title: String, message: String) =
        viewModelScope.launch(supervisor + Dispatchers.IO) {
            repository.sendNotify("${repository.getDataStore().getUserName()} $title", message)
        }

    fun getRoomMap(): MutableLiveData<MutableMap<String, String>> {
        return repository.getRoomMaps()
    }

    fun getDiffData(): LiveData<MutableList<MutableMap<String, Any>>> {
        viewModelScope.launch(Dispatchers.IO) {
            repository.fetchDiffData(ArrayList())
        }
        return repository.diffData
    }

    fun modifyStartDate(timeStamp: Long) =
        viewModelScope.launch(supervisor + Dispatchers.IO) {
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
        viewModelScope.launch(supervisor + Dispatchers.IO) {
            repository.fetchSummary(currMonth, category, _summary)
        }


    fun updateItem(
        item: String?,
        amount: String,
        timeStamp: String,
        note: String,
        tags: List<String>,
        category: String
    ) =
        viewModelScope.launch(supervisor + Dispatchers.IO) {
            repository.updateItem(item, amount, timeStamp, _addItem, note, tags, category)
        }

    fun getAllRoomsDetails(): MutableLiveData<MutableList<RoomDetail>> {
        return repository.getAllRoomDetail()
    }

    private fun fetchHomeData() = viewModelScope.launch(supervisor + Dispatchers.IO) {
        repository.fetchHomeData(_homeData, _homeDataLoading, _homeUser, _homeDetails, _homeDonut)
    }

    fun getTotalAmount(): MutableLiveData<Int> {
        return repository.getTotalAmount()
    }


    fun createAlert(msg: String) {
        repository.createAlert(msg)
    }

    fun getStoredCards() = viewModelScope.launch(supervisor + Dispatchers.IO) {
        repository.getStoredItemCards(_storedCards)
    }

    fun uploadCard(
        date: String,
        note: String,
        byteArray: ByteArray
    ) = viewModelScope.launch(supervisor + Dispatchers.IO) {
        repository.uploadCard(_uploadStoreCard, date, note, byteArray)
    }

    fun doPredictions() = viewModelScope.launch(supervisor + Dispatchers.IO) {
        repository.doPredictions(_summary,_predictions)
    }
}