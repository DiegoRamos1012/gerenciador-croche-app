package com.diego_ramos.croche_api.modules.product.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diego_ramos.croche_api.modules.product.domain.Product
import java.math.BigDecimal
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "color")
    val color: String,

    @ColumnInfo(name = "size")
    val size: String,

    @ColumnInfo(name = "price")
    val price: String,

    @ColumnInfo(name = "photo_path")
    val photoPath: String,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)

@OptIn(ExperimentalUuidApi::class)
fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        id = id.toString(),
        name = name,
        description = description,
        color = color,
        size = size,
        price = price.toPlainString(),
        photoPath = photoPath,
        isActive = isActive,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}

@OptIn(ExperimentalUuidApi::class)
fun ProductEntity.toDomain(): Product {
    return Product.restore(
        id = Uuid.parse(id),
        name = name,
        description = description,
        color = color,
        size = size,
        price = BigDecimal(price),
        photoPath = photoPath,
        isActive = isActive,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )
}
