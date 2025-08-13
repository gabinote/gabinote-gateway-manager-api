package com.gabinote.gateway.manager.api.path.service

import com.gabinote.gateway.manager.api.common.util.exception.ServiceException
import com.gabinote.gateway.manager.api.item.service.ItemService
import com.gabinote.gateway.manager.api.path.domain.Path
import com.gabinote.gateway.manager.api.path.domain.PathRepository
import com.gabinote.gateway.manager.api.path.dto.service.PathCreateReqServiceDto
import com.gabinote.gateway.manager.api.path.dto.service.PathResServiceDto
import com.gabinote.gateway.manager.api.path.dto.service.PathUpdateReqServiceDto
import com.gabinote.gateway.manager.api.path.mapper.PathMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PathService(
    private val pathRepository: PathRepository,
    private val pathMapper: PathMapper,
    private val itemService: ItemService
) {

    @Transactional(readOnly = true)
    fun fetchById(id: Long): Path {
        return pathRepository.findByIdOrNull(id) ?: throw ServiceException(
            status = HttpStatus.NOT_FOUND,
            title = "Path Not Found",
            message = "Path with id $id not found.",
            loggingDetail = "Path with id $id not found."
        )
    }

    @Transactional(readOnly = true)
    fun getAllByItem(itemId: Long, pageable: Pageable): Page<PathResServiceDto> {
        val targetItem = itemService.fetchById(itemId)
        return pathRepository.findAllByItem(
            pageable = pageable,
            item = targetItem
        ).map { pathMapper.toPathResServiceDto(it) }
    }

    @Transactional
    fun create(dto: PathCreateReqServiceDto): PathResServiceDto {
        val targetItem = itemService.fetchById(dto.itemId)
        checkAuthOptionValid(dto.enableAuth, dto.role)
        val newPath = pathMapper.toPath(dto, targetItem)
        val savedPath = pathRepository.save(newPath)
        return pathMapper.toPathResServiceDto(savedPath)
    }

    @Transactional
    fun update(dto: PathUpdateReqServiceDto): PathResServiceDto {
        val targetPath = fetchById(dto.id)
        val updatedPath = pathMapper.updatePathFromDto(dto, targetPath)
        checkAuthOptionAndRole(dto, updatedPath)
        val savedPath = pathRepository.save(updatedPath)
        return pathMapper.toPathResServiceDto(savedPath)
    }

    @Transactional
    fun delete(id: Long) {
        val targetPath = fetchById(id)
        pathRepository.delete(targetPath)
    }

    private fun checkAuthOptionAndRole(dto: PathUpdateReqServiceDto, targetPath: Path) {
        // 인증옵션이 변경되는 경우에는 기존 true + role or false + null 조합을 유지해야 함
        if (isChangeAuthOption(dto, targetPath)) {
            checkAuthOptionValid(dto.enableAuth!!, dto.role)
            targetPath.changeRole(dto.role)
            targetPath.changeEnableAuth(dto.enableAuth)

        } else if (isChangeRole(dto, targetPath)) {
            // 단순 role 변경일 경우, 인증옵션 활성화 상태일 때 null이 아닌 role로 변경 가능
            if (canChangeRole(targetPath, dto)) {
                targetPath.changeRole(dto.role)
            } else {
                throw ServiceException(
                    status = HttpStatus.BAD_REQUEST,
                    title = "Path Role Change Error",
                    message = "Role can only be changed if enableAuth is true.",
                    loggingDetail = "Path Role Change Error: enableAuth is false but role is being changed."
                )
            }
        }
    }

    private fun canChangeRole(
        targetPath: Path,
        dto: PathUpdateReqServiceDto
    ): Boolean = targetPath.enableAuth && dto.role != null

    private fun isChangeAuthOption(
        dto: PathUpdateReqServiceDto,
        targetPath: Path
    ): Boolean = dto.enableAuth != null && dto.enableAuth != targetPath.enableAuth


    private fun isChangeRole(
        dto: PathUpdateReqServiceDto,
        targetPath: Path
    ): Boolean = dto.role != null && dto.role != targetPath.role


    private fun checkAuthOptionValid(enableAuth: Boolean, role: String?) {
        if (enableAuth && role == null) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                title = "Path Auth Option Error",
                message = "If enableAuth is true, role must be provided.",
                loggingDetail = "Path Auth Option Error: enableAuth is true but role is null."
            )
        }

        if (!enableAuth && role != null) {
            throw ServiceException(
                status = HttpStatus.BAD_REQUEST,
                title = "Path Auth Option Error",
                message = "If enableAuth is false, role must be null.",
                loggingDetail = "Path Auth Option Error: enableAuth is false but role is not null."
            )
        }
    }

}