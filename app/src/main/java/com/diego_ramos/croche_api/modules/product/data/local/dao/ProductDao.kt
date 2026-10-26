package com.diego_ramos.croche_api.modules.product.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.diego_ramos.croche_api.modules.product.data.local.entity.ProductEntity

@Dao
interface ProductDao {

    @Upsert
    suspend fun upsert(product: ProductEntity)

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ProductEntity?

    @Query("""
        SELECT COUNT(*) > 0 
        FROM products 
        WHERE LOWER(name) = LOWER(:name) 
          AND LOWER(color) = LOWER(:color) 
          AND LOWER(size) = LOWER(:size) 
          AND is_active = 1
    """)
    suspend fun existsByNameAndColorAndSize(
        name: String,
        color: String,
        size: String
    ): Boolean

    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    suspend fun findAllActive(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE is_active = 0 ORDER BY name ASC")
    suspend fun findAllInactive(): List<ProductEntity>
}
