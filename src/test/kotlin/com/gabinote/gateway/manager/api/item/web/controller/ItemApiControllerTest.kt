package com.gabinote.gateway.manager.api.item.web.controller

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.fasterxml.jackson.databind.ObjectMapper
import com.gabinote.api.testSupport.testUtil.objectMapper.TestObjectMapperExtension.toMatcher
import com.gabinote.gateway.manager.api.common.dto.page.controller.PagedResControllerDto
import com.gabinote.gateway.manager.api.common.dto.sort.controller.SortResControllerDto
import com.gabinote.gateway.manager.api.common.mapper.PageMapper
import com.gabinote.gateway.manager.api.item.dto.controller.ItemCreateReqControllerDto
import com.gabinote.gateway.manager.api.item.dto.controller.ItemResControllerDto
import com.gabinote.gateway.manager.api.item.dto.controller.ItemUpdateReqControllerDto
import com.gabinote.gateway.manager.api.item.dto.service.ItemCreateReqServiceDto
import com.gabinote.gateway.manager.api.item.dto.service.ItemResServiceDto
import com.gabinote.gateway.manager.api.item.dto.service.ItemUpdateReqServiceDto
import com.gabinote.gateway.manager.api.item.mapper.ItemMapper
import com.gabinote.gateway.manager.api.item.service.ItemService
import com.gabinote.gateway.manager.api.testSupport.testTemplate.WebMvcTestTemplate
import com.gabinote.gateway.manager.api.testSupport.testUtil.docs.schema.common.PagedDocsSchema
import com.gabinote.gateway.manager.api.testSupport.testUtil.docs.schema.item.ItemDocsSchema
import com.gabinote.gateway.manager.api.testSupport.testUtil.page.TestPageableUtil
import com.gabinote.gateway.manager.api.testSupport.testUtil.page.TestPageableUtil.toPage
import com.ninjasquad.springmockk.MockkBean
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(
    controllers = [ItemApiController::class],
    excludeAutoConfiguration = [
        OAuth2ClientAutoConfiguration::class,
        OAuth2ResourceServerAutoConfiguration::class
    ]
)
class ItemApiControllerTest : WebMvcTestTemplate() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var pageMapper: PageMapper

    @MockkBean
    lateinit var itemService: ItemService

    @MockkBean
    lateinit var itemMapper: ItemMapper

    private val apiPrefix = "/api/v1/items"

    init {
        describe("[Item] ItemApiController") {

            describe("ItemApiController.getItems") {
                context("올바른 Pageable로 요청하면") {
                    val pageable = TestPageableUtil.createPageable(
                        size = 1,
                        sortKey = "id",
                        sortDirection = Sort.Direction.DESC,
                        page = 0
                    )
                    val target = mockk<ItemResServiceDto> {}
                    val pagedTarget = listOf(target).toPage(pageable)

                    val expectedItem = ItemResControllerDto(
                        id = 1L,
                        name = "testItem",
                        url = "http://test.com",
                        port = 8080,
                        prefix = "test"
                    )

                    val pagedExpectedItem = listOf(expectedItem).toPage(pageable)

                    val expectedResponse = PagedResControllerDto(
                        content = mutableListOf(expectedItem),
                        page = pageable.pageNumber,
                        size = pageable.pageSize,
                        totalElements = 1,
                        totalPages = 1,
                        sortKey = listOf(
                            SortResControllerDto(
                                key = "id",
                                direction = "desc"
                            )
                        )
                    )

                    beforeTest {
                        every { itemService.getAll(pageable) } returns pagedTarget
                        every { itemMapper.toItemResControllerDto(target) } returns expectedItem
                        every { pageMapper.toPagedResponse(pagedExpectedItem) } returns expectedResponse
                    }

                    it("200 OK 응답을 반환하고, 올바른 PagedResControllerDto<ItemResControllerDto>를 포함한다.") {
                        mockMvc.perform(
                            RestDocumentationRequestBuilders.get(apiPrefix)
                                .param("page", pageable.pageNumber.toString())
                                .param("size", pageable.pageSize.toString())
                                .param("sort", "id,desc")
                        )
                            .andExpect(status().isOk)
                            .andExpect(objectMapper.toMatcher(expectedResponse))
                            .andDo(
                                document(
                                    "items/getItems",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("Item")
                                            .description("모든 아이템을 페이징하여 조회")
                                            .queryParameters(
                                                parameterWithName("page").description("페이지 번호 (0부터 시작, 기본값 0)")
                                                    .optional(),
                                                parameterWithName("size").description("페이지 크기 (최대 100, 기본값 20)")
                                                    .optional(),
                                                parameterWithName("sort").description("정렬 기준 및 방향 (id, name, port, prefix) ex) sort=id,desc (기본값 id,desc)")
                                                    .optional()
                                            )
                                            .responseFields(
                                                *PagedDocsSchema.toPageSchema(
                                                    arrayOf(
                                                        fieldWithPath("content[].id").description("아이템의 ID"),
                                                        fieldWithPath("content[].name").description("아이템의 이름"),
                                                        fieldWithPath("content[].url").description("아이템의 URL"),
                                                        fieldWithPath("content[].port").description("아이템의 포트"),
                                                        fieldWithPath("content[].prefix").description("아이템의 프리픽스")
                                                            .optional()
                                                    )
                                                )
                                            )
                                            .responseSchema(Schema("ItemPageResponse"))
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) { itemService.getAll(pageable) }
                        verify(exactly = 1) { itemMapper.toItemResControllerDto(target) }
                        verify(exactly = 1) { pageMapper.toPagedResponse(pagedExpectedItem) }
                    }
                }

                context("최대 페이징 크기를 초과하는 요청을 하면") {
                    val pageable = TestPageableUtil.createPageable(
                        size = 101,
                        sortKey = "id",
                        sortDirection = Sort.Direction.DESC,
                        page = 0
                    )

                    it("400 Bad Request 응답을 반환한다.") {
                        mockMvc.perform(
                            RestDocumentationRequestBuilders.get(apiPrefix)
                                .param("page", pageable.pageNumber.toString())
                                .param("size", pageable.pageSize.toString())
                                .param("sort", "id,desc")
                        )
                            .andExpect(status().isBadRequest)
                    }
                }

                context("잘못된 정렬 키로 요청하면") {
                    val pageable = TestPageableUtil.createPageable(
                        size = 1,
                        sortKey = "invalidKey",
                        sortDirection = Sort.Direction.DESC,
                        page = 0
                    )

                    it("400 Bad Request 응답을 반환한다.") {
                        mockMvc.perform(
                            RestDocumentationRequestBuilders.get(apiPrefix)
                                .param("page", pageable.pageNumber.toString())
                                .param("size", pageable.pageSize.toString())
                                .param("sort", "invalidKey,desc")
                        )
                            .andExpect(status().isBadRequest)
                    }
                }
            }

            describe("ItemApiController.createItem") {
                context("올바른 요청 본문으로 요청하면") {
                    val validCreateDto = ItemCreateReqControllerDto(
                        name = "testItem",
                        url = "http://test.com",
                        port = 8080,
                        prefix = "test"
                    )

                    val createDto = mockk<ItemCreateReqServiceDto> {}
                    val createdData = mockk<ItemResServiceDto> {}

                    val expectedResponse = ItemResControllerDto(
                        id = 1L,
                        name = validCreateDto.name,
                        url = validCreateDto.url,
                        port = validCreateDto.port,
                        prefix = validCreateDto.prefix
                    )

                    beforeTest {
                        every { itemMapper.toItemCreateReqServiceDto(validCreateDto) } returns createDto
                        every { itemService.create(createDto) } returns createdData
                        every { itemMapper.toItemResControllerDto(createdData) } returns expectedResponse
                    }

                    it("201 Created 응답을 반환하고, 올바른 ItemResControllerDto를 포함한다.") {
                        mockMvc.perform(
                            RestDocumentationRequestBuilders.post(apiPrefix)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validCreateDto))
                        )
                            .andExpect(status().isCreated)
                            .andExpect(objectMapper.toMatcher(expectedResponse))
                            .andDo(
                                document(
                                    "items/createItem",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("Item")
                                            .description("새로운 아이템을 생성")
                                            .requestFields(
                                                fieldWithPath("name").description("아이템의 이름 (1-255자)"),
                                                fieldWithPath("url").description("아이템의 URL (1-2048자)"),
                                                fieldWithPath("port").description("아이템의 포트 (0-65535)"),
                                                fieldWithPath("prefix").description("아이템의 프리픽스 (1-255자)").optional()
                                            )
                                            .responseFields(
                                                *ItemDocsSchema.itemResponseSchema
                                            )
                                            .responseSchema(Schema("ItemResponse"))
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) { itemMapper.toItemCreateReqServiceDto(validCreateDto) }
                        verify(exactly = 1) { itemService.create(createDto) }
                        verify(exactly = 1) { itemMapper.toItemResControllerDto(createdData) }
                    }
                }

                describe("유효하지 않은 본문으로 요청할 때") {
                    table(
                        headers("fieldName", "value", "expectedMessage", "reason"),
                        row(
                            "name",
                            ItemCreateReqControllerDto(
                                name = "",
                                url = "http://test.com",
                                port = 8080
                            ),
                            "Name must be between 1 and 255 characters",
                            "name이 빈 문자열인 요청이 들어온 경우"
                        ),
                        row(
                            "url",
                            ItemCreateReqControllerDto(
                                name = "testItem",
                                url = "",
                                port = 8080
                            ),
                            "URL must be between 1 and 2048 characters",
                            "url이 빈 문자열인 요청이 들어온 경우"
                        ),
                        row(
                            "port",
                            ItemCreateReqControllerDto(
                                name = "testItem",
                                url = "http://test.com",
                                port = 65536
                            ),
                            "Port is out of range",
                            "port가 최대값을 초과하는 요청이 들어온 경우"
                        )
                    ).forAll { fieldName, value, expectedMessage, reason ->
                        context(reason) {
                            it("$expectedMessage 에러 메시지를 포함하는 400 Bad Request 응답을 반환한다.") {
                                mockMvc.perform(
                                    RestDocumentationRequestBuilders.post(apiPrefix)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(value))
                                )
                                    .andDo(MockMvcResultHandlers.print())
                                    .andExpect(status().isBadRequest)
                            }
                        }
                    }
                }


            }

            describe("ItemApiController.updateItem") {
                context("올바른 ID와 유효한 요청 본문으로 요청하면") {
                    val validId = 1L
                    val validUpdateDto = ItemUpdateReqControllerDto(
                        name = "updatedItem",
                        url = "http://updated.com",
                        port = 9090,
                        prefix = "updated"
                    )

                    val updateDto = mockk<ItemUpdateReqServiceDto> {}
                    val updatedData = mockk<ItemResServiceDto> {}

                    val expectedResponse = ItemResControllerDto(
                        id = validId,
                        name = validUpdateDto.name!!,
                        url = validUpdateDto.url!!,
                        port = validUpdateDto.port!!,
                        prefix = validUpdateDto.prefix
                    )

                    beforeTest {
                        every {
                            itemMapper.toItemUpdateReqServiceDto(
                                validUpdateDto,
                                validId
                            )
                        } returns updateDto
                        every { itemService.update(updateDto) } returns updatedData
                        every { itemMapper.toItemResControllerDto(updatedData) } returns expectedResponse
                    }

                    it("200 OK 응답을 반환하고, 올바른 ItemResControllerDto를 포함한다.") {
                        mockMvc.perform(
                            RestDocumentationRequestBuilders.put("$apiPrefix/{id}", validId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validUpdateDto))
                        )
                            .andExpect(status().isOk)
                            .andExpect(objectMapper.toMatcher(expectedResponse))
                            .andDo(
                                document(
                                    "items/updateItem",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("Item")
                                            .description("아이템을 ID로 업데이트")
                                            .pathParameters(
                                                parameterWithName("id").description("업데이트할 아이템의 ID")
                                            )
                                            .requestFields(
                                                fieldWithPath("name").description("아이템의 이름 (1-255자)").optional(),
                                                fieldWithPath("url").description("아이템의 URL (1-2048자)").optional(),
                                                fieldWithPath("port").description("아이템의 포트 (0-65535)").optional(),
                                                fieldWithPath("prefix").description("아이템의 프리픽스 (1-255자)").optional()
                                            )
                                            .responseFields(
                                                *ItemDocsSchema.itemResponseSchema
                                            )
                                            .responseSchema(Schema("ItemResponse"))
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) {
                            itemMapper.toItemUpdateReqServiceDto(
                                validUpdateDto,
                                validId
                            )
                        }
                        verify(exactly = 1) { itemService.update(updateDto) }
                        verify(exactly = 1) { itemMapper.toItemResControllerDto(updatedData) }
                    }
                }

                describe("일부 필드만 변경하는 요청이 들어올 때") {
                    table(
                        headers("value", "reason"),
                        row(
                            ItemUpdateReqControllerDto(
                                name = "updatedName"
                            ),
                            "name만 변경하는 요청이 들어온 경우"
                        ),
                        row(
                            ItemUpdateReqControllerDto(
                                url = "http://updated.com"
                            ),
                            "url만 변경하는 요청이 들어온 경우"
                        ),
                        row(
                            ItemUpdateReqControllerDto(
                                port = 9090
                            ),
                            "port만 변경하는 요청이 들어온 경우"
                        ),
                        row(
                            ItemUpdateReqControllerDto(
                                prefix = "updated"
                            ),
                            "prefix만 변경하는 요청이 들어온 경우"
                        )
                    ).forAll { value, reason ->
                        context(reason) {
                            val validId = 1L
                            val updateDto = mockk<ItemUpdateReqServiceDto> {}
                            val updatedData = mockk<ItemResServiceDto> {}

                            val expectedResponse = ItemResControllerDto(
                                id = validId,
                                name = value.name ?: "testName",
                                url = value.url ?: "http://test.com",
                                port = value.port ?: 8080,
                                prefix = value.prefix
                            )

                            beforeTest {
                                every {
                                    itemMapper.toItemUpdateReqServiceDto(
                                        value,
                                        validId
                                    )
                                } returns updateDto
                                every { itemService.update(updateDto) } returns updatedData
                                every { itemMapper.toItemResControllerDto(updatedData) } returns expectedResponse
                            }

                            it("200 OK 응답을 반환한다.") {
                                mockMvc.perform(
                                    RestDocumentationRequestBuilders.put("$apiPrefix/{id}", validId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(value))
                                )
                                    .andExpect(status().isOk)

                                verify(exactly = 1) {
                                    itemMapper.toItemUpdateReqServiceDto(
                                        value,
                                        validId
                                    )
                                }
                                verify(exactly = 1) { itemService.update(updateDto) }
                                verify(exactly = 1) {
                                    itemMapper.toItemResControllerDto(
                                        updatedData
                                    )
                                }
                            }
                        }
                    }
                }

            }

            describe("ItemApiController.deleteItem") {
                context("올바른 ID로 요청하면") {
                    val validId = 1L

                    beforeTest {
                        every { itemService.delete(validId) } returns Unit
                    }

                    it("204 No Content 응답을 반환한다.") {
                        mockMvc.perform(
                            RestDocumentationRequestBuilders.delete("$apiPrefix/{id}", validId)
                        )
                            .andExpect(status().isNoContent)
                            .andDo(
                                document(
                                    "items/deleteItem",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("Item")
                                            .description("아이템을 ID로 삭제")
                                            .pathParameters(
                                                parameterWithName("id").description("삭제할 아이템의 ID")
                                            )
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) { itemService.delete(validId) }
                    }
                }
            }
        }
    }
}
