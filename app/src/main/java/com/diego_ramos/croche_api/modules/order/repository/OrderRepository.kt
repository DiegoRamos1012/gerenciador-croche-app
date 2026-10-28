package com.diego_ramos.croche_api.modules.order.repository

import com.diego_ramos.croche_api.modules.order.domain.Order
import com.diego_ramos.croche_api.modules.order.domain.OrderStatus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface OrderRepository {

    suspend fun save(order: Order)

    suspend fun findById(id: Uuid): Order?

    suspend fun findAll(): List<Order>

    suspend fun updateStatus(id: Uuid, status: OrderStatus)

    suspend fun updateDeliveryStatus(id: Uuid, isDelivered: Boolean)
}
