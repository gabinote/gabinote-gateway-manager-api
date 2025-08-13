package com.gabinote.gateway.manager.api.testSupport.testUtil.docs.schema.item

import com.gabinote.gateway.manager.api.testSupport.testUtil.docs.schema.common.PagedDocsSchema
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

object ItemDocsSchema {
    val itemResponseSchema: Array<FieldDescriptor> = arrayOf(
        fieldWithPath("id").description("아이템의 ID"),
        fieldWithPath("name").description("아이템의 이름"),
        fieldWithPath("url").description("아이템의 URL"),
        fieldWithPath("port").description("아이템의 포트"),
        fieldWithPath("prefix").description("아이템의 Prefix. 예) prefix = api1 이면, http://localhost:3000/api/.. 처럼 동작")
            .optional()
    )

    val pagedItemResponseSchema: Array<FieldDescriptor> = arrayOf(
        // 기존 필드에 content[].xxx
        fieldWithPath("content[].id").description("아이템의 ID"),
        fieldWithPath("content[].name").description("아이템의 이름"),
        fieldWithPath("content[].url").description("아이템의 URL"),
        fieldWithPath("content[].port").description("아이템의 포트"),
        fieldWithPath("content[].prefix").description("아이템의 Prefix. 예) prefix = api1 이면, http://localhost:3000/api/.. 처럼 동작")
            .optional(),
        *PagedDocsSchema.pagedResourceSchema
    )
}