package com.gabinote.gateway.manager.api.item.domain

import org.springframework.data.jpa.repository.JpaRepository

interface ItemRepository : JpaRepository<Item, Long> {

}