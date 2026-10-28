package com.diego_ramos.croche_api.modules.product.service

import com.diego_ramos.croche_api.common.exception.DomainException
import com.diego_ramos.croche_api.common.result.Result
import com.diego_ramos.croche_api.common.result.ResultError
import com.diego_ramos.croche_api.modules.product.data.local.image.ProductImageStorage
import com.diego_ramos.croche_api.modules.product.domain.Product
import com.diego_ramos.croche_api.modules.product.error.ProductErrors
import com.diego_ramos.croche_api.modules.product.repository.ProductRepository
import java.math.BigDecimal
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProductService(
    private val productRepository: ProductRepository
) {

    suspend fun create(
        name: String,
        description: String?,
        color: String,
        size: String,
        price: BigDecimal,
        photoPath: String
    ): Result<Product> {
        return try {
            val normalizedName = name.trim()
            val normalizedColor = color.trim()
            val normalizedSize = size.trim()

            val alreadyExists =
                productRepository.existsByNameAndColorAndSize(
                    name = normalizedName,
                    color = normalizedColor,
                    size = normalizedSize
                )

            if (alreadyExists) {
                return Result.failure(ProductErrors.AlreadyExists)
            }

            val product = Product.create(
                name = normalizedName,
                description = description,
                color = normalizedColor,
                size = normalizedSize,
                price = price,
                photoPath = photoPath
            )

            productRepository.save(product)

            Result.success(product)
        } catch (ex: DomainException) {
            Result.failure(
                code = "PRODUCT_INVALID_INPUT",
                message = ex.message
                    ?: "Os dados do produto são inválidos."
            )
        }
    }

    suspend fun update(
        id: Uuid,
        name: String,
        description: String?,
        color: String,
        size: String,
        price: BigDecimal,
        photoPath: String
    ): Result<Product> {
        return try {
            val product = productRepository.findById(id)
                ?: return Result.failure(ProductErrors.NotFound)

            product.updateDetails(
                name = name,
                description = description,
                color = color,
                size = size,
                price = price,
                photoPath = photoPath
            )

            productRepository.save(product)

            Result.success(product)
        } catch (ex: DomainException) {
            Result.failure(
                code = "PRODUCT_INVALID_INPUT",
                message = ex.message ?: "Os dados do produto são inválidos."
            )
        }
    }

    suspend fun updatePhoto(id: Uuid, newPhotoPath: String): Result<Product> {
        return try {
            val product = productRepository.findById(id)
                ?: return Result.failure(ProductErrors.NotFound)

            product.updatePhoto(newPhotoPath)
            productRepository.save(product)

            Result.success(product)
        } catch (ex: DomainException) {
            Result.failure(
                code = "PRODUCT_INVALID_PHOTO",
                message = ex.message ?: "A foto do produto é inválida."
            )
        }
    }

    suspend fun deactivate(id: Uuid): Result<Product> {
        return try {
            val product = productRepository.findById(id)
                ?: return Result.failure(ProductErrors.NotFound)

            product.deactivate()
            productRepository.save(product)

            Result.success(product)
        } catch (ex: DomainException) {
            Result.failure(ProductErrors.AlreadyDeactivated)
        }
    }

    suspend fun activate(id: Uuid): Result<Product> {
        return try {
            val product = productRepository.findById(id)
                ?: return Result.failure(ProductErrors.NotFound)

            product.activate()
            productRepository.save(product)

            Result.success(product)
        } catch (ex: DomainException) {
            Result.failure(ProductErrors.AlreadyActive)
        }
    }

    suspend fun deleteInactive(
        id: Uuid,
        imageStorage: ProductImageStorage,
        deletePhoto: Boolean = true
    ): Result<Unit> {
        val product = productRepository.findById(id)
            ?: return Result.failure(ProductErrors.NotFound)

        if (product.isActive) {
            return Result.failure(
                ResultError("PRODUCT_IS_ACTIVE", "Apenas produtos desativados podem ser excluídos permanentemente.")
            )
        }

        val deleted = productRepository.deleteInactiveById(id)
        if (deleted) {
            if (deletePhoto) {
                imageStorage.deletePhoto(product.photoPath)
            }
            return Result.success(Unit)
        }

        return Result.failure(ProductErrors.NotFound)
    }

    suspend fun deleteInactiveBatch(
        ids: List<Uuid>,
        imageStorage: ProductImageStorage,
        deletePhoto: Boolean = true
    ): Result<Int> {
        if (ids.isEmpty()) return Result.success(0)

        val productsToDelete = ids.mapNotNull { productRepository.findById(it) }.filter { !it.isActive }
        val validIds = productsToDelete.map { it.id }

        val deletedCount = productRepository.deleteInactiveByIds(validIds)
        if (deletePhoto) {
            productsToDelete.forEach { product ->
                imageStorage.deletePhoto(product.photoPath)
            }
        }

        return Result.success(deletedCount)
    }

    suspend fun findById(id: Uuid): Result<Product> {
        val product = productRepository.findById(id)
            ?: return Result.failure(ProductErrors.NotFound)

        return Result.success(product)
    }

    suspend fun findAllActive(): Result<List<Product>> {
        val listActiveProducts = productRepository.findAllActive()

        return Result.success(listActiveProducts)
    }

    suspend fun findAllInactive(): Result<List<Product>> {
        val listInactiveProducts = productRepository.findAllInactive()
        return Result.success(listInactiveProducts)
    }
}
