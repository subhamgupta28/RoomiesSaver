package com.subhamgupta.roomiessaver

interface onClickPerson {
    fun onClick(position: Int)
    fun onIssue()
    fun sendSumMap(sumMap: MutableMap<Int, Int>)
    fun sendSum(sum: Int)
    fun openEdit()
}