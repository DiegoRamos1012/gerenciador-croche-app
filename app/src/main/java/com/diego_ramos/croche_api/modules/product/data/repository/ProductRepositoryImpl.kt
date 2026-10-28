package com.diego_ramos.croche_api.modules.product.data.repository

import com.diego_ramos.croche_api.modules.product.data.local.dao.ProductDao
import com.diego_ramos.croche_api.modules.product.data.local.entity.toDomain
import com.diego_ramos.croche_api.modules.product.data.local.entity.toEntity
import com.diego_ramos.croche_api.modules.product.domain.Product
import com.diego_ramos.croche_api.modules.product.repository.ProductRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ProductRepository {

    override suspend fun save(product: Product): Unit = withContext(ioDispatcher) {
        val entity = product.toEntity()
        productDao.upsert(entity)
    }

    override suspend fun findById(id: Uuid): Product? = withContext(ioDispatcher) {
        val entity = productDao.findById(id.toString()) ?: return@withContext null
        entity.toDomain()
    }

    override suspend fun existsByNameAndColorAndSize(
        name: String,
        color: String,
        size: String
    ): Boolean = withContext(ioDispatcher) {
        productDao.existsByNameAndColorAndSize(
            name = name,
            color = color,
            size = size
        )
    }

    override suspend fun findAllActive(): List<Product> = withContext(ioDispatcher) {
        productDao.findAllActive().map { it.toDomain() }
    }

    override suspend fun findAllInactive(): List<Product> = withContext(ioDispatcher) {
        productDao.findAllInactive().map { it.toDomain() }
    }

    override suspend fun deleteInactiveById(id: Uuid): Boolean = withContext(ioDispatcher) {
        val deletedCount = productDao.deleteInactiveById(id.toString())
        deletedCount > 0
    }

    override suspend fun deleteInactiveByIds(ids: List<Uuid>): Int = withContext(ioDispatcher) {
        if (ids.isEmpty()) return@withContext 0
        val stringIds = ids.map { it.toString() }
        productDao.deleteInactiveByIds(stringIds)
    }
}
