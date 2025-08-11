package com.gabinote.gateway.manager.api.testSupport.testUtil.db

import jakarta.persistence.EntityManager
import org.hibernate.Session
import org.hibernate.stat.Statistics
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@TestComponent
class DbSqlCounter(
    @Autowired
    private val entityManager: EntityManager,
) {

    var insertCount = 0
    var updateCount = 0
    var deleteCount = 0
    var fetchCount = 0
    val session: Session = entityManager.unwrap(Session::class.java)
    val statistics: Statistics = session.sessionFactory.statistics

    fun activate() {
        statistics.isStatisticsEnabled = true
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCompletion(status: Int) {
                insertCount += statistics.entityInsertCount.toInt()
                updateCount += statistics.entityUpdateCount.toInt()
                deleteCount += statistics.entityDeleteCount.toInt()
                fetchCount += statistics.queryExecutionCount.toInt()
                statistics.clear()
            }
        })
    }

    fun clearCounts() {
        insertCount = 0
        updateCount = 0
        deleteCount = 0
        fetchCount = 0
    }

    val hasInteraction: Boolean
        get() = insertCount > 0 || updateCount > 0 || deleteCount > 0 || fetchCount > 0

    val hasInert: Boolean
        get() = insertCount > 0

    val hasUpdate: Boolean
        get() = updateCount > 0

    val hasDelete: Boolean
        get() = deleteCount > 0

    val hasFetch: Boolean
        get() = fetchCount > 0
}