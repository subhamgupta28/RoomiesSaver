package com.subhamgupta.roomiessaver

interface onItemClick {
    fun sendTotalSpending(price:Int)
    fun updateTime(time:String)
    fun updateItem()
    fun allPersonSpending(sumMap: MutableMap<Int, Int>)
}