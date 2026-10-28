package com.diego_ramos.croche_api.modules.product.domain

import com.diego_ramos.croche_api.common.base.BaseEntity
import com.diego_ramos.croche_api.common.exception.DomainException
import com.diego_ramos.croche_api.common.util.TextNormalizer
import java.math.BigDecimal
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class Product private constructor(
    override val id: Uuid = Uuid.random(),
    var name: String,
    var description: String?,
    var color: String,
    var size: String,
    var price: BigDecimal,
    var photoPath: String,
    var isActive: Boolean = true
) : BaseEntity(id) {

    fun updateDetails(
        name: String,
        description: String?,
        color: String,
        size: String,
        price: BigDecimal,
        photoPath: String
    ) {
        if (name.isBlank()) throw DomainException("O nome do produto é obrigatório.")
        if (description != null && description.length > 300) {
            throw DomainException("A descrição não pode passar de 300 caracteres.")
        }
        if (color.isBlank()) throw DomainException("A cor do produto é obrigatória.")
        if (size.isBlank()) throw DomainException("O tamanho do produto é obrigatório.")
        if (price <= BigDecimal.ZERO) throw DomainException("O preço deve ser maior que zero.")
        if (photoPath.isBlank()) throw DomainException("A foto do produto é obrigatória.")

        this.name = TextNormalizer.normalizeName(name)
        this.description = description?.trim()
        this.color = color.trim()
        this.size = size.trim()
        this.price = price
        this.photoPath = photoPath.trim()
        markAsUpdated()
    }

    fun updatePhoto(newPhotoPath: String) {
        if (newPhotoPath.isBlank()) {
            throw DomainException("A foto do produto é obrigatória.")
        }
        photoPath = newPhotoPath
        markAsUpdated()
    }

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
            price: BigDecimal,
            photoPath: String
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

            if (photoPath.isBlank()) throw DomainException("A foto do produto é obrigatória.")

            return Product(
                name = TextNormalizer.normalizeName(name),
                description = description?.trim(),
                color = color.trim(),
                size = size.trim(),
                price = price,
                photoPath = photoPath.trim()
            )
        }

        fun restore(
            id: Uuid,
            name: String,
            description: String?,
            color: String,
            size: String,
            price: BigDecimal,
            photoPath: String,
            isActive: Boolean,
            createdAt: Instant,
            updatedAt: Instant
        ): Product {
            val product = Product(
                id = id,
                name = name,
                description = description,
                color = color,
                size = size,
                price = price,
                photoPath = photoPath,
                isActive = isActive
            )
            product.restoreTimestamps(createdAt, updatedAt)
            return product
        }
    }
}
