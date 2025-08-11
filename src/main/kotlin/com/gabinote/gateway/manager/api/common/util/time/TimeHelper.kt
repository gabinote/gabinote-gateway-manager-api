package com.gabinote.gateway.manager.api.common.util.time

import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

class TimeHelper(
    private val clock: Clock
) {
    fun nowDt(): LocalDateTime {
        return LocalDateTime.now(clock)
    }

    fun nowDate(): LocalDate {
        return LocalDate.now(clock)
    }
}