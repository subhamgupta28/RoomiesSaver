package com.subhamgupta.roomiesapp.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhamgupta.roomiesapp.data.repositories.RoomRepository
import com.subhamgupta.roomiesapp.domain.model.CreateRoom
import com.subhamgupta.roomiesapp.utils.FirebaseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val repository : RoomRepository
) : ViewModel() {

    private val _createRoom = MutableStateFlow<FirebaseState<CreateRoom>>(FirebaseState.empty())
    val createRoom = _createRoom.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getUser()
        }
    }

    fun createRoom(name: String, limit:Int, id: String, date :String) = viewModelScope.launch(Dispatchers.IO){
            repository.createRoom(name, limit, id, date, _createRoom)
    }
    fun joinRoom(room_id: String) = viewModelScope.launch(Dispatchers.IO){
        repository.joinRoom(room_id, _createRoom)
    }
}