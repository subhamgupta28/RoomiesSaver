package com.subhamgupta.roomiesapp.domain.model

import kotlinx.serialization.SerialName
import java.io.Serializable


data class ROOMMATES(
    @SerialName("KEY")
    var KEY: String? = null,

    @SerialName("MONEY_PAID")
    var MONEY_PAID: Double? = null,

    @SerialName("USER_NAME")
    var USER_NAME: String? = null,

    @SerialName("UUID")
    var UUID: String? = null
): Serializable