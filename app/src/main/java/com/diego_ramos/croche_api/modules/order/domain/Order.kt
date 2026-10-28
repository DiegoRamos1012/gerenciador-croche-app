package com.diego_ramos.croche_api.modules.order.domain

import com.diego_ramos.croche_api.common.base.BaseEntity
import com.diego_ramos.croche_api.common.exception.DomainException
import com.diego_ramos.croche_api.common.util.TextNormalizer
import com.diego_ramos.croche_api.modules.orderItem.domain.OrderItem
import java.math.BigDecimal
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class Order private constructor(
    override val id: Uuid = Uuid.random(),
    var customerName: String,
    var customerPhone: String?,
    var customerAddress: String?,
    var shippingType: ShippingType = ShippingType.ENTREGA_PESSOAL,
    var paidAmount: BigDecimal = BigDecimal.ZERO,
    var isDelivered: Boolean = false,
    var status: OrderStatus = OrderStatus.CONCLUIDO,
    var items: List<OrderItem>
) : BaseEntity(id) {

    val downPayment: BigDecimal
        get() = paidAmount

    val total: BigDecimal
        get() = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.subtotal) }

    val remainingBalance: BigDecimal
        get() {
            val result = total.subtract(paidAmount)
            return if (result < BigDecimal.ZERO) BigDecimal.ZERO else result
        }

    val isFullyPaid: Boolean
        get() = paidAmount >= total && total > BigDecimal.ZERO

    fun addPayment(amount: BigDecimal) {
        if (amount <= BigDecimal.ZERO) {
            throw DomainException("O valor do pagamento deve ser maior que zero.")
        }
        paidAmount = paidAmount.add(amount)
        markAsUpdated()
    }

    fun updateDetails(
        customerName: String,
        customerPhone: String?,
        customerAddress: String?,
        shippingType: ShippingType,
        paidAmount: BigDecimal,
        isDelivered: Boolean,
        items: List<OrderItem>
    ) {
        if (customerName.isBlank()) {
            throw DomainException("O nome do cliente é obrigatório.")
        }
        if (items.isEmpty()) {
            throw DomainException("O pedido deve conter pelo menos um item.")
        }
        if (paidAmount < BigDecimal.ZERO) {
            throw DomainException("O valor pago não pode ser negativo.")
        }

        this.customerName = TextNormalizer.normalizeName(customerName)
        this.customerPhone = customerPhone?.trim()
        this.customerAddress = customerAddress?.trim()
        this.shippingType = shippingType
        this.paidAmount = paidAmount
        this.isDelivered = isDelivered
        this.items = items
        markAsUpdated()
    }

    fun toggleDeliveryStatus() {
        isDelivered = !isDelivered
        markAsUpdated()
    }

    fun cancel() {
        if (status == OrderStatus.CANCELADO) {
            throw DomainException("O pedido já está cancelado.")
        }
        status = OrderStatus.CANCELADO
        markAsUpdated()
    }

    companion object {
        fun create(
            id: Uuid = Uuid.random(),
            customerName: String,
            customerPhone: String?,
            customerAddress: String?,
            shippingType: ShippingType,
            downPayment: BigDecimal?,
            isDelivered: Boolean = false,
            items: List<OrderItem>
        ): Order {
            if (customerName.isBlank()) {
                throw DomainException("O nome do cliente é obrigatório.")
            }
            if (items.isEmpty()) {
                throw DomainException("O pedido deve conter pelo menos um item.")
            }
            if (downPayment != null && downPayment < BigDecimal.ZERO) {
                throw DomainException("O pagamento de sinal não pode ser negativo.")
            }

            return Order(
                id = id,
                customerName = TextNormalizer.normalizeName(customerName),
                customerPhone = customerPhone?.trim(),
                customerAddress = customerAddress?.trim(),
                shippingType = shippingType,
                paidAmount = downPayment ?: BigDecimal.ZERO,
                isDelivered = isDelivered,
                status = OrderStatus.CONCLUIDO,
                items = items
            )
        }

        fun restore(
            id: Uuid,
            customerName: String,
            customerPhone: String?,
            customerAddress: String?,
            shippingType: ShippingType,
            paidAmount: BigDecimal,
            isDelivered: Boolean,
            status: OrderStatus,
            items: List<OrderItem>,
            createdAt: Instant,
            updatedAt: Instant
        ): Order {
            val order = Order(
                id = id,
                customerName = customerName,
                customerPhone = customerPhone,
                customerAddress = customerAddress,
                shippingType = shippingType,
                paidAmount = paidAmount,
                isDelivered = isDelivered,
                status = status,
                items = items
            )
            order.restoreTimestamps(createdAt, updatedAt)
            return order
        }
    }
}
