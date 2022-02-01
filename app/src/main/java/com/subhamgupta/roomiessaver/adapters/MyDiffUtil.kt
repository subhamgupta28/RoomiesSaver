package com.subhamgupta.roomiessaver.adapters

import androidx.recyclerview.widget.DiffUtil

class MyDiffUtil(
    private val oldList: List<Map<String, Any?>?>,
    private val newList: List<Map<String, Any?>?>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition]?.get("UUID") == newList[newItemPosition]?.get("UUID")
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition]?.get("UUID") != newList[newItemPosition]?.get("UUID") -> {
                false
            }
            oldList[oldItemPosition]?.get("DATE") != newList[newItemPosition]?.get("DATE") -> {
                false
            }
            oldList[oldItemPosition]?.get("TIME") != newList[newItemPosition]?.get("TIME") -> {
                false
            }
            oldList[oldItemPosition]?.get("BOUGHT_BY") != newList[newItemPosition]?.get("BOUGHT_BY") -> {
                false
            }
            oldList[oldItemPosition]?.get("TIME_STAMP") != newList[newItemPosition]?.get("TIME_STAMP") -> {
                false
            }
            oldList[oldItemPosition]?.get("ITEM_BOUGHT") != newList[newItemPosition]?.get("ITEM_BOUGHT") -> {
                false
            }
            oldList[oldItemPosition]?.get("AMOUNT_PAID") != newList[newItemPosition]?.get("AMOUNT_PAID") -> {
                false
            }
            else -> {
                true
            }
        }

    }


}