package com.gabinote.gateway.manager.api.path.mapper


import com.gabinote.gateway.manager.api.item.domain.Item
import com.gabinote.gateway.manager.api.item.dto.controller.ItemResControllerDto
import com.gabinote.gateway.manager.api.item.dto.service.ItemResServiceDto
import com.gabinote.gateway.manager.api.item.mapper.ItemMapper
import com.gabinote.gateway.manager.api.item.mapper.ItemMapperImpl
import com.gabinote.gateway.manager.api.path.domain.HttpMethodType
import com.gabinote.gateway.manager.api.path.domain.Path
import com.gabinote.gateway.manager.api.path.dto.controller.PathCreateReqControllerDto
import com.gabinote.gateway.manager.api.path.dto.controller.PathUpdateReqControllerDto
import com.gabinote.gateway.manager.api.path.dto.service.PathCreateReqServiceDto
import com.gabinote.gateway.manager.api.path.dto.service.PathResServiceDto
import com.gabinote.gateway.manager.api.path.dto.service.PathUpdateReqServiceDto
import com.gabinote.gateway.manager.api.testSupport.testTemplate.MockkTestTemplate
import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(
    classes = [
        PathMapperImpl::class,
        ItemMapperImpl::class
    ]
)
class PathMapperTest : MockkTestTemplate() {

    @Autowired
    lateinit var pathMapper: PathMapper

    @MockkBean
    lateinit var itemMapper: ItemMapper


