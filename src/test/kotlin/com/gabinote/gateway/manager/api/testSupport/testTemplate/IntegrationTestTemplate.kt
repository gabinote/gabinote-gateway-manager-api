package com.gabinote.gateway.manager.api.testSupport.testTemplate

import com.fasterxml.jackson.databind.ObjectMapper
import com.gabinote.gateway.manager.api.testSupport.testConfig.db.UseTestDatabase
import com.gabinote.gateway.manager.api.testSupport.testConfig.general.UseGeneralTestConfig
import com.gabinote.gateway.manager.api.testSupport.testUtil.db.DbSqlCounter
import com.gabinote.gateway.manager.api.testSupport.testUtil.db.DbUnitTestHelper
import com.gabinote.gateway.manager.api.testSupport.testUtil.db.TestTransactionHookAspect
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.core.test.TestCaseOrder
import io.restassured.RestAssured
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.data.auditing.AuditingHandler
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.util.*


@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
@Import(
    DbUnitTestHelper::class,
    DbSqlCounter::class,
    TestTransactionHookAspect::class,
)
@Testcontainers
@UseTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@UseGeneralTestConfig
abstract class IntegrationTestTemplate : FeatureSpec() {
    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var dbUnitTestHelper: DbUnitTestHelper

    @Autowired
    lateinit var auditingHandler: AuditingHandler

    @Autowired
    lateinit var dbSqlCounter: DbSqlCounter


    val apiPrefix: String = "/api/v1"


    fun beforeTest() {
        dbSqlCounter.clearCounts()
    }

    fun beforeSpec() {
        RestAssured.basePath = apiPrefix
        RestAssured.port = port
        auditingHandler.setDateTimeProvider(DateTimeProvider {
            Optional.of(
                LocalDateTime.of(
                    2002,
                    8,
                    28,
                    0,
                    0,
                    0,
                    0
                )
            )
        })
    }


    override fun testCaseOrder(): TestCaseOrder = TestCaseOrder.Random

    init {
        beforeSpec {
            beforeSpec()
        }

        beforeTest {
            beforeTest()
        }

    }
}