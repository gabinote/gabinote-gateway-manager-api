package com.gabinote.gateway.manager.api.testSupport.testTemplate

import com.gabinote.gateway.manager.api.testSupport.testConfig.general.UseGeneralTestConfig
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext


@AutoConfigureMockMvc
@EnableAspectJAutoProxy
@AutoConfigureRestDocs
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(
    WebMvcTestTemplate.FilterConfig::class,
)
@ExtendWith(MockKExtension::class)
@UseGeneralTestConfig
abstract class WebMvcTestTemplate : DescribeSpec() {
    // 모든 사용자 정의 커스텀 필터 제거
    @TestConfiguration
    class FilterConfig

}