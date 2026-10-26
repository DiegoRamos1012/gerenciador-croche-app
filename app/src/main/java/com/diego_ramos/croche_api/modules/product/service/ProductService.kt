package com.diego_ramos.croche_api.modules.product.service

import com.diego_ramos.croche_api.common.exception.DomainException
import com.diego_ramos.croche_api.common.result.Result
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
        price: BigDecimal
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
                price = price
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
