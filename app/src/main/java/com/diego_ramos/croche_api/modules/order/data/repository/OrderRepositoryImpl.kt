package com.diego_ramos.croche_api.modules.order.data.repository

import com.diego_ramos.croche_api.modules.order.data.local.dao.OrderDao
import com.diego_ramos.croche_api.modules.order.data.local.entity.toDomain
import com.diego_ramos.croche_api.modules.order.data.local.entity.toEntity
import com.diego_ramos.croche_api.modules.order.domain.Order
import com.diego_ramos.croche_api.modules.order.domain.OrderStatus
import com.diego_ramos.croche_api.modules.order.repository.OrderRepository
import com.diego_ramos.croche_api.modules.orderItem.data.local.entity.toEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class OrderRepositoryImpl(
    private val orderDao: OrderDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : OrderRepository {

    override suspend fun save(order: Order): Unit = withContext(ioDispatcher) {
        val orderEntity = order.toEntity()
        val itemEntities = order.items.map { it.toEntity() }
        orderDao.insertOrderWithItems(orderEntity, itemEntities)
    }

    override suspend fun findById(id: Uuid): Order? = withContext(ioDispatcher) {
        val orderWithItems = orderDao.findByIdWithItems(id.toString()) ?: return@withContext null
        orderWithItems.toDomain()
    }

    override suspend fun findAll(): List<Order> = withContext(ioDispatcher) {
        orderDao.findAllWithItems().map { it.toDomain() }
    }

    override suspend fun updateStatus(id: Uuid, status: OrderStatus): Unit = withContext(ioDispatcher) {
        orderDao.updateStatus(
            id = id.toString(),
            status = status.name,
            updatedAt = Instant.now().toString()
        )
    }

    override suspend fun updateDeliveryStatus(id: Uuid, isDelivered: Boolean): Unit = withContext(ioDispatcher) {
        orderDao.updateDeliveryStatus(
            id = id.toString(),
            isDelivered = isDelivered,
            updatedAt = Instant.now().toString()
        )
    }
}
