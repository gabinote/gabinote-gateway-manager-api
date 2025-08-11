package com.gabinote.gateway.manager.api.item.domain

import com.gabinote.gateway.manager.api.common.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name="GATEWAY_ITEM")
class Item(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("GATEWAY_ITEM_PK")
    override var id: Long? = null,

    @Column("GATEWAY_ITEM_NAME", nullable = false, length = 255)
    var name: String,

    @Column("GATEWAY_ITEM_URL", nullable = false, length = 2048)
    var url: String,

    @Column("GATEWAY_ITEM_PORT", nullable = false)
    var port: Int,

    @Column("GATEWAY_ITEM_PREFIX", nullable = true, length = 255)
    var prefix: String? = null,
) : BaseEntity<Long>(){

    fun changeName(name: String) {
        this.name = name
    }

    fun changeUrl(url: String) {
        this.url = url
    }

    fun changePort(port: Int) {
        this.port = port
    }

    fun changePrefix(prefix: String?) {
        this.prefix = prefix
    }
}