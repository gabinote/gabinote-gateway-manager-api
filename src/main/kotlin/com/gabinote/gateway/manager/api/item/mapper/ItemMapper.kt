package com.gabinote.gateway.manager.api.item.mapper

import com.gabinote.gateway.manager.api.item.domain.Item
import com.gabinote.gateway.manager.api.item.dto.controller.ItemCreateReqControllerDto
import com.gabinote.gateway.manager.api.item.dto.controller.ItemResControllerDto
import com.gabinote.gateway.manager.api.item.dto.controller.ItemUpdateReqControllerDto
import com.gabinote.gateway.manager.api.item.dto.service.ItemCreateReqServiceDto
import com.gabinote.gateway.manager.api.item.dto.service.ItemResServiceDto
import com.gabinote.gateway.manager.api.item.dto.service.ItemUpdateReqServiceDto
import org.mapstruct.*

@Mapper(
    componentModel = "spring",
)
interface ItemMapper {
    fun toItemResServiceDto(item: Item): ItemResServiceDto
    fun toItemResControllerDto(itemResServiceDto: ItemResServiceDto): ItemResControllerDto
    fun toItemCreateReqServiceDto(item: ItemCreateReqControllerDto): ItemCreateReqServiceDto
    fun toItemUpdateReqServiceDto(item: ItemUpdateReqControllerDto, id: Long): ItemUpdateReqServiceDto

    @Mapping(target = "id", ignore = true)
    fun toItem(item: ItemCreateReqServiceDto): Item

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    fun updateItemFromDto(
        itemUpdateReqServiceDto: ItemUpdateReqServiceDto,
        @MappingTarget item: Item
    ): Item
}