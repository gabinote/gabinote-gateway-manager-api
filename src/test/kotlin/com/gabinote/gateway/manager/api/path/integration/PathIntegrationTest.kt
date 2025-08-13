package com.gabinote.gateway.manager.api.path.integration

import com.gabinote.gateway.manager.api.testSupport.testTemplate.IntegrationTestTemplate
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue

class PathIntegrationTest : IntegrationTestTemplate() {
    override val apiPrefix: String = "/api/v1"

    init {
        feature("[Path] Path API Integration Tests") {
            feature("[GET] /api/v1/items/{itemId}/paths") {
                scenario("올바른 요청이 들어오면 경로 목록을 반환한다") {
                    Given {
                        dbUnitTestHelper.loadDataSets(
                            "dataset/item/integration/base-item.xml",
                            "dataset/path/integration/base-path.xml"
                        )
                        port(port)
                        basePath(apiPrefix)
                        accept("application/json")
                        contentType("application/json")
                    }.When {
                        get("/items/1/paths")
                    }.Then {
                        statusCode(200)
                        body(
                            "content.size()", equalTo(2),

                            "content[0].id", equalTo(2),
                            "content[0].path", equalTo("/api/v2/test"),
                            "content[0].priority", equalTo(0),
                            "content[0].enable_auth", equalTo(true),
                            "content[0].role", equalTo("ROLE_ADMIN"),
                            "content[0].http_method", equalTo("GET"),
                            "content[0].enabled", equalTo(true),
                            "content[0].item.id", equalTo(1),
                            "content[0].item.name", equalTo("test"),
                            "content[0].item.url", equalTo("http://test.com"),
                            "content[0].item.prefix", equalTo("test1"),
                            "content[0].item.port", equalTo(123),

                            "content[1].id", equalTo(1),
                            "content[1].path", equalTo("/api/v1/test"),
                            "content[1].priority", equalTo(1),
                            "content[1].enable_auth", equalTo(false),
                            "content[1].role", nullValue(),
                            "content[1].http_method", equalTo("POST"),
                            "content[1].enabled", equalTo(true),
                            "content[1].item.id", equalTo(1),
                            "content[1].item.name", equalTo("test"),
                            "content[1].item.url", equalTo("http://test.com"),
                            "content[1].item.prefix", equalTo("test1"),
                            "content[1].item.port", equalTo(123),
                        )
                    }
                }
            }

            feature("[POST] /api/v1/paths") {
                scenario("올바른 요청이 들어오면 경로를 생성한다") {
                    Given {
                        dbUnitTestHelper.loadDataSets(
                            "dataset/item/integration/base-item.xml",
                            "dataset/path/integration/base-path.xml"
                        )
                        port(port)
                        basePath(apiPrefix)
                        accept("application/json")
                        contentType("application/json")
                    }.When {
                        body(
                            """
                            {
                                "path": "/api/v2/test",
                                "priority": 0,
                                "enable_auth": true,
                                "role": "ROLE_ADMIN",
                                "http_method": "GET",
                                "enabled": true,
                                "item_id": 1
                            }
                            """.trimIndent()
                        )
                        post("/paths")
                    }.Then {
                        statusCode(201)
                        body(
                            "id", equalTo(3),
                            "path", equalTo("/api/v2/test"),
                            "priority", equalTo(0),
                            "enable_auth", equalTo(true),
                            "role", equalTo("ROLE_ADMIN"),
                            "http_method", equalTo("GET"),
                            "enabled", equalTo(true),
                            "item.id", equalTo(1),
                            "item.name", equalTo("test"),
                            "item.url", equalTo("http://test.com"),
                            "item.prefix", equalTo("test1"),
                            "item.port", equalTo(123)
                        )
                    }
                }
            }

            feature("[PUT] /api/v1/paths/{pathId}") {
                scenario("올바른 요청이 들어오면 경로를 수정한다") {
                    Given {
                        dbUnitTestHelper.loadDataSets(
                            "dataset/item/integration/base-item.xml",
                            "dataset/path/integration/base-path.xml"
                        )
                        port(port)
                        basePath(apiPrefix)
                        accept("application/json")
                        contentType("application/json")
                    }.When {
                        body(
                            """
                            {
                                "path": "/api/v2/updated-test",
                                "priority": 1,
                                "enable_auth": false,
                                "role": null,
                                "http_method": "POST"
                            }
                            """.trimIndent()
                        )
                        put("/paths/1")
                    }.Then {
                        statusCode(200)
                        body(
                            "id", equalTo(1),
                            "path", equalTo("/api/v2/updated-test"),
                            "priority", equalTo(1),
                            "enable_auth", equalTo(false),
                            "role", nullValue(),
                            "http_method", equalTo("POST"),
                            "enabled", equalTo(true),
                            "item.id", equalTo(1),
                            "item.name", equalTo("test"),
                            "item.url", equalTo("http://test.com"),
                            "item.prefix", equalTo("test1"),
                            "item.port", equalTo(123)
                        )
                    }
                }
            }

            feature("[DELETE] /api/v1/paths/{pathId}") {
                scenario("올바른 요청이 들어오면 경로를 삭제한다") {
                    Given {
                        dbUnitTestHelper.loadDataSets(
                            "dataset/item/integration/base-item.xml",
                            "dataset/path/integration/base-path.xml"
                        )
                        port(port)
                        basePath(apiPrefix)
                        accept("application/json")
                        contentType("application/json")
                    }.When {
                        delete("/paths/1")
                    }.Then {
                        statusCode(204)
                    }
                }
            }
        }
    }
}