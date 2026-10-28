package com.diego_ramos.croche_api.modules.orderItem.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.diego_ramos.croche_api.modules.order.data.local.entity.OrderEntity
import com.diego_ramos.croche_api.modules.orderItem.domain.OrderItem
import java.math.BigDecimal
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["order_id"]),
        Index(value = ["product_id"])
    ]
)
data class OrderItemEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "order_id")
    val orderId: String,

    @ColumnInfo(name = "product_id")
    val productId: String,

    @ColumnInfo(name = "product_name")
    val productName: String,

    @ColumnInfo(name = "product_color")
    val productColor: String,

    @ColumnInfo(name = "product_size")
    val productSize: String,

    @ColumnInfo(name = "unit_price")
    val unitPrice: String,

    @ColumnInfo(name = "quantity")
    val quantity: Int,

    @ColumnInfo(name = "subtotal")
    val subtotal: String,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)

@OptIn(ExperimentalUuidApi::class)
fun OrderItem.toEntity(): OrderItemEntity {
    return OrderItemEntity(
        id = id.toString(),
        orderId = orderId.toString(),
        productId = productId.toString(),
        productName = productName,
        productColor = productColor,
        productSize = productSize,
        unitPrice = unitPrice.toPlainString(),
        quantity = quantity,
        subtotal = subtotal.toPlainString(),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}

@OptIn(ExperimentalUuidApi::class)
fun OrderItemEntity.toDomain(): OrderItem {
    return OrderItem.restore(
        id = Uuid.parse(id),
        orderId = Uuid.parse(orderId),
        productId = Uuid.parse(productId),
        productName = productName,
        productColor = productColor,
        productSize = productSize,
        unitPrice = BigDecimal(unitPrice),
        quantity = quantity,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )
}
