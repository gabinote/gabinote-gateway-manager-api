package com.gabinote.gateway.manager.api.item.service

import com.gabinote.gateway.manager.api.common.util.exception.ServiceException
import com.gabinote.gateway.manager.api.item.domain.Item
import com.gabinote.gateway.manager.api.item.domain.ItemRepository
import com.gabinote.gateway.manager.api.item.dto.service.ItemCreateReqServiceDto
import com.gabinote.gateway.manager.api.item.dto.service.ItemResServiceDto
import com.gabinote.gateway.manager.api.item.dto.service.ItemUpdateReqServiceDto
import com.gabinote.gateway.manager.api.item.mapper.ItemMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ItemService(
    private val itemRepository: ItemRepository,
    private val itemMapper: ItemMapper
) {

    @Transactional(readOnly = true)
    fun fetchById(id: Long): Item {
        return itemRepository.findByIdOrNull(id) ?: throw ServiceException(
            status = HttpStatus.NOT_FOUND,
            title = "Item not found.",
            message = "Item with ID $id does not exist.",
            loggingDetail = "Item with ID $id does not exist."
        )
    }

    @Transactional(readOnly = true)
    fun getAll(pageable: Pageable): Page<ItemResServiceDto> {
        val data = itemRepository.findAll(pageable)
        return data.map { itemMapper.toItemResServiceDto(it) }
    }

    @Transactional
    fun create(dto: ItemCreateReqServiceDto): ItemResServiceDto {
        val item = itemMapper.toItem(dto)
        val savedItem = itemRepository.save(item)
        return itemMapper.toItemResServiceDto(savedItem)
    }

    @Transactional
    fun update(dto: ItemUpdateReqServiceDto): ItemResServiceDto {
        val item = fetchById(dto.id)
        itemMapper.updateItemFromDto(dto, item)
        val updatedItem = itemRepository.save(item)
        return itemMapper.toItemResServiceDto(updatedItem)
    }

    @Transactional
    fun delete(id: Long) {
        val item = fetchById(id)
        itemRepository.delete(item)
    }
}