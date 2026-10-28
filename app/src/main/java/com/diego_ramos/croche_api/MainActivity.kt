package com.diego_ramos.croche_api

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.diego_ramos.croche_api.config.database.AppDatabase
import com.diego_ramos.croche_api.modules.order.data.repository.OrderRepositoryImpl
import com.diego_ramos.croche_api.modules.order.service.OrderService
import com.diego_ramos.croche_api.modules.product.data.local.image.ProductImageStorage
import com.diego_ramos.croche_api.modules.product.data.repository.ProductRepositoryImpl
import com.diego_ramos.croche_api.modules.product.service.ProductService
import com.diego_ramos.croche_api.ui.orders.OrdersScreen
import com.diego_ramos.croche_api.ui.products.ProductCatalogScreen
import com.diego_ramos.croche_api.ui.theme.CrocheapiTheme
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val imageStorage = ProductImageStorage(applicationContext)

        val productRepository = ProductRepositoryImpl(database.productDao())
        val orderRepository = OrderRepositoryImpl(database.orderDao())

        val productService = ProductService(productRepository)
        val orderService = OrderService(orderRepository, productRepository)

        setContent {
            CrocheapiTheme {
                MainAppScreen(
                    productService = productService,
                    orderService = orderService,
                    imageStorage = imageStorage
                )
            }
        }
    }
}

@Composable
fun MainAppScreen(
    productService: ProductService,
    orderService: OrderService,
    imageStorage: ProductImageStorage
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Produtos") },
                    label = { Text("Produtos") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Pedidos") },
                    label = { Text("Pedidos") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ProductCatalogScreen(
                    productService = productService,
                    orderService = orderService,
                    imageStorage = imageStorage
                )
                1 -> OrdersScreen(
                    orderService = orderService,
                    productService = productService,
                    imageStorage = imageStorage
                )
            }
        }
    }
}
