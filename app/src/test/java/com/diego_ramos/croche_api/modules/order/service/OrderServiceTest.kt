package com.diego_ramos.croche_api.modules.order.service

import com.diego_ramos.croche_api.modules.order.domain.Order
import com.diego_ramos.croche_api.modules.order.domain.OrderStatus
import com.diego_ramos.croche_api.modules.order.error.OrderErrors
import com.diego_ramos.croche_api.modules.order.repository.OrderRepository
import com.diego_ramos.croche_api.modules.product.domain.Product
import com.diego_ramos.croche_api.modules.product.service.FakeProductRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class FakeOrderRepository : OrderRepository {
    val orders = mutableListOf<Order>()

    override suspend fun save(order: Order) {
        val index = orders.indexOfFirst { it.id == order.id }
        if (index >= 0) {
            orders[index] = order
        } else {
            orders.add(order)
        }
    }

    override suspend fun findById(id: Uuid): Order? {
        return orders.find { it.id == id }
    }

    override suspend fun findAll(): List<Order> {
        return orders.toList()
    }

    override suspend fun updateStatus(id: Uuid, status: OrderStatus) {
        val order = orders.find { it.id == id }
        if (order != null) {
            order.status = status
        }
    }

    override suspend fun updateDeliveryStatus(id: Uuid, isDelivered: Boolean) {
        val order = orders.find { it.id == id }
        if (order != null) {
            order.isDelivered = isDelivered
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
class OrderServiceTest {

    private lateinit var fakeOrderRepository: FakeOrderRepository
    private lateinit var fakeProductRepository: FakeProductRepository
    private lateinit var orderService: OrderService

    @Before
    fun setUp() {
        fakeOrderRepository = FakeOrderRepository()
        fakeProductRepository = FakeProductRepository()
        orderService = OrderService(fakeOrderRepository, fakeProductRepository)
    }

    @Test
    fun createOrder_withValidData_shouldCalculateTotalsAndSaveOrder() = runTest {
        val product = Product.create(
            name = "Tapete de Crochê",
            description = "Lindo tapete",
            color = "Azul",
            size = "Médio",
            price = BigDecimal("50.00"),
            photoPath = "product_photos/tapete.jpg"
        )
        fakeProductRepository.save(product)

        val result = orderService.createOrder(
            customerName = "Maria Silva",
            customerPhone = "11999998888",
            customerAddress = "Rua das Flores, 123",
            itemsInput = listOf(
                CreateOrderItemInput(productId = product.id, quantity = 2)
            )
        )

        assertTrue(result.isSuccess)
        val order = result.value
        assertEquals("Maria Silva", order.customerName)
        assertEquals("11999998888", order.customerPhone)
        assertEquals("Rua das Flores, 123", order.customerAddress)
        assertEquals(1, order.items.size)

        val item = order.items.first()
        assertEquals(product.id, item.productId)
        assertEquals("Tapete de Crochê", item.productName)
        assertEquals("Azul", item.productColor)
        assertEquals("Médio", item.productSize)
        assertEquals(BigDecimal("50.00"), item.unitPrice)
        assertEquals(2, item.quantity)
        assertEquals(BigDecimal("100.00"), item.subtotal)
        assertEquals(BigDecimal("100.00"), order.total)

        assertEquals(1, fakeOrderRepository.orders.size)
    }

    @Test
    fun createOrder_historicalPricePreservation_shouldRetainPriceWhenCatalogPriceChanges() = runTest {
        val product = Product.create(
            name = "Bolsa de Crochê",
            description = "Bolsa",
            color = "Rosa",
            size = "G",
            price = BigDecimal("80.00"),
            photoPath = "product_photos/bolsa.jpg"
        )
        fakeProductRepository.save(product)

        val orderResult = orderService.createOrder(
            customerName = "Ana Souza",
            customerPhone = null,
            customerAddress = null,
            itemsInput = listOf(
                CreateOrderItemInput(productId = product.id, quantity = 1)
            )
        )
        assertTrue(orderResult.isSuccess)
        val createdOrder = orderResult.value
        assertEquals(BigDecimal("80.00"), createdOrder.total)

        val updatedProduct = Product.restore(
            id = product.id,
            name = product.name,
            description = product.description,
            color = product.color,
            size = product.size,
            price = BigDecimal("120.00"),
            photoPath = product.photoPath,
            isActive = product.isActive,
            createdAt = product.createdAt,
            updatedAt = product.updatedAt
        )
        fakeProductRepository.save(updatedProduct)

        val retrievedOrder = fakeOrderRepository.findById(createdOrder.id)
        requireNotNull(retrievedOrder)
        assertEquals(BigDecimal("80.00"), retrievedOrder.items.first().unitPrice)
        assertEquals(BigDecimal("80.00"), retrievedOrder.total)
    }

    @Test
    fun createOrder_whenProductIsInactive_shouldReturnProductInactiveError() = runTest {
        val product = Product.create(
            name = "Caminho de Mesa",
            description = null,
            color = "Branco",
            size = "G",
            price = BigDecimal("90.00"),
            photoPath = "product_photos/caminho.jpg"
        )
        product.deactivate()
        fakeProductRepository.save(product)

        val result = orderService.createOrder(
            customerName = "Carla",
            customerPhone = null,
            customerAddress = null,
            itemsInput = listOf(
                CreateOrderItemInput(productId = product.id, quantity = 1)
            )
        )

        assertTrue(result.isFailure)
        assertEquals(OrderErrors.ProductInactive.code, result.error.code)
    }

    @Test
    fun createOrder_withEmptyItems_shouldReturnEmptyItemsError() = runTest {
        val result = orderService.createOrder(
            customerName = "Fernanda",
            customerPhone = null,
            customerAddress = null,
            itemsInput = emptyList()
        )

        assertTrue(result.isFailure)
        assertEquals(OrderErrors.EmptyItems.code, result.error.code)
    }

    @Test
    fun cancelOrder_whenActive_shouldUpdateStatusToCancelled() = runTest {
        val product = Product.create(
            name = "Sousplat",
            description = null,
            color = "Verde",
            size = "Padrão",
            price = BigDecimal("30.00"),
            photoPath = "product_photos/sousplat.jpg"
        )
        fakeProductRepository.save(product)

        val createResult = orderService.createOrder(
            customerName = "Juliana",
            customerPhone = null,
            customerAddress = null,
            itemsInput = listOf(
                CreateOrderItemInput(productId = product.id, quantity = 3)
            )
        )
        val order = createResult.value

        val cancelResult = orderService.cancelOrder(order.id)
        assertTrue(cancelResult.isSuccess)
        assertEquals(OrderStatus.CANCELADO, cancelResult.value.status)
    }

    @Test
    fun cancelOrder_whenAlreadyCancelled_shouldReturnError() = runTest {
        val product = Product.create(
            name = "Sousplat",
            description = null,
            color = "Verde",
            size = "Padrão",
            price = BigDecimal("30.00"),
            photoPath = "product_photos/sousplat.jpg"
        )
        fakeProductRepository.save(product)

        val createResult = orderService.createOrder(
            customerName = "Juliana",
            customerPhone = null,
            customerAddress = null,
            itemsInput = listOf(
                CreateOrderItemInput(productId = product.id, quantity = 1)
            )
        )
        val order = createResult.value

        orderService.cancelOrder(order.id)
        val secondCancel = orderService.cancelOrder(order.id)

        assertTrue(secondCancel.isFailure)
        assertEquals(OrderErrors.AlreadyCancelled.code, secondCancel.error.code)
    }
}
