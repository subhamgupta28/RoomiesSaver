package com.subhamgupta.roomiesapp.domain.model

data class HomeUserMap(
    var eachPersonAmount: String? = null,
    var userMap: List<MutableMap<String, String>>? =null
)
