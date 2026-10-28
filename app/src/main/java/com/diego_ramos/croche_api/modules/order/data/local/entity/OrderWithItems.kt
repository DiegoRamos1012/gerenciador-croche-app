package com.diego_ramos.croche_api.modules.order.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.diego_ramos.croche_api.modules.orderItem.data.local.entity.OrderItemEntity

data class OrderWithItems(
    @Embedded
    val order: OrderEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "order_id"
    )
    val items: List<OrderItemEntity>
)
