package com.gabinote.gateway.manager.api.item.dto.controller

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ItemCreateReqControllerDto(
    val name: String,
    val url: String,
    val port: Int,
    val prefix: String? = null,
)