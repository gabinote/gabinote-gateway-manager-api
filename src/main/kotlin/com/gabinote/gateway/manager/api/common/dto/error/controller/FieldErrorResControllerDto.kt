package com.gabinote.gateway.manager.api.common.dto.error.controller

data class FieldErrorResControllerDto(
    val field: String,
    val message: String,
    val code: String? = null
)