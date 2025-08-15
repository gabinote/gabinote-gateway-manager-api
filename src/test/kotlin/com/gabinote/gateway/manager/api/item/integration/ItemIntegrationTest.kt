package com.gabinote.gateway.manager.api.item.integration

import com.gabinote.gateway.manager.api.testSupport.testTemplate.IntegrationTestTemplate
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan

class ItemIntegrationTest : IntegrationTestTemplate() {
    override val apiPrefix: String = "/api/v1/items"

    init {
        feature("[Item] Item Api Integration Test") {
            feature("[GET] /api/v1/items") {
                scenario("올바른 요청이 주어지면, 아이템 목록을 반환한다.") {
                    Given {
                        dbUnitTestHelper.loadDataSet("dataset/item/integration/base-item.xml")
                        port(port)
                        basePath(apiPrefix)
                        accept("application/json")
                    }.When {
                        get("")
                    }.Then {
                        statusCode(200)
                        body(
                            "content.size()", greaterThan(0),


                            "content[0].id", equalTo(2),
                            "content[0].name", equalTo("test2"),
                            "content[0].url", equalTo("http://test2.com"),
                            "content[0].port", equalTo(456),
                            "content[0].prefix", equalTo("test2"),

                            "content[1].id", equalTo(1),
                            "content[1].name", equalTo("test"),
                            "content[1].url", equalTo("http://test.com"),
                            "content[1].port", equalTo(123),
                            "content[1].prefix", equalTo("test1"),

                            )
                    }
                }
            }

            feature("[POST] /api/v1/items") {
                scenario("올바른 요청이 주어지면, 아이템을 생성한다.") {
                    Given {
                        port(port)
                        basePath(apiPrefix)
                        accept("application/json")
                        contentType("application/json")
                        body(
                            """
                            {
                                "name": "test3",
                                "url": "http://test3.com",
                                "port": 828,
                                "prefix": "test3"
                            }
                            """.trimIndent()
                        )
                    }.When {
                        post("")
                    }.Then {
                        statusCode(201)
                        body(
                            "id", equalTo(3),
                            "name", equalTo("test3"),
                            "url", equalTo("http://test3.com"),
                            "port", equalTo(828),
                            "prefix", equalTo("test3")
                        )

                        dbUnitTestHelper.assertDataset(
                            "dataset/item/integration/save-expected-item.xml",
                            txForceCommit = false
                        )
                    }
                }
            }

            feature("[PUT] /api/v1/items/{id}") {
                scenario("올바른 요청이 주어지면, 아이템을 수정한다.") {
                    Given {
                        dbUnitTestHelper.loadDataSet("dataset/item/integration/base-item.xml")
                        port(port)
                        basePath(apiPrefix)
                        accept("application/json")
                        contentType("application/json")
                        body(
                            """
                            {
                                "name": "new",
                                "url": "http://new.com",
                                "port": 828,
                                "prefix": "new"
                            }
                            """.trimIndent()
                        )
                    }.When {
                        put("/2")
                    }.Then {
                        statusCode(200)
                        body(
                            "id", equalTo(2),
                            "name", equalTo("new"),
                            "url", equalTo("http://new.com"),
                            "port", equalTo(828),
                            "prefix", equalTo("new")
                        )

                        dbUnitTestHelper.assertDataset(
                            "dataset/item/integration/update-expected-item.xml",
                            txForceCommit = false
                        )
                    }
                }
            }

            feature("[DELETE] /api/v1/items/{id}") {
                scenario("올바른 요청이 주어지면, 아이템을 삭제한다.") {
                    Given {
                        dbUnitTestHelper.loadDataSet("dataset/item/integration/base-item.xml")
                        port(port)
                        basePath(apiPrefix)
                        accept("application/json")
                    }.When {
                        delete("/2")
                    }.Then {
                        statusCode(204)

                        dbUnitTestHelper.assertDataset(
                            "dataset/item/integration/delete-expected-item.xml",
                            txForceCommit = false
                        )
                    }
                }
            }
        }
    }
}