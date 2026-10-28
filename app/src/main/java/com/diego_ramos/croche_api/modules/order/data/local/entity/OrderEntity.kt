package com.diego_ramos.croche_api.modules.order.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diego_ramos.croche_api.modules.order.domain.Order
import com.diego_ramos.croche_api.modules.order.domain.OrderStatus
import com.diego_ramos.croche_api.modules.order.domain.ShippingType
import com.diego_ramos.croche_api.modules.orderItem.data.local.entity.toDomain
import java.math.BigDecimal
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "customer_name")
    val customerName: String,

    @ColumnInfo(name = "customer_phone")
    val customerPhone: String?,

    @ColumnInfo(name = "customer_address")
    val customerAddress: String?,

    @ColumnInfo(name = "shipping_type")
    val shippingType: String,

    @ColumnInfo(name = "down_payment")
    val downPayment: String?,

    @ColumnInfo(name = "paid_amount")
    val paidAmount: String? = null,

    @ColumnInfo(name = "is_delivered")
    val isDelivered: Boolean,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "total")
    val total: String,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)

@OptIn(ExperimentalUuidApi::class)
fun Order.toEntity(): OrderEntity {
    return OrderEntity(
        id = id.toString(),
        customerName = customerName,
        customerPhone = customerPhone,
        customerAddress = customerAddress,
        shippingType = shippingType.name,
        downPayment = paidAmount.toPlainString(),
        paidAmount = paidAmount.toPlainString(),
        isDelivered = isDelivered,
        status = status.name,
        total = total.toPlainString(),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}

@OptIn(ExperimentalUuidApi::class)
fun OrderWithItems.toDomain(): Order {
    val domainItems = items.map { it.toDomain() }
    val parsedShippingType = try {
        ShippingType.valueOf(order.shippingType)
    } catch (_: Exception) {
        ShippingType.ENTREGA_PESSOAL
    }

    val amountPaid = when {
        !order.paidAmount.isNullOrBlank() -> BigDecimal(order.paidAmount)
        !order.downPayment.isNullOrBlank() -> BigDecimal(order.downPayment)
        else -> BigDecimal.ZERO
    }

    return Order.restore(
        id = Uuid.parse(order.id),
        customerName = order.customerName,
        customerPhone = order.customerPhone,
        customerAddress = order.customerAddress,
        shippingType = parsedShippingType,
        paidAmount = amountPaid,
        isDelivered = order.isDelivered,
        status = OrderStatus.valueOf(order.status),
        items = domainItems,
        createdAt = Instant.parse(order.createdAt),
        updatedAt = Instant.parse(order.updatedAt)
    )
}
