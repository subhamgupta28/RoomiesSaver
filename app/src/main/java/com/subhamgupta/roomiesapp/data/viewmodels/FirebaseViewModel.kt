package com.subhamgupta.roomiesapp.data.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.subhamgupta.roomiesapp.data.repositories.FireBaseRepository
import com.subhamgupta.roomiesapp.domain.model.Alerts
import com.subhamgupta.roomiesapp.domain.model.Detail
import com.subhamgupta.roomiesapp.domain.model.HomeData
import com.subhamgupta.roomiesapp.domain.model.RoomDetail
import com.subhamgupta.roomiesapp.utils.FirebaseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirebaseViewModel @Inject constructor(
    private var repository: FireBaseRepository
) : ViewModel() {

    private val _startDate = MutableLiveData<Boolean>()
    val startDate: LiveData<Boolean> = _startDate

    private val _leaveRoom = MutableLiveData<Boolean>()
    val leaveRoom: LiveData<Boolean> = _leaveRoom

    private val _sheetLoading = MutableStateFlow<FirebaseState<Boolean>>(FirebaseState.empty())
    val sheetLoading = _sheetLoading.asStateFlow()

    private val _editUser = MutableStateFlow<FirebaseState<Boolean>>(FirebaseState.empty())
    val editUser = _editUser.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _loadingHome = MutableStateFlow(false)
    private val loadingHome = _loadingHome.asStateFlow()

    private val _alert = MutableStateFlow(Alerts())
    val alert = _alert.asStateFlow()

    val map = mutableMapOf(
        "IS_ROOM_JOINED" to true,
        "UUID" to "h"
    )
    private val _userData = MutableStateFlow(map.toMutableMap())
    val userData = _userData.asStateFlow()

    private val _homeData = MutableStateFlow<FirebaseState<HomeData>>(FirebaseState.loading())
    val homeData = _homeData.asStateFlow()

    private val _summary = MutableStateFlow<FirebaseState<List<Detail?>>>(FirebaseState.loading())
    val summary = _summary.asStateFlow()

    private val _roomDetails = MutableStateFlow<FirebaseState<RoomDetail>>(FirebaseState.loading())
    val roomDetail = _roomDetails.asStateFlow()

    private val _addItem = MutableLiveData<Boolean>(false)
    val addItem: LiveData<Boolean> = _addItem

    init {
        repository.setFCM()
    }
    fun clearStorage(){
        repository.clearStorage()
    }

    fun getRoomMates() = repository.getRoomMates()
    val getLoading = loadingHome

    fun fetchAlert() {
        repository.fetchAlert(_alert)
    }

    fun generateSheet() = viewModelScope.launch {
        repository.generateExcel(_sheetLoading)
    }

    fun leaveRoom(key:String) = viewModelScope.launch(Dispatchers.IO){
        repository.leaveRoom(_leaveRoom, key)
    }
    fun getData() = viewModelScope.launch(Dispatchers.IO) {
        fetchUserData()
        _loading.value = false
        repository.loading.collect {
            if (!it) {
                fetchHomeData()
                fetchSummary(true, "All")
            }
        }
    }
    fun refreshData() = viewModelScope.launch(Dispatchers.IO){
        repository.fetchUserRoomData(_userData, _roomDetails)
    }

    fun editUser(uri: Uri, userName: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.uploadPic(uri, userName, _editUser)
    }

    fun fetchUserData() = viewModelScope.launch(Dispatchers.IO) {
        repository.forceInit()
        repository.prepareRoom(_userData, _roomDetails)
    }

    fun getUuidLink(): MutableMap<String, Int> {
        return repository.getUuidLink()
    }

    fun getDataStore() = repository.getDataStore()


    fun logout() = viewModelScope.launch(Dispatchers.IO) {
        repository.signOut()
    }

    fun sendNotification(title: String, message: String) {
        repository.sendNotify(title, message)
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

    fun modifyStartDate(timeStamp: Long) = viewModelScope.launch(Dispatchers.IO) {
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

    fun fetchSummary(currMonth: Boolean, category: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.fetchSummary(currMonth, category, _summary)
    }


    fun updateItem(item: String?, amount: String, timeStamp: String, note: String, tags: List<String>, category: String) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateItem(item, amount, timeStamp, _addItem, note, tags, category)
        }

    fun getAllRoomsDetails(): MutableLiveData<MutableList<RoomDetail>> {
        return repository.getAllRoomDetail()
    }

    private fun fetchHomeData() = viewModelScope.launch(Dispatchers.IO) {
        repository.fetchHomeData(_homeData, _loadingHome)
    }

    fun getTotalAmount(): MutableLiveData<Int> {
        return repository.getTotalAmount()
    }


    fun createAlert(msg: String) {
        repository.createAlert(msg)
    }
}