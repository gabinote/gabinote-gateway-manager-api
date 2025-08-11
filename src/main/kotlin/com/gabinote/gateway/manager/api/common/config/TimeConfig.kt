package com.gabinote.gateway.manager.api.common.config

import com.gabinote.gateway.manager.api.common.util.time.TimeHelper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class TimeConfig {

    private val clock: Clock = Clock.systemDefaultZone()

    @Bean
    fun timeHelper(): TimeHelper {
        return TimeHelper(clock)
    }

}