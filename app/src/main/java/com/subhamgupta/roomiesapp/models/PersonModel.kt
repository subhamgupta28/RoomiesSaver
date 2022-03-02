package com.subhamgupta.roomiesapp.models;



import com.google.gson.annotations.SerializedName

data class PersonModel (
    @SerializedName("KEY"         ) var KEY        : String? = null,
    @SerializedName("MONEY_SPENT" ) var MONEY_SPENT : String? = null,
    @SerializedName("USER_NAME"   ) var USERNAME   : String? = null,
    @SerializedName("UUID"        ) var UUID       : String? = null
)

