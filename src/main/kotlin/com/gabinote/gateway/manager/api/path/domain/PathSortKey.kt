package com.gabinote.gateway.manager.api.path.domain

import com.gabinote.gateway.manager.api.common.domain.BaseSortKey

enum class PathSortKey(
    override val key: String,
) : BaseSortKey {
    ID("id"),
    ROLE("role"),
    METHOD("method"),
    PRIORITY("priority"),
}