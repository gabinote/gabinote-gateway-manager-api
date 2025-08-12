package com.gabinote.gateway.manager.api.common.web.advice

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.gabinote.gateway.manager.api.common.util.exception.BaseAppException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.net.URI
import java.util.*

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionAdvice {

    private fun getRequestId(request: HttpServletRequest): String =
        request.getHeader("X-Request-Id")?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()

    private fun problemDetail(
        status: HttpStatus,
        title: String? = "Unexpected Error",
        detail: String? = null,
        type: URI = URI("about:blank"),
        instance: URI? = null,
        requestId: String? = null,
        additionalProperties: Map<String, Any> = emptyMap()
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(status, detail ?: title)
        problemDetail.title = title
        problemDetail.type = type
        problemDetail.instance = instance
        problemDetail.properties?.let { it["requestId"] = requestId ?: UUID.randomUUID().toString() }
        problemDetail.properties?.putAll(additionalProperties)
        return problemDetail
    }

    // Handle custom application exceptions
    @ExceptionHandler(BaseAppException::class)
    fun handleBaseAppException(
        exception: BaseAppException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {

        val requestId = getRequestId(request)
        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = exception.status,
            error = exception::class.simpleName ?: "Exception",
            message = exception.loggingDetail
        )

        logger.error { log }
        logger.debug { "$log -> ${exception.stackTraceToString()}" }

        val res = problemDetail(
            status = exception.status,
            title = exception.title,
            detail = exception.message,
            type = URI("about:blank"),
            instance = URI(request.requestURI),
            requestId = requestId,
        )
        return ResponseEntity.status(exception.status).body(res)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.BAD_REQUEST

        val clientDetails = ex.bindingResult.fieldErrors.map { it.defaultMessage ?: "Invalid value" }
        val serverDetails = ex.bindingResult.fieldErrors.map {
            "${it.field}: ${it.defaultMessage} (rejected value: ${it.rejectedValue})"
        }

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "MethodArgumentNotValidException",
            message = serverDetails.joinToString("; ")
        )
        logger.error { log }
        logger.debug(ex) { "Validation stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Validation Failed",
            detail = "Validation failed for the request. See errors property for details.",
            instance = URI(request.requestURI),
            requestId = requestId,
            additionalProperties = mapOf("errors" to clientDetails)
        )

        return ResponseEntity.status(httpStatus).header("X-Request-Id", requestId).body(pd)
    }

    @ExceptionHandler(BindException::class)
    fun handleBindException(
        ex: BindException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.BAD_REQUEST

        val clientDetails = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        val serverDetails = ex.bindingResult.fieldErrors.map {
            "${it.field}: ${it.defaultMessage} (rejected value: ${it.rejectedValue})"
        }

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "BindException",
            message = serverDetails.joinToString("; ")
        )
        logger.error { log }
        logger.debug(ex) { "BindException stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Binding Failed",
            detail = "Request binding failed. See errors property for details.",
            instance = URI(request.requestURI),
            requestId = requestId,
            additionalProperties = mapOf("errors" to clientDetails)
        )

        return ResponseEntity.status(httpStatus).header("X-Request-Id", requestId).body(pd)
    }

    // --- Missing parameter ---
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(
        ex: MissingServletRequestParameterException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.BAD_REQUEST
        val detail = "Required parameter '${ex.parameterName}' of type '${ex.parameterType}' is missing"

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "MissingServletRequestParameterException",
            message = detail
        )
        logger.error { log }
        logger.debug(ex) { "Missing parameter stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Missing Required Parameter",
            detail = detail,
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).header("X-Request-Id", requestId).body(pd)
    }

    // --- Type mismatch ---
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.BAD_REQUEST
        val detail = "Parameter '${ex.name}' should be of type '${ex.requiredType?.simpleName}'"

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "MethodArgumentTypeMismatchException",
            message = ex.message
        )
        logger.error { log }
        logger.debug(ex) { "Type mismatch stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Type Mismatch",
            detail = detail,
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).header("X-Request-Id", requestId).body(pd)
    }

    // --- Malformed JSON / 메시지 읽기 실패 ---
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.BAD_REQUEST

        var detail = "Request body is not readable or malformed"
        val cause = ex.cause
        if (cause is MismatchedInputException) {
            val missingField = cause.path.joinToString(".") { it.fieldName }
            detail = "Missing or invalid required field: $missingField"
        }

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "HttpMessageNotReadableException",
            message = ex.message
        )
        logger.error { log }
        logger.debug(ex) { "Malformed body stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Malformed Request Body",
            detail = detail,
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).header("X-Request-Id", requestId).body(pd)
    }

    // --- HTTP method not allowed ---
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(
        ex: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.METHOD_NOT_ALLOWED
        val detail = "Method '${ex.method}' is not supported for this endpoint. Supported methods: ${
            ex.supportedMethods?.joinToString(", ")
        }"

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "HttpRequestMethodNotSupportedException",
            message = ex.message
        )
        logger.error { log }
        logger.debug(ex) { "Method not allowed stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Method Not Allowed",
            detail = detail,
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).header("X-Request-Id", requestId).body(pd)
    }

    // --- Unsupported media type ---
    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupportedException(
        ex: HttpMediaTypeNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.UNSUPPORTED_MEDIA_TYPE
        val detail = "Media type '${ex.contentType}' is not supported. Supported media types: ${
            ex.supportedMediaTypes.joinToString(", ")
        }"

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "HttpMediaTypeNotSupportedException",
            message = ex.message
        )
        logger.error { log }
        logger.debug(ex) { "Unsupported media type stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Unsupported Media Type",
            detail = detail,
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).header("X-Request-Id", requestId).body(pd)
    }

    // --- No resource / handler found ---
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(
        ex: NoResourceFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.NOT_FOUND
        val detail = "No resource found for ${ex.resourcePath}"

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "NoResourceFoundException",
            message = ex.message
        )
        logger.error { log }
        logger.debug(ex) { "No resource found stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Resource Not Found",
            detail = detail,
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).header("X-Request-Id", requestId).body(pd)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(
        ex: NoHandlerFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.NOT_FOUND
        val detail = "No handler found for ${ex.httpMethod} ${ex.requestURL}"

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "NoHandlerFoundException",
            message = ex.message
        )
        logger.error { log }
        logger.debug(ex) { "No handler found stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Handler Not Found",
            detail = detail,
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).header("X-Request-Id", requestId).body(pd)
    }

    // --- Method-level validation exceptions (ConstraintViolation) ---
    @ExceptionHandler(HandlerMethodValidationException::class, ConstraintViolationException::class)
    fun handleValidationExceptions(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.BAD_REQUEST

        val clientDetails = when (ex) {
            is ConstraintViolationException -> ex.constraintViolations.map { it.message ?: "Invalid value" }
            is HandlerMethodValidationException -> (ex as? ConstraintViolationException)?.constraintViolations?.map {
                it.message
                    ?: "Invalid value"
            } ?: listOf("Invalid value")

            else -> listOf("Invalid value")
        }

        val serverDetails = clientDetails // 간단화

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = ex::class.simpleName ?: "ValidationException",
            message = serverDetails.joinToString("; ")
        )
        logger.error { log }
        logger.debug(ex) { "Validation stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Validation Failed",
            detail = "Validation failed. See errors property for details.",
            instance = URI(request.requestURI),
            requestId = requestId,
            additionalProperties = mapOf("errors" to clientDetails)
        )

        return ResponseEntity.status(httpStatus).header("X-Request-Id", requestId).body(pd)
    }

    // --- Fallback: 모든 기타 예외 처리 ---
    @ExceptionHandler(Exception::class)
    fun handleException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.INTERNAL_SERVER_ERROR

        ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = ex::class.simpleName ?: "Exception",
            message = ex.message
        )
        logger.error { "Unhandled Exception [requestId=$requestId]: ${ex.message ?: ""}" }
        logger.debug(ex) { "Unhandled Exception stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Internal Server Error",
            detail = "An unexpected error occurred. (ref: $requestId)",
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).header("X-Request-Id", requestId).body(pd)
    }

}


