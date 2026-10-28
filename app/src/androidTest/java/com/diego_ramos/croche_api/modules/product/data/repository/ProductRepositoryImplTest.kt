package com.diego_ramos.croche_api.modules.product.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diego_ramos.croche_api.config.database.AppDatabase
import com.diego_ramos.croche_api.modules.product.domain.Product
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@RunWith(AndroidJUnit4::class)
class ProductRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ProductRepositoryImpl

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ProductRepositoryImpl(db.productDao())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun save_and_findById_shouldPersistAndRestoreProductCorrectly() = runTest {
        val product = Product.create(
            name = "Jogo de Banheiro",
            description = "Conjunto com 3 peças de crochê",
            color = "Rosa",
            size = "Padrão",
            price = BigDecimal("120.50"),
            photoPath = "product_photos/jogo_banheiro.jpg"
        )

        repository.save(product)

        val retrieved = repository.findById(product.id)

        assertNotNull(retrieved)
        requireNotNull(retrieved)

        assertEquals(product.id, retrieved.id)
        assertEquals("Jogo de Banheiro", retrieved.name)
        assertEquals("Conjunto com 3 peças de crochê", retrieved.description)
        assertEquals("Rosa", retrieved.color)
        assertEquals("Padrão", retrieved.size)
        assertEquals(BigDecimal("120.50"), retrieved.price)
        assertEquals("product_photos/jogo_banheiro.jpg", retrieved.photoPath)
        assertTrue(retrieved.isActive)
        assertEquals(product.createdAt, retrieved.createdAt)
        assertEquals(product.updatedAt, retrieved.updatedAt)
    }

    @Test
    fun findById_whenNotExists_shouldReturnNull() = runTest {
        val retrieved = repository.findById(Uuid.random())
        assertNull(retrieved)
    }

    @Test
    fun existsByNameAndColorAndSize_shouldOnlyConsiderActiveProducts() = runTest {
        val product = Product.create(
            name = "Manta de Bebê",
            description = "Manta artesanal",
            color = "Amarelo",
            size = "Único",
            price = BigDecimal("150.00"),
            photoPath = "product_photos/manta.jpg"
        )

        repository.save(product)

        val existsActive = repository.existsByNameAndColorAndSize(
            name = "manta de bebê",
            color = "amarelo",
            size = "único"
        )
        assertTrue(existsActive)

        product.deactivate()
        repository.save(product)

        val existsInactive = repository.existsByNameAndColorAndSize(
            name = "manta de bebê",
            color = "amarelo",
            size = "único"
        )
        assertFalse(existsInactive)
    }

    @Test
    fun findAllActive_and_findAllInactive_shouldReturnCorrectLists() = runTest {
        val activeProduct = Product.create(
            name = "Chaveiro Coração",
            description = null,
            color = "Vermelho",
            size = "Pequeno",
            price = BigDecimal("15.00"),
            photoPath = "product_photos/chaveiro1.jpg"
        )

        val inactiveProduct = Product.create(
            name = "Chaveiro Urso",
            description = null,
            color = "Bege",
            size = "Pequeno",
            price = BigDecimal("18.00"),
            photoPath = "product_photos/chaveiro2.jpg"
        )
        inactiveProduct.deactivate()

        repository.save(activeProduct)
        repository.save(inactiveProduct)

        val activeList = repository.findAllActive()
        val inactiveList = repository.findAllInactive()

        assertEquals(1, activeList.size)
        assertEquals("Chaveiro Coração", activeList.first().name)

        assertEquals(1, inactiveList.size)
        assertEquals("Chaveiro Urso", inactiveList.first().name)
    }
}
