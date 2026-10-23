package com.diego_ramos.croche_api.modules.orderItem.domain

import com.diego_ramos.croche_api.common.base.BaseEntity
import com.diego_ramos.croche_api.common.exception.DomainException
import java.math.BigDecimal
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class OrderItem private constructor(
    val orderId: Uuid,
    val productId: Uuid,
    val quantity: Int,
    val unitPrice: BigDecimal
) : BaseEntity() {

    val subtotal: BigDecimal
        get() = unitPrice.multiply(BigDecimal(quantity))

    companion object {
        fun create(
            orderId: Uuid,
            productId: Uuid,
            quantity: Int,
            unitPrice: BigDecimal
        ): OrderItem {

            if (quantity <= 0) throw DomainException("A quantidade deve ser maior que zero.")

            if (unitPrice <= BigDecimal.ZERO) throw DomainException("O preço unitário deve ser maior que zero.")

            return OrderItem(
                orderId = orderId,
                productId = productId,
                quantity = quantity,
                unitPrice = unitPrice)
        }
    }
}