package com.gabinote.gateway.manager.api.testSupport.testConfig.kotest

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension

class KoTestConfig : AbstractProjectConfig() {

    override fun extensions(): List<Extension> = listOf(SpringTestContextModeExtension())

}