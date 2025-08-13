package com.gabinote.gateway.manager.api.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJpaRepositories(basePackages = ["com.gabinote.gateway.manager.api"])
@EnableJpaAuditing
@Configuration
class JpaConfig