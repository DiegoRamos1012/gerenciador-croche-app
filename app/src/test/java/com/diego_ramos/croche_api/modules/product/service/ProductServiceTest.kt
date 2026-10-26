package com.diego_ramos.croche_api.modules.product.service

import com.diego_ramos.croche_api.modules.product.domain.Product
import com.diego_ramos.croche_api.modules.product.error.ProductErrors
import com.diego_ramos.croche_api.modules.product.repository.ProductRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class FakeProductRepository : ProductRepository {
    val products = mutableListOf<Product>()

    override suspend fun save(product: Product) {
        val existingIndex = products.indexOfFirst { it.id == product.id }
        if (existingIndex >= 0) {
            products[existingIndex] = product
        } else {
            products.add(product)
        }
    }

    override suspend fun findById(id: Uuid): Product? {
        return products.find { it.id == id }
    }

    override suspend fun existsByNameAndColorAndSize(
        name: String,
        color: String,
        size: String
    ): Boolean {
        return products.any {
            it.isActive &&
                it.name.equals(name, ignoreCase = true) &&
                it.color.equals(color, ignoreCase = true) &&
                it.size.equals(size, ignoreCase = true)
        }
    }

    override suspend fun findAllActive(): List<Product> {
        return products.filter { it.isActive }
    }

    override suspend fun findAllInactive(): List<Product> {
        return products.filter { !it.isActive }
    }
}

@OptIn(ExperimentalUuidApi::class)
class ProductServiceTest {

    private lateinit var fakeRepository: FakeProductRepository
    private lateinit var productService: ProductService

    @Before
    fun setUp() {
        fakeRepository = FakeProductRepository()
        productService = ProductService(fakeRepository)
    }

    @Test
    fun create_withValidData_shouldReturnSuccessAndSaveProduct() = runTest {
        val result = productService.create(
            name = "tapete de crochê",
            description = "Lindo tapete artesanal",
            color = "Azul",
            size = "Médio",
            price = BigDecimal("49.90")
        )

        assertTrue(result.isSuccess)
        val product = result.value
        assertEquals("Tapete de Crochê", product.name)
        assertEquals("Azul", product.color)
        assertEquals("Médio", product.size)
        assertEquals(BigDecimal("49.90"), product.price)
        assertTrue(product.isActive)

        assertEquals(1, fakeRepository.products.size)
    }

    @Test
    fun create_withBlankName_shouldReturnFailure() = runTest {
        val result = productService.create(
            name = "   ",
            description = "Descrição",
            color = "Azul",
            size = "Médio",
            price = BigDecimal("10.00")
        )

        assertTrue(result.isFailure)
        assertEquals("PRODUCT_INVALID_INPUT", result.error.code)
    }

    @Test
    fun create_withZeroOrNegativePrice_shouldReturnFailure() = runTest {
        val result = productService.create(
            name = "Bolsa",
            description = "Bolsa de praia",
            color = "Amarelo",
            size = "G",
            price = BigDecimal("0.00")
        )

        assertTrue(result.isFailure)
        assertEquals("PRODUCT_INVALID_INPUT", result.error.code)
    }

    @Test
    fun create_whenProductAlreadyExistsAndIsActive_shouldReturnAlreadyExistsError() = runTest {
        productService.create(
            name = "Sousplat",
            description = "Mesa posta",
            color = "Vermelho",
            size = "Único",
            price = BigDecimal("25.00")
        )

        val resultDuplicate = productService.create(
            name = "Sousplat",
            description = "Outro sousplat",
            color = "Vermelho",
            size = "Único",
            price = BigDecimal("30.00")
        )

        assertTrue(resultDuplicate.isFailure)
        assertEquals(ProductErrors.AlreadyExists.code, resultDuplicate.error.code)
    }

    @Test
    fun create_whenProductExistsButIsInactive_shouldAllowCreation() = runTest {
        val firstResult = productService.create(
            name = "Amigurumi Urso",
            description = "Urso de pelúcia",
            color = "Marrom",
            size = "Pequeno",
            price = BigDecimal("60.00")
        )
        val firstProduct = firstResult.value
        firstProduct.deactivate()
        fakeRepository.save(firstProduct)

        val secondResult = productService.create(
            name = "Amigurumi Urso",
            description = "Novo urso marrom",
            color = "Marrom",
            size = "Pequeno",
            price = BigDecimal("65.00")
        )

        assertTrue(secondResult.isSuccess)
        assertEquals(2, fakeRepository.products.size)
    }

    @Test
    fun findById_whenExists_shouldReturnSuccess() = runTest {
        val createResult = productService.create(
            name = "Caminho de Mesa",
            description = null,
            color = "Branco",
            size = "1,5m",
            price = BigDecimal("80.00")
        )

        val savedProduct = createResult.value
        val findResult = productService.findById(savedProduct.id)

        assertTrue(findResult.isSuccess)
        assertEquals(savedProduct.id, findResult.value.id)
    }

    @Test
    fun findById_whenNotExists_shouldReturnNotFound() = runTest {
        val randomId = Uuid.random()
        val findResult = productService.findById(randomId)

        assertTrue(findResult.isFailure)
        assertEquals(ProductErrors.NotFound.code, findResult.error.code)
    }

    @Test
    fun findAllActiveAndInactive_shouldFilterCorrectly() = runTest {
        productService.create(
            name = "Produto 1",
            description = null,
            color = "Azul",
            size = "P",
            price = BigDecimal("10.00")
        )
        val p2Result = productService.create(
            name = "Produto 2",
            description = null,
            color = "Verde",
            size = "M",
            price = BigDecimal("20.00")
        )

        val p2 = p2Result.value
        p2.deactivate()
        fakeRepository.save(p2)

        val activeResult = productService.findAllActive()
        val inactiveResult = productService.findAllInactive()

        assertTrue(activeResult.isSuccess)
        assertEquals(1, activeResult.value.size)
        assertEquals("Produto 1", activeResult.value.first().name)

        assertTrue(inactiveResult.isSuccess)
        assertEquals(1, inactiveResult.value.size)
        assertEquals("Produto 2", inactiveResult.value.first().name)
    }
}
