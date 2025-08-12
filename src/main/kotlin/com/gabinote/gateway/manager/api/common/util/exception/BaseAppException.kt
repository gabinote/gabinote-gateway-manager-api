package com.gabinote.gateway.manager.api.common.util.exception

import org.springframework.http.HttpStatus


abstract class BaseAppException(
) : RuntimeException() {
    abstract val status: HttpStatus
    abstract val loggingDetail: String?
    abstract val title: String?
}