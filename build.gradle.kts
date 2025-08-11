plugins {
	kotlin("jvm") version "2.1.20"
	kotlin("plugin.spring") version "2.1.20"
	kotlin("plugin.jpa") version "2.1.20"
	kotlin("kapt") version "2.1.20"
	id("org.springframework.boot") version "3.4.4"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.epages.restdocs-api-spec") version "0.17.1"
	id("org.hidetake.swagger.generator") version "2.18.2"
}

group = "com.gabinote.gateway.manager"
version = "0.0.1-SNAPSHOT"

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

noArg {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

swaggerSources {
	create("sample") {
		setInputFile(file("${project.buildDir}/api-spec/openapi3.yaml"))
	}
}

openapi3 {
	title = "API 문서"
	description = "RestDocsWithSwagger Docs"
	version = "0.0.1"
	format = "yaml"
}

dependencyManagement {
	imports {
		mavenBom("org.testcontainers:testcontainers-bom:1.20.6")
	}
}

buildscript {
	extra["restdocsApiSpecVersion"] = "0.17.1"

}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	kapt("org.springframework.boot:spring-boot-configuration-processor")
	implementation("com.google.code.gson:gson:2.10.1")
	runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
	testRuntimeOnly("org.mariadb.jdbc:mariadb-java-client")

	// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-logging
//    implementation("org.springframework.boot:spring-boot-starter-logging")

	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springframework:spring-aspects")

	//tests
	testImplementation("org.junit.jupiter:junit-jupiter-api")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
	testImplementation("org.junit.jupiter:junit-jupiter-params")

	//mockito
	testImplementation("org.mockito:mockito-core")
	testImplementation("org.mockito:mockito-junit-jupiter")
	// https://mvnrepository.com/artifact/io.mockk/mockk
	testImplementation("io.mockk:mockk:1.14.2")
	// https://mvnrepository.com/artifact/com.ninja-squad/springmockk
	testImplementation("com.ninja-squad:springmockk:4.0.2")

	// https://mvnrepository.com/artifact/org.jsoup/jsoup
	implementation("org.jsoup:jsoup:1.19.1")

	//testcontainers
	testImplementation("org.testcontainers:mariadb:1.20.6")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:jdbc")


	// swagger docs
	implementation("org.webjars:swagger-ui:4.11.1")
	testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
	testImplementation("com.epages:restdocs-api-spec-mockmvc:0.17.1")

	// https://mvnrepository.com/artifact/io.github.oshai/kotlin-logging-jvm
	implementation("io.github.oshai:kotlin-logging-jvm:7.0.5")

	implementation("commons-io:commons-io:2.14.0")

	// https://mvnrepository.com/artifact/org.flywaydb/flyway-mysql
	implementation("org.flywaydb:flyway-mysql:11.5.0")
	// https://mvnrepository.com/artifact/org.flywaydb/flyway-core
	implementation("org.flywaydb:flyway-core:11.5.0")

	testImplementation("io.rest-assured:rest-assured:5.5.5")
	testImplementation("io.rest-assured:kotlin-extensions:5.5.5") // Kotlin DSL 지원

	// https://mvnrepository.com/artifact/io.kotest/kotest-assertions-core-jvm
	val kotestVersion = "5.9.1"
	testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
	testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
	testImplementation("io.kotest:kotest-framework-datatest:$kotestVersion")
	testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")

	// https://mvnrepository.com/artifact/org.dbunit/dbunit
	testImplementation("org.dbunit:dbunit:3.0.0")

	// https://mvnrepository.com/artifact/org.mapstruct/mapstruct
	implementation("org.mapstruct:mapstruct:1.6.3")
	// https://mvnrepository.com/artifact/org.mapstruct/mapstruct-processor
	//ksp 미지원
	kapt("org.mapstruct:mapstruct-processor:1.6.3")
	kaptTest("org.mapstruct:mapstruct-processor:1.6.3")

	// https://mvnrepository.com/artifact/com.github.gavlyukovskiy/p6spy-spring-boot-starter
	testImplementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.11.0")


	implementation(platform("com.fasterxml.jackson:jackson-bom:2.15.2"))
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
