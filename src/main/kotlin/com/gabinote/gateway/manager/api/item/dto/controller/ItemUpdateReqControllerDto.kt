package com.gabinote.gateway.manager.api.item.dto.controller

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ItemUpdateReqControllerDto(
    val name: String? = null,
    val url: String? = null,
    val port: Int? = null,
    val prefix: String? = null,
)