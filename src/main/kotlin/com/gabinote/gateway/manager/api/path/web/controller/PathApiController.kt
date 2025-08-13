package com.gabinote.gateway.manager.api.path.web.controller

import com.gabinote.gateway.manager.api.common.dto.page.controller.PagedResControllerDto
import com.gabinote.gateway.manager.api.common.mapper.PageMapper
import com.gabinote.gateway.manager.api.common.util.validation.pageable.size.PageSizeCheck
import com.gabinote.gateway.manager.api.common.util.validation.pageable.sort.PageSortKeyCheck
import com.gabinote.gateway.manager.api.path.domain.PathSortKey
import com.gabinote.gateway.manager.api.path.dto.controller.PathCreateReqControllerDto
import com.gabinote.gateway.manager.api.path.dto.controller.PathResControllerDto
import com.gabinote.gateway.manager.api.path.dto.controller.PathUpdateReqControllerDto
import com.gabinote.gateway.manager.api.path.mapper.PathMapper
import com.gabinote.gateway.manager.api.path.service.PathService
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/v1")
@RestController
class PathApiController(
    private val pathService: PathService,
    private val pathMapper: PathMapper,
    private val pageMapper: PageMapper
) {

    @GetMapping("/items/{itemId}/paths")
    fun getPaths(
        @PathVariable("itemId")
        itemId: Long,
        @PageSizeCheck(min = 1, max = 100)
        @PageableDefault(page = 0, size = 20, sort = ["id"], direction = Sort.Direction.DESC)
        @PageSortKeyCheck(sortKey = PathSortKey::class, message = "Invalid sort key")
        pageable: Pageable
    ): ResponseEntity<PagedResControllerDto<PathResControllerDto>> {
        val paths = pathService.getAllByItem(
            itemId = itemId,
            pageable = pageable
        )
        val data = paths.map { pathMapper.toPathResControllerDto(it) }
        val res = pageMapper.toPagedResponse(data)

        return ResponseEntity.ok(res)
    }

    @PostMapping("/paths")
    fun createPath(
        @Validated
        @RequestBody
        dto: PathCreateReqControllerDto
    ): ResponseEntity<PathResControllerDto> {
        val reqDto = pathMapper.toPathCreateReqServiceDto(dto)
        val result = pathService.create(reqDto)
        val res = pathMapper.toPathResControllerDto(result)
        return ResponseEntity.status(201).body(res)
    }

    @DeleteMapping("/paths/{pathId}")
    fun deletePath(
        @PathVariable("pathId")
        pathId: Long
    ): ResponseEntity<Void> {
        pathService.delete(pathId)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/paths/{pathId}")
    fun updatePath(
        @PathVariable("pathId")
        pathId: Long,
        @Validated
        @RequestBody
        dto: PathUpdateReqControllerDto
    ): ResponseEntity<PathResControllerDto> {
        val reqDto = pathMapper.toPathUpdateReqServiceDto(
            dto = dto,
            id = pathId
        )
        val result = pathService.update(reqDto)
        val res = pathMapper.toPathResControllerDto(result)
        return ResponseEntity.ok(res)
    }


}