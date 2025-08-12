package com.gabinote.gateway.manager.api.path.dto.service

import com.gabinote.gateway.manager.api.item.dto.service.ItemResServiceDto
import com.gabinote.gateway.manager.api.path.domain.HttpMethodType

data class PathResServiceDto(
    val id: Long,
    val path: String,
    val priority: Int,
    val enableAuth: Boolean,
    val role: String? = null,
    val httpMethod: HttpMethodType,
    val enabled: Boolean,
    val item: ItemResServiceDto,
)