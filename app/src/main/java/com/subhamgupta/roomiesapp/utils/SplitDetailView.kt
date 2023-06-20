package com.subhamgupta.roomiesapp.utils

import com.subhamgupta.roomiesapp.domain.model.SplitData

interface SplitDetailView {
    fun openView(splitData: SplitData)
    fun pay(splitData: SplitData)
}