package com.gabinote.gateway.manager.api.path.domain

import com.gabinote.gateway.manager.api.item.domain.Item
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface PathRepository : JpaRepository<Path, Long> {
    fun findAllByItem(item: Item, pageable: Pageable): Page<Path>
}