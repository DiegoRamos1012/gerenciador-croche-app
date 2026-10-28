package com.diego_ramos.croche_api.modules.order.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.diego_ramos.croche_api.modules.order.data.local.entity.OrderEntity
import com.diego_ramos.croche_api.modules.order.data.local.entity.OrderWithItems
import com.diego_ramos.croche_api.modules.orderItem.data.local.entity.OrderItemEntity

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Transaction
    suspend fun insertOrderWithItems(order: OrderEntity, items: List<OrderItemEntity>) {
        insertOrder(order)
        insertOrderItems(items)
    }

    @Transaction
    @Query("SELECT * FROM orders ORDER BY created_at DESC")
    suspend fun findAllWithItems(): List<OrderWithItems>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun findByIdWithItems(id: String): OrderWithItems?

    @Query("UPDATE orders SET status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: String)

    @Query("UPDATE orders SET is_delivered = :isDelivered, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateDeliveryStatus(id: String, isDelivered: Boolean, updatedAt: String)
}
