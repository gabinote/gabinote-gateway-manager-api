package com.gabinote.gateway.manager.api.path.dto.service

import com.gabinote.gateway.manager.api.path.domain.HttpMethodType

data class PathCreateReqServiceDto(
    val path: String,
    val priority: Int,
    val enableAuth: Boolean,
    val role: String? = null,
    val httpMethod: HttpMethodType,
    val enabled: Boolean,
    val itemId: Long
)