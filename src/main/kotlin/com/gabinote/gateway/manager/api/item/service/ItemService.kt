package com.gabinote.gateway.manager.api.item.service

import com.gabinote.gateway.manager.api.item.domain.ItemRepository
import org.springframework.stereotype.Service

@Service
class ItemService(
    private val itemRepository: ItemRepository
)