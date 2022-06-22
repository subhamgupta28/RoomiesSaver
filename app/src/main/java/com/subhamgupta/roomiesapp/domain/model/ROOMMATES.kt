package com.subhamgupta.roomiesapp.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ROOMMATES(
    @SerialName("KEY")
    var KEY: String? = null,

    @SerialName("MONEY_PAID")
    var MONEY_PAID: Int? = null,

    @SerialName("USER_NAME")
    var USER_NAME: String? = null,

    @SerialName("UUID")
    var UUID: String? = null
)