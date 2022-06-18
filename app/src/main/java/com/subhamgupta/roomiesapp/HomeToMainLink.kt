package com.subhamgupta.roomiesapp

interface HomeToMainLink {
    fun goToMain(position: Int, uuid: String)
    fun goToAllExpenses()
    fun goToDiffUser()
}