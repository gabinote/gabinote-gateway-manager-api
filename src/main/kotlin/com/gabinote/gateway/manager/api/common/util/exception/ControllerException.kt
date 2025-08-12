package com.gabinote.gateway.manager.api.common.util.exception

import org.springframework.http.HttpStatus


class ControllerException(
    override val status: HttpStatus,
    override val loggingDetail: String?,
    override val title: String? = null,
) : BaseAppException()