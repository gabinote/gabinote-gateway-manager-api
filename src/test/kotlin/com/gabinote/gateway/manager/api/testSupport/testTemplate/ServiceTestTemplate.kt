package com.gabinote.gateway.manager.api.testSupport.testTemplate


import com.gabinote.gateway.manager.api.testSupport.testConfig.general.UseGeneralTestConfig
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.annotation.DirtiesContext

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(MockKExtension::class)
@UseGeneralTestConfig
abstract class ServiceTestTemplate : DescribeSpec()
