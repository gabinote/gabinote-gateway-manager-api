package com.gabinote.gateway.manager.api.testSupport.testConfig.db

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.lang.annotation.Inherited


@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
//@DBRider
//@DBUnit(
//    dataTypeFactoryClass = MariaDBDataTypeFactory::class,
//    allowEmptyFields = true,
//    cacheConnection = false
//)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = [DatabaseContainerInitializer::class])
annotation class UseTestDatabase