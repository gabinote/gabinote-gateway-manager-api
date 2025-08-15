package com.gabinote.gateway.manager.api

import com.gabinote.gateway.manager.api.testSupport.testConfig.db.UseTestDatabase
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@UseTestDatabase
@SpringBootTest
class GatewayManagerApiApplicationTests {

    @Test
    fun contextLoads() {
    }

}
