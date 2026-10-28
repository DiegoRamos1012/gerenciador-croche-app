package com.diego_ramos.croche_api.modules.order.service

import com.diego_ramos.croche_api.common.exception.DomainException
import com.diego_ramos.croche_api.common.result.Result
import com.diego_ramos.croche_api.modules.order.domain.Order
import com.diego_ramos.croche_api.modules.order.domain.OrderStatus
import com.diego_ramos.croche_api.modules.order.domain.ShippingType
import com.diego_ramos.croche_api.modules.order.error.OrderErrors
import com.diego_ramos.croche_api.modules.order.repository.OrderRepository
import com.diego_ramos.croche_api.modules.orderItem.domain.OrderItem
import com.diego_ramos.croche_api.modules.product.repository.ProductRepository
import java.math.BigDecimal
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class CreateOrderItemInput(
    val productId: Uuid,
    val quantity: Int,
    val unitPrice: BigDecimal? = null
)

@OptIn(ExperimentalUuidApi::class)
class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository
) {

    suspend fun createOrder(
        customerName: String,
        customerPhone: String?,
        customerAddress: String?,
        shippingType: ShippingType = ShippingType.ENTREGA_PESSOAL,
        downPayment: BigDecimal? = null,
        isDelivered: Boolean = false,
        itemsInput: List<CreateOrderItemInput>
    ): Result<Order> {
        return try {
            if (customerName.trim().isEmpty()) {
                return Result.failure(OrderErrors.CustomerNameRequired)
            }
            if (itemsInput.isEmpty()) {
                return Result.failure(OrderErrors.EmptyItems)
            }

            val orderId = Uuid.random()
            val orderItems = mutableListOf<OrderItem>()

            for (input in itemsInput) {
                val product = productRepository.findById(input.productId)
                    ?: return Result.failure(OrderErrors.ProductNotFound)

                if (!product.isActive) {
                    return Result.failure(OrderErrors.ProductInactive)
                }

                val priceToUse = input.unitPrice ?: product.price

                val orderItem = OrderItem.create(
                    orderId = orderId,
                    productId = product.id,
                    productName = product.name,
                    productColor = product.color,
                    productSize = product.size,
                    unitPrice = priceToUse,
                    quantity = input.quantity
                )
                orderItems.add(orderItem)
            }

            val order = Order.create(
                id = orderId,
                customerName = customerName,
                customerPhone = customerPhone,
                customerAddress = customerAddress,
                shippingType = shippingType,
                downPayment = downPayment,
                isDelivered = isDelivered,
                items = orderItems
            )

            orderRepository.save(order)

            Result.success(order)
        } catch (ex: DomainException) {
            Result.failure(
                code = "ORDER_INVALID_INPUT",
                message = ex.message ?: "Os dados do pedido são inválidos."
            )
        } catch (ex: Exception) {
            Result.failure(
                code = "ORDER_SAVE_ERROR",
                message = "Erro ao salvar o pedido: ${ex.localizedMessage ?: "Falha ao gravar no banco de dados."}"
            )
        }
    }

    suspend fun updateOrder(
        orderId: Uuid,
        customerName: String,
        customerPhone: String?,
        customerAddress: String?,
        shippingType: ShippingType,
        paidAmount: BigDecimal,
        isDelivered: Boolean,
        itemsInput: List<CreateOrderItemInput>
    ): Result<Order> {
        return try {
            val order = orderRepository.findById(orderId)
                ?: return Result.failure(OrderErrors.NotFound)

            if (itemsInput.isEmpty()) {
                return Result.failure(OrderErrors.EmptyItems)
            }

            val orderItems = mutableListOf<OrderItem>()
            for (input in itemsInput) {
                val product = productRepository.findById(input.productId)
                    ?: return Result.failure(OrderErrors.ProductNotFound)

                val priceToUse = input.unitPrice ?: product.price

                val orderItem = OrderItem.create(
                    orderId = orderId,
                    productId = product.id,
                    productName = product.name,
                    productColor = product.color,
                    productSize = product.size,
                    unitPrice = priceToUse,
                    quantity = input.quantity
                )
                orderItems.add(orderItem)
            }

            order.updateDetails(
                customerName = customerName,
                customerPhone = customerPhone,
                customerAddress = customerAddress,
                shippingType = shippingType,
                paidAmount = paidAmount,
                isDelivered = isDelivered,
                items = orderItems
            )

            orderRepository.save(order)

            Result.success(order)
        } catch (ex: DomainException) {
            Result.failure(
                code = "ORDER_INVALID_INPUT",
                message = ex.message ?: "Os dados do pedido são inválidos."
            )
        } catch (ex: Exception) {
            Result.failure(
                code = "ORDER_SAVE_ERROR",
                message = "Erro ao atualizar o pedido: ${ex.localizedMessage ?: "Falha no banco de dados."}"
            )
        }
    }

    suspend fun addPayment(orderId: Uuid, paymentAmount: BigDecimal): Result<Order> {
        return try {
            val order = orderRepository.findById(orderId)
                ?: return Result.failure(OrderErrors.NotFound)

            order.addPayment(paymentAmount)
            orderRepository.save(order)

            Result.success(order)
        } catch (ex: DomainException) {
            Result.failure(
                code = "ORDER_INVALID_PAYMENT",
                message = ex.message ?: "Valor de pagamento inválido."
            )
        } catch (ex: Exception) {
            Result.failure(
                code = "ORDER_SAVE_ERROR",
                message = "Erro ao registrar pagamento: ${ex.localizedMessage ?: "Falha no banco de dados."}"
            )
        }
    }

    suspend fun findPendingDeliveryOrdersForProduct(productId: Uuid): Result<List<Order>> {
        val allOrders = orderRepository.findAll()
        val pendingOrders = allOrders.filter { order ->
            order.status != OrderStatus.CANCELADO &&
                !order.isDelivered &&
                order.items.any { it.productId == productId }
        }
        return Result.success(pendingOrders)
    }

    suspend fun findById(id: Uuid): Result<Order> {
        val order = orderRepository.findById(id)
            ?: return Result.failure(OrderErrors.NotFound)

        return Result.success(order)
    }

    suspend fun findAll(): Result<List<Order>> {
        val orders = orderRepository.findAll()
        return Result.success(orders)
    }

    suspend fun toggleDeliveryStatus(id: Uuid): Result<Order> {
        val order = orderRepository.findById(id)
            ?: return Result.failure(OrderErrors.NotFound)

        order.toggleDeliveryStatus()
        orderRepository.updateDeliveryStatus(order.id, order.isDelivered)

        return Result.success(order)
    }

    suspend fun cancelOrder(id: Uuid): Result<Order> {
        return try {
            val order = orderRepository.findById(id)
                ?: return Result.failure(OrderErrors.NotFound)

            order.cancel()
            orderRepository.updateStatus(order.id, order.status)

            Result.success(order)
        } catch (ex: DomainException) {
            Result.failure(OrderErrors.AlreadyCancelled)
        }
    }
}
