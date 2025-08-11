package com.gabinote.gateway.manager.api.testSupport.testConfig.general

import org.springframework.test.context.TestPropertySource
import java.lang.annotation.Inherited

@TestPropertySource(locations = ["classpath:application-test.properties"])
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class UseGeneralTestConfig