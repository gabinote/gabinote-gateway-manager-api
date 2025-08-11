package com.gabinote.gateway.manager.api.testSupport.testUtil.db

import com.gabinote.gateway.manager.api.testSupport.testConfig.db.MariaDBCustomDataTypeFactory
import org.dbunit.assertion.DbUnitAssert
import org.dbunit.assertion.DiffCollectingFailureHandler
import org.dbunit.database.DatabaseConfig
import org.dbunit.database.DatabaseConnection
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.dbunit.ext.mysql.MySqlMetadataHandler
import org.dbunit.operation.DatabaseOperation
import org.springframework.boot.test.context.TestComponent
import org.springframework.test.context.transaction.TestTransaction
import java.io.InputStream
import java.sql.Connection
import javax.sql.DataSource


@TestComponent
class DbUnitTestHelper(private val dataSource: DataSource) {

    private fun getConnection(conn: Connection): DatabaseConnection {
        val dbUnitConn = DatabaseConnection(conn)
        dbUnitConn.config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, MariaDBCustomDataTypeFactory())
        dbUnitConn.config.setProperty(DatabaseConfig.PROPERTY_METADATA_HANDLER, MySqlMetadataHandler())
        dbUnitConn.config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true)
        dbUnitConn.config.setProperty(DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES, true)
        return dbUnitConn
    }

    private fun insertDataSet(dataSet: IDataSet) {
        dataSource.connection.use { conn ->
            val dbUnitConn = getConnection(conn)
            DatabaseOperation.INSERT.execute(dbUnitConn, dataSet)
        }
    }

    private fun clearDB(excludeClearTable: List<String> = emptyList()) {
        dataSource.connection.use { conn ->
            val metaData = conn.metaData
            val tables = mutableListOf<String>()
            val rs = metaData.getTables(null, null, "%", arrayOf("TABLE"))
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"))
            }
            rs.close()
            // 외래키 제약조건 해제
            conn.createStatement().use { it.execute("SET FOREIGN_KEY_CHECKS = 0") }
            tables.forEach { table ->
                if (excludeClearTable.contains(table)) {
                    return@forEach
                }
                conn.createStatement().use { it.execute("TRUNCATE TABLE `$table`") }
            }
            // 외래키 제약조건 복구
            conn.createStatement().use { it.execute("SET FOREIGN_KEY_CHECKS = 1") }
        }
    }

    /**
     * 클래스패스에서 XML 파일을 로드하여 IDataSet으로 변환합니다.
     * @param path 클래스패스 기준의 XML 파일 경로
     * @return IDataSet 객체
     */
    private fun loadDataSetFromClasspath(path: String): IDataSet {
        val inputStream: InputStream = Thread.currentThread().contextClassLoader.getResourceAsStream(path)
            ?: throw IllegalArgumentException("File not found in classpath: $path")
        return FlatXmlDataSetBuilder().apply {
            isColumnSensing = true
            isCaseSensitiveTableNames = true
        }.build(inputStream)
    }

    /**
     * 주어진 XML 파일을 기반으로 데이터베이스를 초기화합니다.
     * @param xmlPath 클래스패스 기준의 XML 파일 경로 (예: "dataset/users.xml")
     */
    fun loadDataSet(
        xmlPath: String,
        excludeClearTable: List<String> = listOf("flyway_schema_history")
    ) {
        clearDB(excludeClearTable)
        val dataSet = loadDataSetFromClasspath(xmlPath)
        insertDataSet(dataSet)
    }


    fun loadDataSets(
        vararg xmlPath: String,
        excludeClearTable: List<String> = listOf("flyway_schema_history")
    ) {
        clearDB(excludeClearTable)
        xmlPath.forEach { path ->
            val dataSet = loadDataSetFromClasspath(path)
            insertDataSet(dataSet)
        }
    }


    fun assertDataset(expectedXmlPath: String, txForceCommit: Boolean = true) {
        // 검증전에 미리 기존 트랜잭션을 강제 커밋처리
        if (txForceCommit) {
            TestTransaction.flagForCommit()
            TestTransaction.end()
        }

        dataSource.connection.use { conn ->
            val dbUnitConn = getConnection(conn)

            // 기대하는 데이터셋 로드
            val expectedDataSet = loadDataSetFromClasspath(expectedXmlPath)

            // 기대하는 테이블 이름 추출
            val tableNames = expectedDataSet.tableNames

            // 실제 데이터셋에서 동일한 테이블만 추출
            val actualDataSet = dbUnitConn.createDataSet(tableNames)

            // 비교 수행
            val handler = DiffCollectingFailureHandler()
            val dbUnitAssert = DbUnitAssert()
            dbUnitAssert.assertEquals(expectedDataSet, actualDataSet, handler)

            if (handler.diffList.isNotEmpty()) {
                throw AssertionError("Database state does not match expected dataset. Differences: ${handler.diffList}")
            }
        }
    }

    fun assertDatasets(vararg expectedXmlPath: String) {
        dataSource.connection.use { conn ->
            val dbUnitConn = getConnection(conn)

            expectedXmlPath.forEach { path ->

                val expectedDataSet = loadDataSetFromClasspath(path)

                // 기대하는 테이블 이름 추출
                val tableNames = expectedDataSet.tableNames

                // 실제 데이터셋에서 동일한 테이블만 추출
                val actualDataSet = dbUnitConn.createDataSet(tableNames)

                // 비교 수행
                val handler = DiffCollectingFailureHandler()
                val dbUnitAssert = DbUnitAssert()
                dbUnitAssert.assertEquals(expectedDataSet, actualDataSet, handler)

                if (handler.diffList.isNotEmpty()) {
                    throw AssertionError("Database state does not match expected dataset for $path. Differences: ${handler.diffList}")
                }
            }
        }
    }
}