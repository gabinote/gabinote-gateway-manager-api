package com.gabinote.gateway.manager.api.testSupport.testUtil.db

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.boot.test.context.TestComponent

@Aspect
@TestComponent
class TestTransactionHookAspect(
    private val dbSqlCounter: DbSqlCounter
) {
    @Around("within(@org.springframework.stereotype.Service *)")
    fun wrapServiceMethod(pjp: ProceedingJoinPoint): Any? {
        dbSqlCounter.activate()
        return pjp.proceed()
    }
}