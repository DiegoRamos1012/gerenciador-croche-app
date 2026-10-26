package com.diego_ramos.croche_api.modules.product.data.repository

import com.diego_ramos.croche_api.modules.product.data.local.dao.ProductDao
import com.diego_ramos.croche_api.modules.product.data.local.entity.toDomain
import com.diego_ramos.croche_api.modules.product.data.local.entity.toEntity
import com.diego_ramos.croche_api.modules.product.domain.Product
import com.diego_ramos.croche_api.modules.product.repository.ProductRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProductRepositoryImpl(
    private val productDao: ProductDao
) : ProductRepository {

    override suspend fun save(product: Product) {
        val entity = product.toEntity()
        productDao.upsert(entity)
    }

    override suspend fun findById(id: Uuid): Product? {
        val entity = productDao.findById(id.toString()) ?: return null
        return entity.toDomain()
    }

    override suspend fun existsByNameAndColorAndSize(
        name: String,
        color: String,
        size: String
    ): Boolean {
        return productDao.existsByNameAndColorAndSize(
            name = name,
            color = color,
            size = size
        )
    }

    override suspend fun findAllActive(): List<Product> {
        return productDao.findAllActive().map { it.toDomain() }
    }

    override suspend fun findAllInactive(): List<Product> {
        return productDao.findAllInactive().map { it.toDomain() }
    }
}
