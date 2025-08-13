package com.gabinote.gateway.manager.api.item.service

import com.gabinote.gateway.manager.api.common.util.exception.ServiceException
import com.gabinote.gateway.manager.api.item.domain.Item
import com.gabinote.gateway.manager.api.item.domain.ItemRepository
import com.gabinote.gateway.manager.api.item.dto.service.ItemCreateReqServiceDto
import com.gabinote.gateway.manager.api.item.dto.service.ItemResServiceDto
import com.gabinote.gateway.manager.api.item.dto.service.ItemUpdateReqServiceDto
import com.gabinote.gateway.manager.api.item.mapper.ItemMapper
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

class ItemServiceTest : ServiceTestTemplate() {
    @MockK
    lateinit var itemRepository: ItemRepository

    @MockK
    lateinit var itemMapper: ItemMapper

    lateinit var itemService: ItemService

    init {
        beforeTest {
            clearMocks(
                itemRepository,
                itemMapper
            )
            itemService = ItemService(
                itemRepository = itemRepository,
                itemMapper = itemMapper
            )
        }

        describe("[Item] ItemService") {
            describe("ItemService.fetchById") {
                context("존재하는 아이템의 id가 주어졌을 때") {
                    val existsId = 1L
                    val existsItem = mockk<Item>()
                    beforeTest {
                        every { itemRepository.findByIdOrNull(existsId) } returns existsItem
                    }
                    it("해당 아이템을 반환한다") {
                        val result = itemService.fetchById(existsId)
                        result shouldBe existsItem

                        verify(exactly = 1) { itemRepository.findByIdOrNull(existsId) }
                    }
                }

                context("존재하지 않는 아이템의 id가 주어졌을 때") {
                    val notExistsId = 999L
                    beforeTest {
                        every { itemRepository.findByIdOrNull(notExistsId) } returns null
                    }
                    it("Item not found ServiceException를 발생시킨다") {
                        val result = shouldThrow<ServiceException> {
                            itemService.fetchById(notExistsId)
                        }

                        result.status shouldBe HttpStatus.NOT_FOUND
                        result.title shouldBe "Item not found."
                        verify(exactly = 1) { itemRepository.findByIdOrNull(notExistsId) }
                    }
                }
            }

            describe("ItemService.getAll") {
                context("페이지네이션 정보가 주어졌을 때") {
                    val pageable = TestPageableUtil.createPageable()
                    val items = listOf(mockk<Item>(), mockk<Item>())
                    val itemDtos = listOf(mockk<ItemResServiceDto>(), mockk<ItemResServiceDto>())
                    val page = PageImpl(items, pageable, items.size.toLong())

                    beforeTest {
                        every { itemRepository.findAll(pageable) } returns page
                        every { itemMapper.toItemResServiceDto(items[0]) } returns itemDtos[0]
                        every { itemMapper.toItemResServiceDto(items[1]) } returns itemDtos[1]
                    }

                    it("모든 아이템을 페이지네이션된 형태로 반환한다") {
                        val result = itemService.getAll(pageable)

                        result.content shouldBe itemDtos
                        result.totalElements shouldBe items.size.toLong()
                        result.totalPages shouldBe 1

                        verify(exactly = 1) { itemRepository.findAll(pageable) }
                        verify(exactly = 2) {
                            itemMapper.toItemResServiceDto(any())
                        }
                    }
                }
            }

            describe("ItemService.create") {
                context("올바른 생성 정보가 주어지면") {
                    val reqDto = ItemCreateReqServiceDto(
                        name = "Test Gateway",
                        url = "https://test.example.com",
                        port = 8080,
                        prefix = "/api"
                    )

                    val newItem = mockk<Item>()
                    val savedItem = mockk<Item>()
                    val expected = mockk<ItemResServiceDto>()

                    beforeTest {
                        every { itemMapper.toItem(reqDto) } returns newItem
                        every { itemRepository.save(newItem) } returns savedItem
                        every { itemMapper.toItemResServiceDto(savedItem) } returns expected
                    }

                    it("새로운 아이템을 생성하고 반환한다") {
                        val result = itemService.create(reqDto)
                        result shouldBe expected

                        verify(exactly = 1) {
                            itemMapper.toItem(reqDto)
                            itemRepository.save(newItem)
                            itemMapper.toItemResServiceDto(savedItem)
                        }
                    }
                }
            }

            describe("ItemService.update") {
                context("올바른 수정 정보가 주어지면") {
                    val reqDto = ItemUpdateReqServiceDto(
                        id = 1L,
                        name = "Updated Gateway",
                        url = "https://updated.example.com",
                        port = 9090,
                        prefix = "/api/v2"
                    )

                    val existingItem = mockk<Item>()
                    val updatedItem = mockk<Item>()
                    val expected = mockk<ItemResServiceDto>()

                    beforeTest {
                        every { itemRepository.findByIdOrNull(reqDto.id) } returns existingItem
                        every {
                            itemMapper.updateItemFromDto(reqDto, existingItem)
                        } returns updatedItem
                        every { itemRepository.save(existingItem) } returns updatedItem
                        every { itemMapper.toItemResServiceDto(updatedItem) } returns expected
                    }

                    it("기존 아이템을 업데이트하고 반환한다") {
                        val result = itemService.update(reqDto)
                        result shouldBe expected

                        verify(exactly = 1) {
                            itemRepository.findByIdOrNull(reqDto.id)
                            itemMapper.updateItemFromDto(reqDto, existingItem)
                            itemRepository.save(existingItem)
                            itemMapper.toItemResServiceDto(updatedItem)
                        }
                    }
                }

                context("존재하지 않는 아이템의 id가 주어지면") {
                    val notExistsId = 999L
                    val reqDto = ItemUpdateReqServiceDto(
                        id = notExistsId,
                        name = "Updated Gateway",
                        url = "https://updated.example.com",
                        port = 9090,
                        prefix = "/api/v2"
                    )

                    beforeTest {
                        every { itemRepository.findByIdOrNull(notExistsId) } returns null
                    }

                    it("Item not found ServiceException를 발생시킨다") {
                        val result = shouldThrow<ServiceException> {
                            itemService.update(reqDto)
                        }

                        result.status shouldBe HttpStatus.NOT_FOUND
                        result.title shouldBe "Item not found."
                        verify(exactly = 1) { itemRepository.findByIdOrNull(notExistsId) }
                    }
                }
            }

            describe("ItemService.delete") {
                context("존재하는 아이템의 id가 주어지면") {
                    val existsId = 1L
                    val existingItem = mockk<Item>()

                    beforeTest {
                        every { itemRepository.findByIdOrNull(existsId) } returns existingItem
                        every { itemRepository.delete(existingItem) } returns Unit
                    }

                    it("해당 아이템을 삭제한다") {
                        itemService.delete(existsId)

                        verify(exactly = 1) {
                            itemRepository.findByIdOrNull(existsId)
                            itemRepository.delete(existingItem)
                        }
                    }
                }

                context("존재하지 않는 아이템의 id가 주어지면") {
                    val notExistsId = 999L

                    beforeTest {
                        every { itemRepository.findByIdOrNull(notExistsId) } returns null
                    }

                    it("Item not found ServiceException를 발생시킨다") {
                        val result = shouldThrow<ServiceException> {
                            itemService.delete(notExistsId)
                        }

                        result.status shouldBe HttpStatus.NOT_FOUND
                        result.title shouldBe "Item not found."
                        verify(exactly = 1) { itemRepository.findByIdOrNull(notExistsId) }
                    }
                }
            }
        }
    }
}
