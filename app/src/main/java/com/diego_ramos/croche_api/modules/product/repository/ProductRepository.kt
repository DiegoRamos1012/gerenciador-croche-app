package com.diego_ramos.croche_api.modules.product.repository

import com.diego_ramos.croche_api.modules.product.domain.Product
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface ProductRepository { // Contrato, sem persistência

    suspend fun save(product: Product)

    suspend fun findById(id: Uuid): Product?

    suspend fun existsByNameAndColorAndSize(
        name: String,
        color: String,
        size: String
    ): Boolean

    suspend fun findAllActive(): List<Product>

    suspend fun findAllInactive(): List<Product>
}
