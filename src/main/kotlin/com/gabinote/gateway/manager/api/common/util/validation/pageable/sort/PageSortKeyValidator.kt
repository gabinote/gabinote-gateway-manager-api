package com.gabinote.api.common.utils.validation.page.sort

import com.gabinote.gateway.manager.api.common.util.validation.pageable.sort.PageSortKeyCheck
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.data.domain.Pageable

class PageSortKeyValidator : ConstraintValidator<PageSortKeyCheck, Pageable> {

    private var sortKeys: List<String> = emptyList()
    override fun initialize(constraintAnnotation: PageSortKeyCheck) {
        sortKeys = constraintAnnotation.sortKey.java.enumConstants.map { it.key }
    }

    override fun isValid(value: Pageable, context: ConstraintValidatorContext): Boolean {
        var result = true
        val notValidKeys = mutableListOf<String>()
        val sort = value.sort
        if (sort.isEmpty) {
            return true
        }

        for (order in sort) {
            if (!sortKeys.contains(order.property)) {
                notValidKeys.add(order.property)
                result = false
            }
        }

        if (!result) {
            val message = StringBuilder("Invalid sort key(s): ")
            message.append(notValidKeys.joinToString(","))
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(message.toString()).addConstraintViolation()
        }
        return result
    }


}