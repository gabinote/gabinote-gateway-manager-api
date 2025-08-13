package com.gabinote.gateway.manager.api.item.web.controller

import com.gabinote.gateway.manager.api.common.dto.page.controller.PagedResControllerDto
import com.gabinote.gateway.manager.api.common.mapper.PageMapper
import com.gabinote.gateway.manager.api.common.util.validation.pageable.size.PageSizeCheck
import com.gabinote.gateway.manager.api.common.util.validation.pageable.sort.PageSortKeyCheck
import com.gabinote.gateway.manager.api.item.domain.ItemSortKey
import com.gabinote.gateway.manager.api.item.dto.controller.ItemCreateReqControllerDto
import com.gabinote.gateway.manager.api.item.dto.controller.ItemResControllerDto
import com.gabinote.gateway.manager.api.item.dto.controller.ItemUpdateReqControllerDto
import com.gabinote.gateway.manager.api.item.mapper.ItemMapper
import com.gabinote.gateway.manager.api.item.service.ItemService
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RequestMapping("/api/v1/items")
@RestController
class ItemApiController(
    private val itemService: ItemService,
    private val itemMapper: ItemMapper,
    private val pageMapper: PageMapper
) {

    @GetMapping
    fun getItems(
        @PageSizeCheck(min = 1, max = 100)
        @PageableDefault(page = 0, size = 20, sort = ["id"], direction = Sort.Direction.DESC)
        @PageSortKeyCheck(sortKey = ItemSortKey::class, message = "Invalid sort key")
        pageable: Pageable
    ): ResponseEntity<PagedResControllerDto<ItemResControllerDto>> {
        val items = itemService.getAll(pageable)
        val data = items.map { itemMapper.toItemResControllerDto(it) }
        val res = pageMapper.toPagedResponse(data)
        return ResponseEntity.ok(res)
    }

    @PostMapping
    fun createItem(
        @Validated
        @RequestBody
        dto: ItemCreateReqControllerDto
    ): ResponseEntity<ItemResControllerDto> {
        val reqDto = itemMapper.toItemCreateReqServiceDto(dto)
        val result = itemService.create(reqDto)
        val res = itemMapper.toItemResControllerDto(result)

        return ResponseEntity.status(201).body(res)
    }

    @PutMapping("/{id}")
    fun updateItem(
        @PathVariable
        id: Long,
        @Validated
        @RequestBody
        dto: ItemUpdateReqControllerDto
    ): ResponseEntity<ItemResControllerDto> {
        val reqDto = itemMapper.toItemUpdateReqServiceDto(
            id = id,
            item = dto
        )
        val result = itemService.update(reqDto)
        val res = itemMapper.toItemResControllerDto(result)
        return ResponseEntity.ok(res)
    }

    @DeleteMapping("/{id}")
    fun deleteItem(
        @PathVariable
        id: Long
    ): ResponseEntity<Void> {
        itemService.delete(id)
        return ResponseEntity.noContent().build()
    }
}