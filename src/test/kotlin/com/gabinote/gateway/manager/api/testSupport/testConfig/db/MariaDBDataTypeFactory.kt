package com.gabinote.gateway.manager.api.testSupport.testConfig.db

import org.dbunit.dataset.datatype.DataType
import org.dbunit.dataset.datatype.DataTypeException
import org.dbunit.dataset.datatype.DefaultDataTypeFactory
import java.util.*

class MariaDBDataTypeFactory : DefaultDataTypeFactory() {
    val validDbProducts: List<String>
        get() = Collections.singletonList("MariaDB")

    @Throws(DataTypeException::class, DataTypeException::class)
    override fun createDataType(sqlType: Int, sqlTypeName: String?): DataType {
        return super.createDataType(sqlType, sqlTypeName)
    }
}