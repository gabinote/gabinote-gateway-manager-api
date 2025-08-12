package com.gabinote.gateway.manager.api.common.dto.error.controller

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.Instant

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ProblemDetailResControllerDto(
    val type: String? = null,              // 오류 분류 URI
    val title: String,                     // 오류 요약
    val status: Int,                       // HTTP 상태 코드
    val detail: String? = null,            // 상세 메시지
    val instance: String? = null,          // 오류 발생 리소스 경로나 ID
    val code: String? = null,              // 애플리케이션 고유 코드
    val errors: List<FieldErrorResControllerDto>? = null,  // 필드별 오류 상세
    val timestamp: Instant = Instant.now(),
    val path: String? = null,
    val requestId: String? = null,
    val documentationUrl: String? = null
)