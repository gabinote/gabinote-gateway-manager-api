package com.gabinote.gateway.manager.api.common.util.exception

import org.springframework.http.HttpStatus

class ServiceException(
    override val status: HttpStatus,
    override val loggingDetail: String?,
    override val title: String? = null,
    override val message: String? = null,
) : BaseAppException()