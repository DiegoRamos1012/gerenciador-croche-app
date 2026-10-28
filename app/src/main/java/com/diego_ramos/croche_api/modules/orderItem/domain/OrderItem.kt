package com.diego_ramos.croche_api.modules.orderItem.domain

import com.diego_ramos.croche_api.common.base.BaseEntity
import com.diego_ramos.croche_api.common.exception.DomainException
import java.math.BigDecimal
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class OrderItem private constructor(
    override val id: Uuid = Uuid.random(),
    val orderId: Uuid,
    val productId: Uuid,
    val productName: String,
    val productColor: String,
    val productSize: String,
    val unitPrice: BigDecimal,
    val quantity: Int
) : BaseEntity(id) {

    val subtotal: BigDecimal
        get() = unitPrice.multiply(BigDecimal(quantity))

    companion object {
        fun create(
            orderId: Uuid,
            productId: Uuid,
            productName: String,
            productColor: String,
            productSize: String,
            unitPrice: BigDecimal,
            quantity: Int
        ): OrderItem {
            if (productName.isBlank()) throw DomainException("O nome do produto é obrigatório no item.")
            if (productColor.isBlank()) throw DomainException("A cor do produto é obrigatória no item.")
            if (productSize.isBlank()) throw DomainException("O tamanho do produto é obrigatório no item.")
            if (quantity <= 0) throw DomainException("A quantidade do item deve ser maior que zero.")
            if (unitPrice <= BigDecimal.ZERO) throw DomainException("O preço unitário deve ser maior que zero.")

            return OrderItem(
                orderId = orderId,
                productId = productId,
                productName = productName.trim(),
                productColor = productColor.trim(),
                productSize = productSize.trim(),
                unitPrice = unitPrice,
                quantity = quantity
            )
        }

        fun restore(
            id: Uuid,
            orderId: Uuid,
            productId: Uuid,
            productName: String,
            productColor: String,
            productSize: String,
            unitPrice: BigDecimal,
            quantity: Int,
            createdAt: Instant,
            updatedAt: Instant
        ): OrderItem {
            val item = OrderItem(
                id = id,
                orderId = orderId,
                productId = productId,
                productName = productName,
                productColor = productColor,
                productSize = productSize,
                unitPrice = unitPrice,
                quantity = quantity
            )
            item.restoreTimestamps(createdAt, updatedAt)
            return item
        }
    }
}
