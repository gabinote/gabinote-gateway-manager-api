package com.gabinote.gateway.manager.api.item.domain

import com.gabinote.gateway.manager.api.common.domain.BaseSortKey

enum class ItemSortKey(
    override val key: String
) : BaseSortKey {
    NAME("name"),
    ID("id"),
    PORT("port"),
    PREFIX("prefix");
}