package com.gabinote.gateway.manager.api.path.mapper

import com.gabinote.gateway.manager.api.item.domain.Item
import com.gabinote.gateway.manager.api.item.mapper.ItemMapper
import com.gabinote.gateway.manager.api.path.domain.Path
import com.gabinote.gateway.manager.api.path.dto.controller.PathCreateReqControllerDto
import com.gabinote.gateway.manager.api.path.dto.controller.PathResControllerDto
import com.gabinote.gateway.manager.api.path.dto.controller.PathUpdateReqControllerDto
import com.gabinote.gateway.manager.api.path.dto.service.PathCreateReqServiceDto
import com.gabinote.gateway.manager.api.path.dto.service.PathResServiceDto
import com.gabinote.gateway.manager.api.path.dto.service.PathUpdateReqServiceDto
import org.mapstruct.*

@Mapper(
    componentModel = "spring",
    uses = [ItemMapper::class],
)
interface PathMapper {
    fun toPathResServiceDto(path: Path): PathResServiceDto
    fun toPathResControllerDto(dto: PathResServiceDto): PathResControllerDto
    fun toPathCreateReqServiceDto(dto: PathCreateReqControllerDto): PathCreateReqServiceDto
    fun toPathUpdateReqServiceDto(dto: PathUpdateReqControllerDto, id: Long): PathUpdateReqServiceDto

    @Mapping(target = "id", ignore = true)
    fun toPath(dto: PathCreateReqServiceDto, item: Item): Path

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    fun updatePathFromDto(dto: PathUpdateReqServiceDto, @MappingTarget path: Path): Path
}