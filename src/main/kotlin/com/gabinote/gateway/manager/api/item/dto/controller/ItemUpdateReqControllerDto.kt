package com.gabinote.gateway.manager.api.item.dto.controller

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ItemUpdateReqControllerDto(

    @field:Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    val name: String? = null,

    @field:Size(min = 1, max = 255, message = "URL must be between 1 and 2048 characters")
    val url: String? = null,

    @field:Max(value = 65535, message = "Port is out of range")
    val port: Int? = null,

    @field:Size(min = 1, max = 255, message = "Prefix must be between 1 and 255 characters")
    val prefix: String? = null,
)