package com.gabinote.gateway.manager.api.path.service

import com.gabinote.gateway.manager.api.common.util.exception.ServiceException
import com.gabinote.gateway.manager.api.item.domain.Item
import com.gabinote.gateway.manager.api.item.service.ItemService
import com.gabinote.gateway.manager.api.path.domain.HttpMethodType
import com.gabinote.gateway.manager.api.path.domain.Path
import com.gabinote.gateway.manager.api.path.domain.PathRepository
import com.gabinote.gateway.manager.api.path.dto.service.PathCreateReqServiceDto
import com.gabinote.gateway.manager.api.path.dto.service.PathResServiceDto
import com.gabinote.gateway.manager.api.path.dto.service.PathUpdateReqServiceDto
import com.gabinote.gateway.manager.api.path.mapper.PathMapper
import com.gabinote.gateway.manager.api.testSupport.testTemplate.ServiceTestTemplate
import com.gabinote.gateway.manager.api.testSupport.testUtil.page.TestPageableUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus

class PathServiceTest : ServiceTestTemplate() {
    @MockK
    lateinit var pathRepository: PathRepository

    @MockK
    lateinit var pathMapper: PathMapper

    @MockK
    lateinit var itemService: ItemService

    lateinit var pathService: PathService

    init {
        beforeTest {
            clearMocks(
                pathRepository,
                pathMapper,
                itemService
            )
            pathService = PathService(
                pathRepository = pathRepository,
                pathMapper = pathMapper,
                itemService = itemService
            )
        }

        describe("[Path] PathService") {
            describe("PathService.fetchById") {
                context("존재하는 Path의 id가 주어졌을 때") {
                    val existsId = 1L
                    val existsPath = mockk<Path>()
                    beforeTest {
                        every { pathRepository.findByIdOrNull(existsId) } returns existsPath
                    }
                    it("해당 Path를 반환한다") {
                        val result = pathService.fetchById(existsId)
                        result shouldBe existsPath

                        verify(exactly = 1) { pathRepository.findByIdOrNull(existsId) }
                    }
                }

                context("존재하지 않는 Path의 id가 주어졌을 때") {
                    val notExistsId = 999L
                    beforeTest {
                        every { pathRepository.findByIdOrNull(notExistsId) } returns null
                    }
                    it("Path Not Found ServiceException를 발생시킨다") {
                        val result = shouldThrow<ServiceException> {
                            pathService.fetchById(notExistsId)
                        }

                        result.status shouldBe HttpStatus.NOT_FOUND
                        result.title shouldBe "Path Not Found"
                        result.message shouldBe "Path with id $notExistsId not found."
                        verify(exactly = 1) { pathRepository.findByIdOrNull(notExistsId) }
                    }
                }
            }

            describe("PathService.getAllByItem") {
                context("아이템 ID와 페이지네이션 정보가 주어졌을 때") {
                    val itemId = 1L
                    val pageable = TestPageableUtil.createPageable()
                    val item = mockk<Item>()
                    val paths = listOf(mockk<Path>(), mockk<Path>())
                    val pathDtos = listOf(mockk<PathResServiceDto>(), mockk<PathResServiceDto>())
                    val page = PageImpl(paths, pageable, paths.size.toLong())

                    beforeTest {
                        every { itemService.fetchById(itemId) } returns item
                        every { pathRepository.findAllByItem(item, pageable) } returns page
                        every { pathMapper.toPathResServiceDto(paths[0]) } returns pathDtos[0]
                        every { pathMapper.toPathResServiceDto(paths[1]) } returns pathDtos[1]
                    }

                    it("해당 아이템의 모든 Path를 페이지네이션된 형태로 반환한다") {
                        val result = pathService.getAllByItem(itemId, pageable)

                        result.content shouldBe pathDtos
                        result.totalElements shouldBe paths.size.toLong()
                        result.totalPages shouldBe 1

                        verify(exactly = 1) {
                            itemService.fetchById(itemId)
                            pathRepository.findAllByItem(item, pageable)
                        }
                        verify(exactly = 2) {
                            pathMapper.toPathResServiceDto(any())
                        }
                    }
                }
            }

            describe("PathService.create") {
                context("올바른 생성 정보가 주어지면") {
                    val itemId = 1L
                    val item = mockk<Item>()
                    val reqDto = PathCreateReqServiceDto(
                        path = "/api/test",
                        priority = 1,
                        enableAuth = true,
                        role = "ROLE_USER",
                        httpMethod = HttpMethodType.GET,
                        enabled = true,
                        itemId = itemId
                    )

                    val newPath = mockk<Path>()
                    val savedPath = mockk<Path>()
                    val expected = mockk<PathResServiceDto>()

                    beforeTest {
                        every { itemService.fetchById(itemId) } returns item
                        every { pathMapper.toPath(reqDto, item) } returns newPath
                        every { pathRepository.save(newPath) } returns savedPath
                        every { pathMapper.toPathResServiceDto(savedPath) } returns expected
                    }

                    it("새로운 Path를 생성하고 반환한다") {
                        val result = pathService.create(reqDto)
                        result shouldBe expected

                        verify(exactly = 1) {
                            itemService.fetchById(itemId)
                            pathMapper.toPath(reqDto, item)
                            pathRepository.save(newPath)
                            pathMapper.toPathResServiceDto(savedPath)
                        }
                    }
                }

                context("인증옵션이 활성화 인데 role이 null인 잘못된 경우") {
                    val itemId = 1L
                    val item = mockk<Item>()
                    val reqDto = PathCreateReqServiceDto(
                        path = "/api/test",
                        priority = 1,
                        enableAuth = true,
                        role = null,
                        httpMethod = HttpMethodType.GET,
                        enabled = true,
                        itemId = itemId
                    )

                    beforeTest {
                        every { itemService.fetchById(itemId) } returns item
                    }

                    it("Path Auth Option Error를 발생시킨다") {
                        val result = shouldThrow<ServiceException> {
                            pathService.create(reqDto)
                        }

                        result.status shouldBe HttpStatus.BAD_REQUEST
                        result.title shouldBe "Path Auth Option Error"
                        result.message shouldBe "If enableAuth is true, role must be provided."
                        verify(exactly = 1) { itemService.fetchById(itemId) }
                    }
                }

                context("인증옵션이 비활성화 인데 role이 null이 아닌 잘못된 경우") {
                    val itemId = 1L
                    val item = mockk<Item>()
                    val reqDto = PathCreateReqServiceDto(
                        path = "/api/test",
                        priority = 1,
                        enableAuth = false,
                        role = "ROLE_USER",
                        httpMethod = HttpMethodType.GET,
                        enabled = true,
                        itemId = itemId
                    )

                    beforeTest {
                        every { itemService.fetchById(itemId) } returns item
                    }

                    it("Path Auth Option Error를 발생시킨다") {
                        val result = shouldThrow<ServiceException> {
                            pathService.create(reqDto)
                        }

                        result.status shouldBe HttpStatus.BAD_REQUEST
                        result.title shouldBe "Path Auth Option Error"
                        result.message shouldBe "If enableAuth is false, role must be null."
                        verify(exactly = 1) { itemService.fetchById(itemId) }
                    }
                }
            }

            describe("PathService.update") {
                context("올바른 수정 정보가 주어지면") {
                    val pathId = 1L
                    val reqDto = PathUpdateReqServiceDto(
                        id = pathId,
                        path = "/api/updated",
                        priority = 2,
                        enableAuth = null,
                        role = null,
                        httpMethod = HttpMethodType.POST,
                        enabled = true
                    )

                    val existingPath = mockk<Path>()
                    val updatedPath = mockk<Path> {
                        every { enableAuth } returns true
                    }
                    val savedPath = mockk<Path>()
                    val expected = mockk<PathResServiceDto>()

                    beforeTest {
                        every { pathRepository.findByIdOrNull(pathId) } returns existingPath
                        every {
                            pathMapper.updatePathFromDto(reqDto, existingPath)
                        } returns updatedPath
                        every { pathRepository.save(updatedPath) } returns savedPath
                        every { pathMapper.toPathResServiceDto(savedPath) } returns expected
                    }

                    it("기존 Path를 업데이트하고 반환한다") {
                        val result = pathService.update(reqDto)
                        result shouldBe expected

                        verify(exactly = 1) {
                            pathRepository.findByIdOrNull(pathId)
                            pathMapper.updatePathFromDto(reqDto, existingPath)
                            pathRepository.save(updatedPath)
                            pathMapper.toPathResServiceDto(savedPath)
                        }
                    }
                }

                context("존재하지 않는 Path의 id가 주어지면") {
                    val notExistsId = 999L
                    val reqDto = PathUpdateReqServiceDto(
                        id = notExistsId,
                        path = "/api/updated",
                        priority = 2,
                        enableAuth = true,
                        role = "ROLE_ADMIN",
                        httpMethod = HttpMethodType.POST,
                        enabled = true
                    )

                    beforeTest {
                        every { pathRepository.findByIdOrNull(notExistsId) } returns null
                    }

                    it("Path Not Found 에러코드를 가진 ServiceException를 발생시킨다") {
                        val result = shouldThrow<ServiceException> {
                            pathService.update(reqDto)
                        }

                        result.status shouldBe HttpStatus.NOT_FOUND
                        result.title shouldBe "Path Not Found"
                        result.message shouldBe "Path with id $notExistsId not found."
                        verify(exactly = 1) { pathRepository.findByIdOrNull(notExistsId) }
                    }
                }

                describe("인증 옵션을 변경하는 경우") {
                    context("인증 옵션을 올바르게 비활성화 하는 경우") {
                        val pathId = 1L
                        val reqDto = PathUpdateReqServiceDto(
                            id = pathId,
                            path = "/api/updated",
                            priority = 2,
                            enableAuth = false,
                            role = null,
                            httpMethod = HttpMethodType.POST,
                            enabled = true
                        )

                        val existingPath = mockk<Path>()
                        val updatedPath = mockk<Path> {
                            every { enableAuth } returns true
                            every { changeEnableAuth(false) } returns Unit
                            every { changeRole(null) } returns Unit
                        }
                        val savedPath = mockk<Path>()
                        val expected = mockk<PathResServiceDto>()

                        beforeTest {
                            every { pathRepository.findByIdOrNull(pathId) } returns existingPath
                            every {
                                pathMapper.updatePathFromDto(reqDto, existingPath)
                            } returns updatedPath
                            every { pathRepository.save(updatedPath) } returns savedPath
                            every { pathMapper.toPathResServiceDto(savedPath) } returns expected
                        }

                        it("인증 옵션과 역할을 올바르게 변경한다") {
                            val result = pathService.update(reqDto)
                            result shouldBe expected

                            verify(exactly = 1) {
                                pathRepository.findByIdOrNull(pathId)
                                pathMapper.updatePathFromDto(reqDto, existingPath)
                                pathRepository.save(updatedPath)
                                pathMapper.toPathResServiceDto(savedPath)
                            }

                        }
                    }

                    context("인증 옵션을 올바르게 활성화 하는 경우") {
                        val pathId = 1L
                        val reqDto = PathUpdateReqServiceDto(
                            id = pathId,
                            path = "/api/updated",
                            priority = 2,
                            enableAuth = true,
                            role = "ROLE_USER",
                            httpMethod = HttpMethodType.POST,
                            enabled = true
                        )

                        val existingPath = mockk<Path>()
                        val updatedPath = mockk<Path> {
                            every { enableAuth } returns false
                            every { changeEnableAuth(true) } returns Unit
                            every { changeRole(reqDto.role) } returns Unit
                        }
                        val savedPath = mockk<Path>()
                        val expected = mockk<PathResServiceDto>()

                        beforeTest {
                            every { pathRepository.findByIdOrNull(pathId) } returns existingPath
                            every {
                                pathMapper.updatePathFromDto(reqDto, existingPath)
                            } returns updatedPath
                            every { pathRepository.save(updatedPath) } returns savedPath
                            every { pathMapper.toPathResServiceDto(savedPath) } returns expected
                        }

                        it("인증 옵션과 역할을 올바르게 변경한다") {
                            val result = pathService.update(reqDto)
                            result shouldBe expected

                            verify(exactly = 1) {
                                pathRepository.findByIdOrNull(pathId)
                                pathMapper.updatePathFromDto(reqDto, existingPath)
                                pathRepository.save(updatedPath)
                                pathMapper.toPathResServiceDto(savedPath)
                            }

                        }
                    }

                    context("인증 옵션을 비활성화 하는데, role이 null이 아닌 잘못된 경우") {
                        val pathId = 1L
                        val reqDto = PathUpdateReqServiceDto(
                            id = pathId,
                            path = "/api/updated",
                            priority = 2,
                            enableAuth = false,
                            role = "NONE_ROLE",
                            httpMethod = HttpMethodType.POST,
                            enabled = true
                        )

                        val existingPath = mockk<Path>()
                        val updatedPath = mockk<Path> {
                            every { enableAuth } returns true
                            every { role } returns "NONE_ROLE"
                        }

                        beforeTest {
                            every { pathRepository.findByIdOrNull(pathId) } returns existingPath
                            every {
                                pathMapper.updatePathFromDto(reqDto, existingPath)
                            } returns updatedPath
                        }

                        it("Path Auth Option Error ServiceException이 발생한다.") {
                            val result = shouldThrow<ServiceException> {
                                pathService.update(reqDto)
                            }
                            result.status shouldBe HttpStatus.BAD_REQUEST
                            result.title shouldBe "Path Auth Option Error"
                            result.message shouldBe "If enableAuth is false, role must be null."

                            verify(exactly = 1) {
                                pathRepository.findByIdOrNull(pathId)
                                pathMapper.updatePathFromDto(reqDto, existingPath)
                            }
                        }
                    }

                    context("인증 옵션을 활성화 하는데, role이 null인 잘못된 경우") {
                        val pathId = 1L
                        val reqDto = PathUpdateReqServiceDto(
                            id = pathId,
                            path = "/api/updated",
                            priority = 2,
                            enableAuth = true,
                            role = null,
                            httpMethod = HttpMethodType.POST,
                            enabled = true
                        )

                        val existingPath = mockk<Path>()
                        val updatedPath = mockk<Path> {
                            every { enableAuth } returns false
                            every { role } returns null
                        }

                        beforeTest {
                            every { pathRepository.findByIdOrNull(pathId) } returns existingPath
                            every {
                                pathMapper.updatePathFromDto(reqDto, existingPath)
                            } returns updatedPath
                        }

                        it("Path Auth Option Error ServiceException이 발생한다.") {
                            val result = shouldThrow<ServiceException> {
                                pathService.update(reqDto)
                            }
                            result.status shouldBe HttpStatus.BAD_REQUEST
                            result.title shouldBe "Path Auth Option Error"
                            result.message shouldBe "If enableAuth is true, role must be provided."

                            verify(exactly = 1) {
                                pathRepository.findByIdOrNull(pathId)
                                pathMapper.updatePathFromDto(reqDto, existingPath)
                            }
                        }
                    }
                }

                describe("Role을 변경하는 경우") {
                    context("올바른 수정 정보가 주어지면") {
                        val pathId = 1L
                        val reqDto = PathUpdateReqServiceDto(
                            id = pathId,
                            path = "/api/updated",
                            priority = 2,
                            enableAuth = null,
                            role = "NEW_ROLE",
                            httpMethod = HttpMethodType.POST,
                            enabled = true
                        )

                        val existingPath = mockk<Path>()
                        val updatedPath = mockk<Path> {
                            every { enableAuth } returns true
                            every { changeRole(reqDto.role) } returns Unit
                            every { changeEnableAuth(true) } returns Unit
                            every { role } returns "BEFORE_ROLE"
                        }
                        val savedPath = mockk<Path>()
                        val expected = mockk<PathResServiceDto>()

                        beforeTest {
                            every { pathRepository.findByIdOrNull(pathId) } returns existingPath
                            every {
                                pathMapper.updatePathFromDto(reqDto, existingPath)
                            } returns updatedPath
                            every { pathRepository.save(updatedPath) } returns savedPath
                            every { pathMapper.toPathResServiceDto(savedPath) } returns expected
                        }

                        it("기존 Path를 업데이트하고 반환한다") {
                            val result = pathService.update(reqDto)
                            result shouldBe expected

                            verify(exactly = 1) {
                                pathRepository.findByIdOrNull(pathId)
                                pathMapper.updatePathFromDto(reqDto, existingPath)
                                pathRepository.save(updatedPath)
                                pathMapper.toPathResServiceDto(savedPath)
                            }
                        }
                    }

                    context("기존 인증 옵션이 비활성화 상태인데, Role을 변경하는 잘못된 경우") {
                        val pathId = 1L
                        val reqDto = PathUpdateReqServiceDto(
                            id = pathId,
                            path = "/api/updated",
                            priority = 2,
                            enableAuth = null,
                            role = "ROLE_ADMIN",
                            httpMethod = HttpMethodType.POST,
                            enabled = true
                        )

                        val existingPath = mockk<Path>()
                        val updatedPath = mockk<Path> {
                            every { enableAuth } returns false
                            every { role } returns null
                        }

                        beforeTest {
                            every { pathRepository.findByIdOrNull(pathId) } returns existingPath
                            every {
                                pathMapper.updatePathFromDto(reqDto, existingPath)
                            } returns updatedPath
                        }

                        it("Path Role Change Error ServiceException 를 발생시킨다") {
                            val result = shouldThrow<ServiceException> {
                                pathService.update(reqDto)
                            }

                            result.status shouldBe HttpStatus.BAD_REQUEST
                            result.title shouldBe "Path Role Change Error"
                        }
                    }
                }
            }

            describe("PathService.delete") {
                context("존재하는 Path의 id가 주어지면") {
                    val existsId = 1L
                    val existingPath = mockk<Path>()

                    beforeTest {
                        every { pathRepository.findByIdOrNull(existsId) } returns existingPath
                        every { pathRepository.delete(existingPath) } returns Unit
                    }

                    it("해당 Path를 삭제한다") {
                        pathService.delete(existsId)

                        verify(exactly = 1) {
                            pathRepository.findByIdOrNull(existsId)
                            pathRepository.delete(existingPath)
                        }
                    }
                }

                context("존재하지 않는 Path의 id가 주어지면") {
                    val notExistsId = 999L

                    beforeTest {
                        every { pathRepository.findByIdOrNull(notExistsId) } returns null
                    }

                    it("Path Not Found ServiceException를 발생시킨다") {
                        val result = shouldThrow<ServiceException> {
                            pathService.delete(notExistsId)
                        }

                        result.status shouldBe HttpStatus.NOT_FOUND
                        result.title shouldBe "Path Not Found"
                        verify(exactly = 1) { pathRepository.findByIdOrNull(notExistsId) }
                    }
                }
            }
        }
    }
}

