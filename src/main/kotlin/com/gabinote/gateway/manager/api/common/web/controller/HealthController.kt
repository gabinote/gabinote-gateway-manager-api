package com.gabinote.gateway.manager.api.common.web.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {
    @GetMapping("/health")
    fun healthCheck(): String {
        return "OK"
    }
}