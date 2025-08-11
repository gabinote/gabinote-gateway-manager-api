package com.gabinote.api.testSupport.testUtil.objectMapper

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content

object TestObjectMapperExtension {
    fun ObjectMapper.toMatcher(obj: Any): ResultMatcher {
        return content().json(
            this.writeValueAsString(obj),
        )
    }
}