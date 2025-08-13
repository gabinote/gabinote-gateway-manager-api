package com.gabinote.gateway.manager.api.path.web.controller

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
import com.gabinote.gateway.manager.api.item.dto.controller.ItemResControllerDto
import com.gabinote.gateway.manager.api.path.domain.HttpMethodType
import com.gabinote.gateway.manager.api.path.dto.controller.PathCreateReqControllerDto
import com.gabinote.gateway.manager.api.path.dto.controller.PathResControllerDto
import com.gabinote.gateway.manager.api.path.dto.controller.PathUpdateReqControllerDto
import com.gabinote.gateway.manager.api.path.dto.service.PathCreateReqServiceDto
import com.gabinote.gateway.manager.api.path.dto.service.PathResServiceDto
import com.gabinote.gateway.manager.api.path.dto.service.PathUpdateReqServiceDto
import com.gabinote.gateway.manager.api.path.mapper.PathMapper
import com.gabinote.gateway.manager.api.path.service.PathService
import com.gabinote.gateway.manager.api.testSupport.testTemplate.WebMvcTestTemplate
import com.gabinote.gateway.manager.api.testSupport.testUtil.docs.schema.path.PathDocsSchema
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
    controllers = [PathApiController::class],
    excludeAutoConfiguration = [
        OAuth2ClientAutoConfiguration::class,
        OAuth2ResourceServerAutoConfiguration::class
    ]
)
class PathApiControllerTest : WebMvcTestTemplate() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var pageMapper: PageMapper

    @MockkBean
    lateinit var pathService: PathService

    @MockkBean
    lateinit var pathMapper: PathMapper

    private val apiPrefix = "/api/v1"

    init {
        describe("[Path] PathApiController") {

            describe("PathApiController.getPaths") {
                context("올바른 itemId와 Pageable로 요청하면") {
                    val itemId = 1L
                    val pageable = TestPageableUtil.createPageable(
                        size = 1,
                        sortKey = "id",
                        sortDirection = Sort.Direction.DESC,
                        page = 0
                    )
                    val target = mockk<PathResServiceDto> {}
                    val pagedTarget = listOf(target).toPage(pageable)

                    val expectedItem = ItemResControllerDto(
                        id = itemId,
                        name = "testItem",
                        url = "http://test.com",
                        port = 8080,
                        prefix = "test"
                    )

                    val expectedPath = PathResControllerDto(
                        id = 1L,
                        path = "/test",
                        priority = 1,
                        enableAuth = true,
                        role = "USER",
                        httpMethod = HttpMethodType.GET,
                        enabled = true,
                        item = expectedItem
                    )

                    val pagedExpectedPath = listOf(expectedPath).toPage(pageable)

                    val expectedResponse = PagedResControllerDto(
                        content = mutableListOf(expectedPath),
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
                        every { pathService.getAllByItem(itemId, pageable) } returns pagedTarget
                        every { pathMapper.toPathResControllerDto(target) } returns expectedPath
                        every { pageMapper.toPagedResponse(pagedExpectedPath) } returns expectedResponse
                    }

                    it("200 OK 응답을 반환하고, 올바른 PagedResControllerDto<PathResControllerDto>를 포함한다.") {
                        mockMvc.perform(
                            RestDocumentationRequestBuilders.get("$apiPrefix/items/{itemId}/paths", itemId)
                                .param("page", pageable.pageNumber.toString())
                                .param("size", pageable.pageSize.toString())
                                .param("sort", "id,desc")
                        )
                            .andExpect(status().isOk)
                            .andExpect(objectMapper.toMatcher(expectedResponse))
                            .andDo(
                                document(
                                    "paths/getPaths",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("Path")
                                            .description("특정 아이템의 모든 경로를 페이징하여 조회")
                                            .pathParameters(
                                                parameterWithName("itemId").description("경로를 조회할 아이템의 ID")
                                            )
                                            .queryParameters(
                                                parameterWithName("page").description("페이지 번호 (0부터 시작, 기본값 0)")
                                                    .optional(),
                                                parameterWithName("size").description("페이지 크기 (최대 100, 기본값 20)")
                                                    .optional(),
                                                parameterWithName("sort").description("정렬 기준 및 방향 (id, path, priority, role) ex) sort=id,desc (기본값 id,desc)")
                                                    .optional()
                                            )
                                            .responseFields(
                                                *PathDocsSchema.pathPagedResponseSchema
                                            )
                                            .responseSchema(Schema("PathPageResponse"))
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) { pathService.getAllByItem(itemId, pageable) }
                        verify(exactly = 1) { pathMapper.toPathResControllerDto(target) }
                        verify(exactly = 1) { pageMapper.toPagedResponse(pagedExpectedPath) }
                    }
                }

                context("최대 페이징 크기를 초과하는 요청을 하면") {
                    val itemId = 1L
                    val pageable = TestPageableUtil.createPageable(
                        size = 101,
                        sortKey = "id",
                        sortDirection = Sort.Direction.DESC,
                        page = 0
                    )

                    it("400 Bad Request 응답을 반환한다.") {
                        mockMvc.perform(
                            RestDocumentationRequestBuilders.get("$apiPrefix/items/{itemId}/paths", itemId)
                                .param("page", pageable.pageNumber.toString())
                                .param("size", pageable.pageSize.toString())
                                .param("sort", "id,desc")
                        )
                            .andExpect(status().isBadRequest)
                    }
                }

                context("잘못된 정렬 키로 요청하면") {
                    val itemId = 1L
                    val pageable = TestPageableUtil.createPageable(
                        size = 1,
                        sortKey = "invalidKey",
                        sortDirection = Sort.Direction.DESC,
                        page = 0
                    )

                    it("400 Bad Request 응답을 반환한다.") {
                        mockMvc.perform(
                            RestDocumentationRequestBuilders.get("$apiPrefix/items/{itemId}/paths", itemId)
                                .param("page", pageable.pageNumber.toString())
                                .param("size", pageable.pageSize.toString())
                                .param("sort", "invalidKey,desc")
                        )
                            .andExpect(status().isBadRequest)
                    }
                }
            }

            describe("PathApiController.createPath") {
                context("올바른 요청 본문으로 요청하면") {
                    val validCreateDto = PathCreateReqControllerDto(
                        path = "/test",
                        priority = 1,
                        enableAuth = true,
                        role = "USER",
                        httpMethod = HttpMethodType.GET,
                        enabled = true,
                        itemId = 1L
                    )

                    val createDto = mockk<PathCreateReqServiceDto> {}
                    val createdData = mockk<PathResServiceDto> {}

                    val expectedItem = ItemResControllerDto(
                        id = 1L,
                        name = "testItem",
                        url = "http://test.com",
                        port = 8080,
                        prefix = "test"
                    )

                    val expectedResponse = PathResControllerDto(
                        id = 1L,
                        path = validCreateDto.path,
                        priority = validCreateDto.priority,
                        enableAuth = validCreateDto.enableAuth,
                        role = validCreateDto.role,
                        httpMethod = validCreateDto.httpMethod,
                        enabled = validCreateDto.enabled,
                        item = expectedItem
                    )

                    beforeTest {
                        every { pathMapper.toPathCreateReqServiceDto(validCreateDto) } returns createDto
                        every { pathService.create(createDto) } returns createdData
                        every { pathMapper.toPathResControllerDto(createdData) } returns expectedResponse
                    }

                    it("201 Created 응답을 반환하고, 올바른 PathResControllerDto를 포함한다.") {
                        mockMvc.perform(
                            RestDocumentationRequestBuilders.post("$apiPrefix/paths")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validCreateDto))
                        )
                            .andExpect(status().isCreated)
                            .andExpect(objectMapper.toMatcher(expectedResponse))
                            .andDo(
                                document(
                                    "paths/createPath",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("Path")
                                            .description("새로운 경로를 생성")
                                            .requestFields(
                                                fieldWithPath("path").description("경로 (1-255자)"),
                                                fieldWithPath("priority").description("우선순위 (최대 2147483647)"),
                                                fieldWithPath("enable_auth").description("인증 활성화 여부"),
                                                fieldWithPath("role").description("역할 (1-255자)").optional(),
                                                fieldWithPath("http_method").description("HTTP 메서드"),
                                                fieldWithPath("enabled").description("활성화 여부"),
                                                fieldWithPath("item_id").description("아이템 ID (양수)")
                                            )
                                            .responseFields(
                                                *PathDocsSchema.pathResponseSchema
                                            )
                                            .responseSchema(Schema("PathResponse"))
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) { pathMapper.toPathCreateReqServiceDto(validCreateDto) }
                        verify(exactly = 1) { pathService.create(createDto) }
                        verify(exactly = 1) { pathMapper.toPathResControllerDto(createdData) }
                    }
                }

                describe("유효하지 않은 요청 본문으로 요청할 때") {
                    table(
                        headers("fieldName", "value", "expectedMessage", "reason"),
                        row(
                            "path",
                            PathCreateReqControllerDto(
                                path = "",
                                priority = 1,
                                enableAuth = true,
                                httpMethod = HttpMethodType.GET,
                                enabled = true,
                                itemId = 1L
                            ),
                            "Path must be between 1 and 255 characters",
                            "path가 빈 문자열인 요청이 들어온 경우"
                        ),
                        row(
                            "role",
                            PathCreateReqControllerDto(
                                path = "/test",
                                priority = 1,
                                enableAuth = true,
                                role = "",
                                httpMethod = HttpMethodType.GET,
                                enabled = true,
                                itemId = 1L
                            ),
                            "Role must be between 1 and 255 characters",
                            "role가 빈 문자열인 요청이 들어온 경우"
                        ),
                        row(
                            "itemId",
                            PathCreateReqControllerDto(
                                path = "/test",
                                priority = 1,
                                enableAuth = true,
                                httpMethod = HttpMethodType.GET,
                                enabled = true,
                                itemId = 0L
                            ),
                            "Item ID must be a positive number",
                            "itemId가 0인 요청이 들어온 경우"
                        )
                    ).forAll { fieldName, value, expectedMessage, reason ->
                        context(reason) {
                            it("$expectedMessage 에러 메시지를 포함하는 400 Bad Request 응답을 반환한다.") {
                                mockMvc.perform(
                                    RestDocumentationRequestBuilders.post("$apiPrefix/paths")
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

            describe("PathApiController.updatePath") {
                context("올바른 ID와 유효한 요청 본문으로 요청하면") {
                    val validId = 1L
                    val validUpdateDto = PathUpdateReqControllerDto(
                        path = "/updated",
                        priority = 2,
                        enableAuth = false,
                        role = "ADMIN",
                        httpMethod = HttpMethodType.POST,
                        enabled = false
                    )

                    val updateDto = mockk<PathUpdateReqServiceDto> {}
                    val updatedData = mockk<PathResServiceDto> {}

                    val expectedItem = ItemResControllerDto(
                        id = 1L,
                        name = "testItem",
                        url = "http://test.com",
                        port = 8080,
                        prefix = "test"
                    )

                    val expectedResponse = PathResControllerDto(
                        id = validId,
                        path = validUpdateDto.path!!,
                        priority = validUpdateDto.priority!!,
                        enableAuth = validUpdateDto.enableAuth!!,
                        role = validUpdateDto.role,
                        httpMethod = validUpdateDto.httpMethod!!,
                        enabled = validUpdateDto.enabled!!,
                        item = expectedItem
                    )

                    beforeTest {
                        every {
                            pathMapper.toPathUpdateReqServiceDto(
                                validUpdateDto,
                                validId
                            )
                        } returns updateDto
                        every { pathService.update(updateDto) } returns updatedData
                        every { pathMapper.toPathResControllerDto(updatedData) } returns expectedResponse
                    }

                    it("200 OK 응답을 반환하고, 올바른 PathResControllerDto를 포함한다.") {
                        mockMvc.perform(
                            RestDocumentationRequestBuilders.put("$apiPrefix/paths/{pathId}", validId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validUpdateDto))
                        )
                            .andExpect(status().isOk)
                            .andExpect(objectMapper.toMatcher(expectedResponse))
                            .andDo(
                                document(
                                    "paths/updatePath",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("Path")
                                            .description("경로를 ID로 업데이트")
                                            .pathParameters(
                                                parameterWithName("pathId").description("업데이트할 경로의 ID")
                                            )
                                            .requestFields(
                                                fieldWithPath("path").description("경로 (1-255자)"),
                                                fieldWithPath("priority").description("우선순위 (최대 2147483647)"),
                                                fieldWithPath("enable_auth").description("인증 활성화 여부"),
                                                fieldWithPath("role").description("역할 (1-255자)").optional(),
                                                fieldWithPath("http_method").description("HTTP 메서드"),
                                                fieldWithPath("enabled").description("활성화 여부")
                                            )
                                            .responseFields(
                                                *PathDocsSchema.pathResponseSchema
                                            )
                                            .responseSchema(Schema("PathResponse"))
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) {
                            pathMapper.toPathUpdateReqServiceDto(
                                validUpdateDto,
                                validId
                            )
                        }
                        verify(exactly = 1) { pathService.update(updateDto) }
                        verify(exactly = 1) { pathMapper.toPathResControllerDto(updatedData) }
                    }
                }

                describe("일부분 만 변경하는 요청을 할 때") {
                    table(
                        headers("value", "reason"),
                        row(
                            PathUpdateReqControllerDto(
                                path = "/updatedPath"
                            ),
                            "path만 변경하는 요청이 들어온 경우"
                        ),
                        row(
                            PathUpdateReqControllerDto(
                                priority = 5
                            ),
                            "priority만 변경하는 요청이 들어온 경우"
                        ),
                        row(
                            PathUpdateReqControllerDto(
                                enableAuth = false
                            ),
                            "enableAuth만 변경하는 요청이 들어온 경우"
                        ),
                        row(
                            PathUpdateReqControllerDto(
                                role = "GUEST"
                            ),
                            "role만 변경하는 요청이 들어온 경우"
                        ),
                        row(
                            PathUpdateReqControllerDto(
                                httpMethod = HttpMethodType.DELETE
                            ),
                            "httpMethod만 변경하는 요청이 들어온 경우"
                        ),
                        row(
                            PathUpdateReqControllerDto(
                                enabled = false
                            ),
                            "enabled만 변경하는 요청이 들어온 경우"
                        )
                    ).forAll { value, reason ->
                        context(reason) {
                            val validId = 1L
                            val updateDto = mockk<PathUpdateReqServiceDto> {}
                            val updatedData = mockk<PathResServiceDto> {}

                            val expectedItem = ItemResControllerDto(
                                id = 1L,
                                name = "testItem",
                                url = "http://test.com",
                                port = 8080,
                                prefix = "test"
                            )

                            val expectedResponse = PathResControllerDto(
                                id = validId,
                                path = value.path ?: "/test",
                                priority = value.priority ?: 1,
                                enableAuth = value.enableAuth ?: true,
                                role = value.role,
                                httpMethod = value.httpMethod ?: HttpMethodType.GET,
                                enabled = value.enabled ?: true,
                                item = expectedItem
                            )

                            beforeTest {
                                every {
                                    pathMapper.toPathUpdateReqServiceDto(
                                        value,
                                        validId
                                    )
                                } returns updateDto
                                every { pathService.update(updateDto) } returns updatedData
                                every { pathMapper.toPathResControllerDto(updatedData) } returns expectedResponse
                            }

                            it("200 OK 응답을 반환한다.") {
                                mockMvc.perform(
                                    RestDocumentationRequestBuilders.put("$apiPrefix/paths/{pathId}", validId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(value))
                                )
                                    .andExpect(status().isOk)

                                verify(exactly = 1) {
                                    pathMapper.toPathUpdateReqServiceDto(
                                        value,
                                        validId
                                    )
                                }
                                verify(exactly = 1) { pathService.update(updateDto) }
                                verify(exactly = 1) {
                                    pathMapper.toPathResControllerDto(
                                        updatedData
                                    )
                                }
                            }
                        }
                    }
                }
            }

            describe("PathApiController.deletePath") {
                context("올바른 ID로 요청하면") {
                    val validId = 1L

                    beforeTest {
                        every { pathService.delete(validId) } returns Unit
                    }

                    it("204 No Content 응답을 반환한다.") {
                        mockMvc.perform(
                            RestDocumentationRequestBuilders.delete("$apiPrefix/paths/{pathId}", validId)
                        )
                            .andExpect(status().isNoContent)
                            .andDo(
                                document(
                                    "paths/deletePath",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("Path")
                                            .description("경로를 ID로 삭제")
                                            .pathParameters(
                                                parameterWithName("pathId").description("삭제할 경로의 ID")
                                            )
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) { pathService.delete(validId) }
                    }
                }
            }
        }
    }
}
