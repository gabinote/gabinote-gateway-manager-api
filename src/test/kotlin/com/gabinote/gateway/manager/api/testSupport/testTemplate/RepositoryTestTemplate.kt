package com.gabinote.gateway.manager.api.testSupport.testTemplate


import com.gabinote.gateway.manager.api.testSupport.testConfig.db.UseTestDatabase
import com.gabinote.gateway.manager.api.testSupport.testConfig.general.UseGeneralTestConfig
import com.gabinote.gateway.manager.api.testSupport.testUtil.db.DbUnitTestHelper
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.data.auditing.AuditingHandler
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.time.LocalDateTime
import java.util.*

@EnableJpaAuditing
@ExtendWith(MockKExtension::class)
@Import(
    DbUnitTestHelper::class,
)
@DataJpaTest(showSql = false)
@UseGeneralTestConfig
@UseTestDatabase
abstract class RepositoryTestTemplate : DescribeSpec() {

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var dbUnitTestHelper: DbUnitTestHelper

    @Autowired
    lateinit var auditingHandler: AuditingHandler

    init {
        beforeSpec {
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
    }
}