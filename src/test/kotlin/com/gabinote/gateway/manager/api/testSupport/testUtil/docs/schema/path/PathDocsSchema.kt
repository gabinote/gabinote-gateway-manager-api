package com.gabinote.gateway.manager.api.testSupport.testUtil.docs.schema.path

import com.gabinote.gateway.manager.api.testSupport.testUtil.docs.schema.common.PagedDocsSchema
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

object PathDocsSchema {
    val pathResponseSchema: Array<FieldDescriptor> = arrayOf(
        fieldWithPath("id").description("경로의 ID"),
        fieldWithPath("path").description("경로"),
        fieldWithPath("priority").description("우선순위"),
        fieldWithPath("enable_auth").description("인증 활성화 여부"),
        fieldWithPath("role").description("역할").optional(),
        fieldWithPath("http_method").description("HTTP 메서드"),
        fieldWithPath("enabled").description("활성화 여부"),
        fieldWithPath("item.id").description("아이템의 ID"),
        fieldWithPath("item.name").description("아이템의 이름"),
        fieldWithPath("item.url").description("아이템의 URL"),
        fieldWithPath("item.port").description("아이템의 포트"),
        fieldWithPath("item.prefix").description("아이템의 프리픽스").optional()
    )

    val pathPagedResponseSchema: Array<FieldDescriptor> = arrayOf(
        fieldWithPath("content[].id").description("경로의 ID"),
        fieldWithPath("content[].path").description("경로"),
        fieldWithPath("content[].priority").description("우선순위"),
        fieldWithPath("content[].enable_auth").description("인증 활성화 여부"),
        fieldWithPath("content[].role").description("역할").optional(),
        fieldWithPath("content[].http_method").description("HTTP 메서드"),
        fieldWithPath("content[].enabled").description("활성화 여부"),
        fieldWithPath("content[].item.id").description("아이템의 ID"),
        fieldWithPath("content[].item.name").description("아이템의 이름"),
        fieldWithPath("content[].item.url").description("아이템의 URL"),
        fieldWithPath("content[].item.port").description("아이템의 포트"),
        fieldWithPath("content[].item.prefix").description("아이템의 프리픽스").optional(),
        *PagedDocsSchema.pagedResourceSchema
    )
}