package com.diego_ramos.croche_api.ui.orders

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diego_ramos.croche_api.common.util.CurrencyUtils
import com.diego_ramos.croche_api.modules.order.domain.Order
import com.diego_ramos.croche_api.modules.order.service.OrderService
import com.diego_ramos.croche_api.modules.product.data.local.image.ProductImageStorage
import com.diego_ramos.croche_api.modules.product.service.ProductService
import com.diego_ramos.croche_api.ui.orders.components.OrderCard
import com.diego_ramos.croche_api.ui.orders.dialogs.OrderFormDialog
import com.diego_ramos.croche_api.ui.orders.dialogs.RegisterPaymentDialog
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Composable
fun OrdersScreen(
    orderService: OrderService,
    productService: ProductService,
    imageStorage: ProductImageStorage
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var visibleOrderCount by remember { mutableIntStateOf(10) }

    var orderToForm by remember { mutableStateOf<Order?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }

    var orderToRegisterPayment by remember { mutableStateOf<Order?>(null) }

    fun loadOrders() {
        scope.launch {
            isLoading = true
            val result = orderService.findAll()
            if (result.isSuccess) {
                orders = result.value
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadOrders()
    }

    LaunchedEffect(searchQuery) {
        visibleOrderCount = 10
    }

    val filteredOrders = remember(orders, searchQuery) {
        if (searchQuery.isBlank()) {
            orders
        } else {
            orders.filter { order ->
                order.customerName.contains(searchQuery, ignoreCase = true) ||
                    (order.customerPhone?.contains(searchQuery, ignoreCase = true) == true) ||
                    (order.customerAddress?.contains(searchQuery, ignoreCase = true) == true) ||
                    order.items.any { item -> item.productName.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    val paginatedOrders = remember(filteredOrders, visibleOrderCount) {
        filteredOrders.take(visibleOrderCount)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedidos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = {
                    orderToForm = null
                    showFormDialog = true
                }) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cadastrar Pedido")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                placeholder = { Text("Pesquisar pedido por cliente, telefone, endereço...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (searchQuery.isNotBlank()) "Nenhum pedido encontrado para \"$searchQuery\"."
                        else "Nenhum pedido cadastrado até o momento."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(paginatedOrders, key = { it.id.toString() }) { order ->
                        OrderCard(
                            order = order,
                            onEditOrder = {
                                orderToForm = order
                                showFormDialog = true
                            },
                            onRegisterPayment = {
                                orderToRegisterPayment = order
                            },
                            onToggleDelivery = {
                                scope.launch {
                                    val result = orderService.toggleDeliveryStatus(order.id)
                                    if (result.isSuccess) {
                                        loadOrders()
                                    }
                                }
                            },
                            onCancelOrder = {
                                scope.launch {
                                    val result = orderService.cancelOrder(order.id)
                                    if (result.isSuccess) {
                                        Toast.makeText(context, "Pedido cancelado.", Toast.LENGTH_SHORT).show()
                                        loadOrders()
                                    } else {
                                        Toast.makeText(context, result.error.message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        )
                    }

                    if (visibleOrderCount < filteredOrders.size) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                OutlinedButton(onClick = { visibleOrderCount += 10 }) {
                                    Text("Carregar Mais Pedidos (${paginatedOrders.size} de ${filteredOrders.size})")
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                orderToForm = null
                showFormDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Cadastrar Pedido")
        }
    }

    if (showFormDialog) {
        OrderFormDialog(
            orderToEdit = orderToForm,
            orderService = orderService,
            productService = productService,
            imageStorage = imageStorage,
            onDismiss = {
                showFormDialog = false
                orderToForm = null
            },
            onOrderSaved = {
                showFormDialog = false
                orderToForm = null
                loadOrders()
            }
        )
    }

    if (orderToRegisterPayment != null) {
        RegisterPaymentDialog(
            order = orderToRegisterPayment!!,
            onDismiss = { orderToRegisterPayment = null },
            onConfirmPayment = { paymentAmount ->
                scope.launch {
                    val result = orderService.addPayment(orderToRegisterPayment!!.id, paymentAmount)
                    if (result.isSuccess) {
                        Toast.makeText(
                            context,
                            "Pagamento de ${CurrencyUtils.format(paymentAmount)} registrado!",
                            Toast.LENGTH_SHORT
                        ).show()
                        orderToRegisterPayment = null
                        loadOrders()
                    } else {
                        Toast.makeText(context, result.error.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}
