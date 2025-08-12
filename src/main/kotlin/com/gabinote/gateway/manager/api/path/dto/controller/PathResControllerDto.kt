package com.gabinote.gateway.manager.api.path.dto.controller

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.gabinote.gateway.manager.api.item.dto.controller.ItemResControllerDto
import com.gabinote.gateway.manager.api.path.domain.HttpMethodType

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PathResControllerDto(
    val id: Long,
    val path: String,
    val priority: Int,
    val enableAuth: Boolean,
    val role: String? = null,
    val httpMethod: HttpMethodType,
    val enabled: Boolean,
    val item: ItemResControllerDto,
)