package com.gabinote.gateway.manager.api.path.domain

import com.gabinote.gateway.manager.api.common.domain.BaseEntity
import com.gabinote.gateway.manager.api.item.domain.Item
import jakarta.persistence.*
import org.springframework.http.HttpMethod

@Entity(name = "GATEWAY_PATH")
class Path(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("GATEWAY_PATH_PK")
    override var id: Long? = null,

    @Column("GATEWAY_PATH_PATH", nullable = false, length = 255)
    var path: String,

    @Column("GATEWAY_PATH_PRIORITY", nullable = false)
    var priority: Int,

    @Column("GATEWAY_PATH_ENABLE_AUTH", nullable = false)
    var enableAuth: Boolean,

    @Column("GATEWAY_PATH_ROLE", nullable = false, length = 255)
    var role: String,

    @Column("GATEWAY_PATH_HTTP_METHOD", nullable = false)
    var _httpMethod: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GATEWAY_ITEM_PK", nullable = false)
    var item: Item

) : BaseEntity<Long>() {
    val httpMethod: HttpMethod
        get() = HttpMethod.valueOf(_httpMethod.uppercase())

    fun changePath(newPath: String) {
        this.path = newPath
    }

    fun changePriority(newPriority: Int) {
        this.priority = newPriority
    }

    fun changeEnableAuth(newEnableAuth: Boolean) {
        this.enableAuth = newEnableAuth
    }

    fun changeRole(newRole: String) {
        this.role = newRole
    }

    fun changeHttpMethod(newHttpMethod: HttpMethod) {
        this._httpMethod = newHttpMethod.name()
    }
}