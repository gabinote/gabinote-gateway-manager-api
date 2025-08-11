package com.gabinote.gateway.manager.api.testSupport.testConfig.db

import org.flywaydb.core.Flyway
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.MariaDBContainer


class DatabaseContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        @JvmStatic
        val database: MariaDBContainer<*> = MariaDBContainer("mariadb:11.2.3").apply {
            withUsername("admin")
            withPassword("admin")
            withDatabaseName("test")
            withLabel("group", "test-db")
        }
    }

    override fun initialize(context: ConfigurableApplicationContext) {
        // 테스트 컨테이너 시작
        database.start()

        // application.yml 대신 프로퍼티로 datasource 설정
        TestPropertyValues.of(
            "spring.datasource.url=${database.jdbcUrl}",
            "spring.datasource.username=${database.username}",
            "spring.datasource.password=${database.password}",
            "spring.test.database.replace=none"
        ).applyTo(context.environment)


        val flyway = Flyway.configure()
            .dataSource(
                database.jdbcUrl,
                database.username,
                database.password
            )
            .cleanDisabled(false)
            .locations("classpath:db/migration/main")
            .validateOnMigrate(true)
            .load()

        flyway.clean()
        flyway.migrate()

    }


}