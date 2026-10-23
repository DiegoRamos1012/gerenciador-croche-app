package com.diego_ramos.croche_api.modules.product.domain

import com.diego_ramos.croche_api.common.base.BaseEntity
import com.diego_ramos.croche_api.common.exception.DomainException
import com.diego_ramos.croche_api.common.util.TextNormalizer
import java.math.BigDecimal
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class Product private constructor(
    val name: String,
    val description: String?,
    val color: String,
    val size: String,
    val price: BigDecimal,
    var isActive: Boolean = true // Deixando isActive como var deixa o valor mutável, permitindo alterações.
) : BaseEntity() {

    fun deactivate() {
        if (!isActive) {
            throw DomainException("O produto já está desativado.")
        }

        isActive = false
        markAsUpdated()
    }

    fun activate() {
        if (isActive) {
            throw DomainException("O produto já está ativo.")
        }

        isActive = true
        markAsUpdated()
    }

    companion object {
        fun create(
            name: String,
            description: String?,
            color: String,
            size: String,
            price: BigDecimal
        ): Product {

            if (name.isBlank()) throw DomainException("O nome do produto é obrigatório.")

            if (description != null && description.length > 300) {
                throw DomainException("A descrição não pode passar de 300 caracteres.")
            }

            if (color.isBlank()) {
                throw DomainException("A cor do produto é obrigatória.")
            }

            if (size.isBlank()) {
                throw DomainException("O tamanho do produto é obrigatório.")
            }

            if (price <= BigDecimal.ZERO) throw DomainException("O preço deve ser maior que zero.")

            return Product(
                name = TextNormalizer.normalizeName(name),
                description = description?.trim(),
                color = color.trim(),
                size = size.trim(),
                price = price
            )
        }
    }
}