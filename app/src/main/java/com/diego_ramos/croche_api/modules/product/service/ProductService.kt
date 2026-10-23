package com.diego_ramos.croche_api.modules.product.service

import com.diego_ramos.croche_api.common.exception.DomainException
import com.diego_ramos.croche_api.common.result.Result
import com.diego_ramos.croche_api.modules.product.domain.Product
import com.diego_ramos.croche_api.modules.product.repository.ProductRepository
import java.math.BigDecimal

class ProductService(
    private val productRepository: ProductRepository
) {
    suspend fun create(
        name: String,
        description: String,
        color: String,
        size: String,
        price: BigDecimal
    ): Result<Product> {
        return try {
            val product = Product.create(
                name = name,
                description = description,
                color = color,
                size = size,
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
}