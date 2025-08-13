package com.gabinote.gateway.manager.api.path.dto.controller

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.gabinote.gateway.manager.api.path.domain.HttpMethodType
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PathCreateReqControllerDto(
    @field:Size(min = 1, max = 255, message = "Path must be between 1 and 255 characters")
    val path: String,

    @field:Max(value = 2147483647, message = "Priority must be a up to 2147483647")
    val priority: Int,

    val enableAuth: Boolean,

    @field:Size(min = 1, max = 255, message = "Role must be between 1 and 255 characters")
    val role: String? = null,
    val httpMethod: HttpMethodType,
    val enabled: Boolean,
    
    @field:Positive(message = "Item ID must be a positive number")
    @field:Max(value = 9223372036854775807, message = "Item ID must be a up to 9223372036854775807")
    val itemId: Long
)