package com.diego_ramos.croche_api.config.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diego_ramos.croche_api.modules.order.data.local.dao.OrderDao
import com.diego_ramos.croche_api.modules.order.data.local.entity.OrderEntity
import com.diego_ramos.croche_api.modules.orderItem.data.local.entity.OrderItemEntity
import com.diego_ramos.croche_api.modules.product.data.local.dao.ProductDao
import com.diego_ramos.croche_api.modules.product.data.local.entity.ProductEntity

@Database(
    entities = [
        ProductEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN photo_path TEXT NOT NULL DEFAULT ''")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `orders` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `customer_name` TEXT NOT NULL,
                        `customer_phone` TEXT,
                        `status` TEXT NOT NULL,
                        `total` TEXT NOT NULL,
                        `created_at` TEXT NOT NULL,
                        `updated_at` TEXT NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `order_items` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `order_id` TEXT NOT NULL,
                        `product_id` TEXT NOT NULL,
                        `product_name` TEXT NOT NULL,
                        `product_color` TEXT NOT NULL,
                        `product_size` TEXT NOT NULL,
                        `unit_price` TEXT NOT NULL,
                        `quantity` INTEGER NOT NULL,
                        `subtotal` TEXT NOT NULL,
                        `created_at` TEXT NOT NULL,
                        `updated_at` TEXT NOT NULL,
                        FOREIGN KEY(`order_id`) REFERENCES `orders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())

                db.execSQL("CREATE INDEX IF NOT EXISTS `index_order_items_order_id` ON `order_items` (`order_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_order_items_product_id` ON `order_items` (`product_id`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE orders ADD COLUMN customer_address TEXT")
                db.execSQL("ALTER TABLE orders ADD COLUMN shipping_type TEXT NOT NULL DEFAULT 'ENTREGA_PESSOAL'")
                db.execSQL("ALTER TABLE orders ADD COLUMN down_payment TEXT")
                db.execSQL("ALTER TABLE orders ADD COLUMN is_delivered INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE orders ADD COLUMN paid_amount TEXT")
                db.execSQL("UPDATE orders SET paid_amount = down_payment WHERE down_payment IS NOT NULL")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "croche_database.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
