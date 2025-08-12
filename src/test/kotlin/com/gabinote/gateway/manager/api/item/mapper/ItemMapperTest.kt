package com.gabinote.gateway.manager.api.item.mapper

import com.gabinote.gateway.manager.api.item.domain.Item
import com.gabinote.gateway.manager.api.item.dto.controller.ItemCreateReqControllerDto
import com.gabinote.gateway.manager.api.item.dto.controller.ItemResControllerDto
import com.gabinote.gateway.manager.api.item.dto.controller.ItemUpdateReqControllerDto
import com.gabinote.gateway.manager.api.item.dto.service.ItemCreateReqServiceDto
import com.gabinote.gateway.manager.api.item.dto.service.ItemResServiceDto
import com.gabinote.gateway.manager.api.item.dto.service.ItemUpdateReqServiceDto
import com.gabinote.gateway.manager.api.testSupport.testTemplate.MockkTestTemplate
import io.kotest.matchers.shouldBe


class ItemMapperTest : MockkTestTemplate() {

    val itemMapper: ItemMapper = ItemMapperImpl()

    init {
        describe("[Item] ItemMapper") {

            describe("ItemMapper.toItemResServiceDto") {
                context("Item 엔티티가 주어지면,") {
                    val item = Item(
                        id = 1L,
                        name = "Gateway",
                        url = "https://example.com",
                        port = 8080,
                        prefix = "/api"
                    )

                    val expected = ItemResServiceDto(
                        id = item.id!!,
                        name = item.name,
                        url = item.url,
                        port = item.port,
                        prefix = item.prefix
                    )

                    it("ItemResServiceDto로 변환되어야 한다.") {
                        val result = itemMapper.toItemResServiceDto(item)
                        result shouldBe expected
                    }
                }
            }

            describe("ItemMapper.toItemResControllerDto") {
                context("ItemResServiceDto가 주어지면,") {
                    val dto = ItemResServiceDto(
                        id = 2L,
                        name = "Proxy",
                        url = "https://proxy.local",
                        port = 9000,
                        prefix = null
                    )

                    val expected = ItemResControllerDto(
                        id = dto.id,
                        name = dto.name,
                        url = dto.url,
                        port = dto.port,
                        prefix = dto.prefix
                    )

                    it("ItemResControllerDto로 변환되어야 한다.") {
                        val result = itemMapper.toItemResControllerDto(dto)
                        result shouldBe expected
                    }
                }
            }

            describe("ItemMapper.toItemCreateReqServiceDto") {
                context("ItemCreateReqControllerDto가 주어지면,") {
                    val controllerDto = ItemCreateReqControllerDto(
                        name = "New Service",
                        url = "https://new.service",
                        port = 7700,
                        prefix = "/new"
                    )

                    val expected = ItemCreateReqServiceDto(
                        name = controllerDto.name,
                        url = controllerDto.url,
                        port = controllerDto.port,
                        prefix = controllerDto.prefix
                    )

                    it("ItemCreateReqServiceDto로 변환되어야 한다.") {
                        val result = itemMapper.toItemCreateReqServiceDto(controllerDto)
                        result shouldBe expected
                    }
                }
            }

            describe("ItemMapper.toItemUpdateReqServiceDto") {
                context("ItemUpdateReqControllerDto가 주어지면,") {
                    val controllerDto = ItemUpdateReqControllerDto(
                        name = "Updated Service",
                        url = "https://updated.service",
                        port = 8800,
                        prefix = null
                    )

                    val expected = ItemUpdateReqServiceDto(
                        id = 1,
                        name = controllerDto.name!!,
                        url = controllerDto.url!!,
                        port = controllerDto.port!!,
                        prefix = controllerDto.prefix
                    )

                    it("ItemUpdateReqServiceDto로 변환되어야 한다") {
                        val result = itemMapper.toItemUpdateReqServiceDto(controllerDto, 1)
                        result.id shouldBe expected.id
                        result.name shouldBe expected.name
                        result.url shouldBe expected.url
                        result.port shouldBe expected.port
                        result.prefix shouldBe expected.prefix
                    }
                }
            }

            describe("ItemMapper.toItem(ItemCreateReqServiceDto)") {
                context("ItemCreateReqServiceDto가 주어지면,") {
                    val dto = ItemCreateReqServiceDto(
                        name = "Created",
                        url = "https://created",
                        port = 7000,
                        prefix = null
                    )

                    it("id는 null이며 필드들이 설정된 Item으로 변환되어야 한다.") {
                        val result = itemMapper.toItem(dto)
                        result.id shouldBe null
                        result.name shouldBe dto.name
                        result.url shouldBe dto.url
                        result.port shouldBe dto.port
                        result.prefix shouldBe dto.prefix
                    }
                }
            }

            describe("ItemMapper.updateItemFromDto(ItemUpdateReqServiceDto)") {
                context("모든 값이 들어있는 DTO가 주어지면, 기존 Item의 필드가 모두 업데이트되어야 한다.") {
                    val existing = Item(
                        id = 10L,
                        name = "OldName",
                        url = "https://old",
                        port = 1111,
                        prefix = "/old"
                    )

                    val dto = ItemUpdateReqServiceDto(
                        id = 10, // 매핑에서 id 변경을 기대하지 않음(타입도 다름)
                        name = "NewName",
                        url = "https://new",
                        port = 2222,
                        prefix = "/new"
                    )

                    val expected = Item(
                        id = existing.id,
                        name = dto.name,
                        url = dto.url,
                        port = dto.port,
                        prefix = dto.prefix
                    )

                    it("name, url, port, prefix가 업데이트되어야 한다.") {
                        val result = itemMapper.updateItemFromDto(dto, existing)
                        // id는 변경되지 않아야 함
                        result.id shouldBe expected.id
                        result.name shouldBe expected.name
                        result.url shouldBe expected.url
                        result.port shouldBe expected.port
                        result.prefix shouldBe expected.prefix
                    }
                }

                context("prefix가 null인 DTO가 주어지면, 기존 prefix는 유지되어야 한다.") {
                    val existing = Item(
                        id = 11L,
                        name = "KeepPrefix",
                        url = "https://keep",
                        port = 3333,
                        prefix = "/keep"
                    )

                    val dto = ItemUpdateReqServiceDto(
                        id = 11,
                        name = "KeepPrefixUpdated",
                        url = "https://keep-updated",
                        port = 4444,
                        prefix = null // null이면 NullValuePropertyMappingStrategy.IGNORE에 의해 무시되어야 함
                    )

                    it("prefix는 변경되지 않고 다른 필드만 업데이트되어야 한다.") {
                        val result = itemMapper.updateItemFromDto(dto, existing)
                        result.id shouldBe existing.id
                        result.name shouldBe dto.name
                        result.url shouldBe dto.url
                        result.port shouldBe dto.port
                        result.prefix shouldBe existing.prefix // 유지되어야 함
                    }
                }
            }
        }
    }
}