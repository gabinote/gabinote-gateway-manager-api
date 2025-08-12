package com.gabinote.gateway.manager.api.item.dto.service

data class ItemResServiceDto(
    val id: Long,
    val name: String,
    val url: String,
    val port: Int,
    val prefix: String? = null,
)