    init {
        describe("[Path] PathMapper") {

            describe("PathMapper.toPathResServiceDto") {
                context("Path 엔티티가 주어지면") {
                    val mockItem = mockk<Item>()
                    val mockItemResServiceDto = mockk<ItemResServiceDto>()
                    every { itemMapper.toItemResServiceDto(mockItem) } returns mockItemResServiceDto

                    val path = Path(
                        id = 100L,
                        path = "/test",
                        priority = 1,
                        enableAuth = true,
                        role = "ROLE_USER",
                        httpMethod = HttpMethodType.GET,
                        enabled = true,
                        item = mockItem
                    )


                    it("PathResServiceDto로 올바르게 변환되어야 한다") {
                        val result = pathMapper.toPathResServiceDto(path)
                        result.id shouldBe path.id
                        result.path shouldBe path.path
                        result.priority shouldBe path.priority
                        result.enableAuth shouldBe path.enableAuth
                        result.role shouldBe path.role
                        result.httpMethod shouldBe HttpMethodType.GET
                        result.enabled shouldBe path.enabled
                        result.item shouldBe result.item

                        verify(exactly = 1) { itemMapper.toItemResServiceDto(mockItem) }
                    }
                }
            }

            describe("toPathResControllerDto") {
                context("PathResServiceDto가 주어지면") {
                    val itemResControllerDto = mockk<ItemResControllerDto>()
                    val itemResServiceDto = mockk<ItemResServiceDto>()
                    every { itemMapper.toItemResControllerDto(itemResServiceDto) } returns itemResControllerDto
                    val serviceDto = PathResServiceDto(
                        id = 200L,
                        path = "/test/path",
                        priority = 2,
                        enableAuth = false,
                        role = null,
                        httpMethod = HttpMethodType.POST,
                        enabled = false,
                        item = itemResServiceDto
                    )


                    it("PathResControllerDto로 변환되어야 한다") {
                        val result = pathMapper.toPathResControllerDto(serviceDto)
                        result.id shouldBe serviceDto.id
                        result.path shouldBe serviceDto.path
                        result.priority shouldBe serviceDto.priority
                        result.enableAuth shouldBe serviceDto.enableAuth
                        result.role shouldBe serviceDto.role
                        result.httpMethod shouldBe serviceDto.httpMethod
                        result.enabled shouldBe serviceDto.enabled
                        result.item shouldBe result.item
                    }
                }
            }

            describe("toPathCreateReqServiceDto") {
                context("PathCreateReqControllerDto가 주어지면") {
                    val controllerDto = PathCreateReqControllerDto(
                        path = "/create/path",
                        priority = 3,
                        enableAuth = true,
                        role = "ROLE_ADMIN",
                        httpMethod = HttpMethodType.PUT,
                        enabled = true,
                        itemId = 10L
                    )

                    it("PathCreateReqServiceDto로 변환되어야 한다") {
                        val result = pathMapper.toPathCreateReqServiceDto(controllerDto)
                        result.path shouldBe controllerDto.path
                        result.priority shouldBe controllerDto.priority
                        result.enableAuth shouldBe controllerDto.enableAuth
                        result.role shouldBe controllerDto.role
                        result.httpMethod shouldBe controllerDto.httpMethod
                        result.enabled shouldBe controllerDto.enabled
                        result.itemId shouldBe controllerDto.itemId
                    }
                }
            }

            describe("toPathUpdateReqServiceDto") {
                context("PathUpdateReqControllerDto와 id가 주어지면") {
                    val controllerDto = PathUpdateReqControllerDto(
                        path = "/updated/path",
                        priority = 4,
                        enableAuth = false,
                        role = null,
                        httpMethod = HttpMethodType.PATCH,
                        enabled = true,
                    )
                    val id = 55L

                    it("PathUpdateReqServiceDto로 변환되어야 한다") {
                        val result = pathMapper.toPathUpdateReqServiceDto(controllerDto, id)
                        result.id shouldBe id
                        result.path shouldBe controllerDto.path
                        result.priority shouldBe controllerDto.priority
                        result.enableAuth shouldBe controllerDto.enableAuth
                        result.role shouldBe controllerDto.role
                        result.httpMethod shouldBe controllerDto.httpMethod
                        result.enabled shouldBe controllerDto.enabled
                    }
                }
            }

            describe("toPath (생성)") {
                context("PathCreateReqServiceDto와 Item이 주어지면") {
                    val mockItem = mockk<Item>()
                    val serviceDto = PathCreateReqServiceDto(
                        path = "/service/path",
                        priority = 5,
                        enableAuth = true,
                        role = "ROLE_USER",
                        httpMethod = HttpMethodType.HEAD,
                        enabled = true,
                        itemId = 100L
                    )

                    it("id는 무시되고 Path 엔티티가 생성되어야 한다") {
                        val result = pathMapper.toPath(serviceDto, mockItem)
                        result.id shouldBe null
                        result.path shouldBe serviceDto.path
                        result.priority shouldBe serviceDto.priority
                        result.enableAuth shouldBe serviceDto.enableAuth
                        result.role shouldBe serviceDto.role
                        result.httpMethod shouldBe serviceDto.httpMethod
                        result.enabled shouldBe serviceDto.enabled
                        result.item shouldBe mockItem
                    }
                }
            }

            describe("updatePathFromDto (업데이트)") {
                context("모든 필드가 있는 PathUpdateReqServiceDto와 기존 Path가 주어지면") {
                    val mockItem = mockk<Item>()
                    val dto = PathUpdateReqServiceDto(
                        id = 10L,
                        path = "/updated/path",
                        priority = 8,
                        enableAuth = false,
                        role = "ROLE_ADMIN",
                        httpMethod = HttpMethodType.TRACE,
                        enabled = false
                    )

                    val existingPath = Path(
                        id = 10L,
                        path = "/old/path",
                        priority = 1,
                        enableAuth = true,
                        role = "ROLE_USER",
                        httpMethod = HttpMethodType.GET,
                        enabled = true,
                        item = mockItem
                    )

                    it("기존 Path가 업데이트 되어야 한다") {
                        val result = pathMapper.updatePathFromDto(dto, existingPath)
                        result.id shouldBe existingPath.id
                        result.path shouldBe dto.path
                        result.priority shouldBe dto.priority
                        result.enableAuth shouldBe dto.enableAuth
                        result.role shouldBe dto.role
                        result.httpMethod shouldBe dto.httpMethod
                        result.enabled shouldBe dto.enabled
                        result.item shouldBe existingPath.item
                    }
                }

                context("null 필드를 가진 PathUpdateReqServiceDto가 주어지면, null 필드는 무시되어야 한다") {
                    val mockItem = mockk<Item>()
                    val dto = PathUpdateReqServiceDto(
                        id = 11L,
                        path = null,
                        priority = null,
                        enableAuth = null,
                        role = null,
                        httpMethod = null,
                        enabled = null
                    )

                    val existingPath = Path(
                        id = 11L,
                        path = "/old/path",
                        priority = 3,
                        enableAuth = true,
                        role = "ROLE_USER",
                        httpMethod = HttpMethodType.POST,
                        enabled = true,
                        item = mockItem
                    )

                    it("기존 Path가 변경 없이 유지되어야 한다") {
                        val result = pathMapper.updatePathFromDto(dto, existingPath)
                        result.id shouldBe existingPath.id
                        result.path shouldBe existingPath.path
                        result.priority shouldBe existingPath.priority
                        result.enableAuth shouldBe existingPath.enableAuth
                        result.role shouldBe existingPath.role
                        result.httpMethod shouldBe existingPath.httpMethod
                        result.enabled shouldBe existingPath.enabled
                        result.item shouldBe existingPath.item
                    }
                }
            }
        }
    }
}
