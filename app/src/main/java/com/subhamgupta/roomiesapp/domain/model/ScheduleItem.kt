package com.subhamgupta.roomiesapp.domain.model

import java.time.LocalDateTime

data class ScheduleItem(
    val time: LocalDateTime,
    val message: String
)