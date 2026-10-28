package com.diego_ramos.croche_api.ui.orders.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.diego_ramos.croche_api.common.util.CurrencyUtils
import com.diego_ramos.croche_api.common.util.PhoneVisualTransformation
import com.diego_ramos.croche_api.modules.order.domain.Order
import com.diego_ramos.croche_api.modules.order.domain.ShippingType
import com.diego_ramos.croche_api.modules.order.service.CreateOrderItemInput
import com.diego_ramos.croche_api.modules.order.service.OrderService
import com.diego_ramos.croche_api.modules.product.data.local.image.ProductImageStorage
import com.diego_ramos.croche_api.modules.product.domain.Product
import com.diego_ramos.croche_api.modules.product.service.ProductService
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalLayoutApi::class)
@Composable
fun OrderFormDialog(
    orderToEdit: Order? = null,
    orderService: OrderService,
    productService: ProductService,
    imageStorage: ProductImageStorage,
    onDismiss: () -> Unit,
    onOrderSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var customerName by remember { mutableStateOf(orderToEdit?.customerName ?: "") }
    var customerPhoneRaw by remember { mutableStateOf(orderToEdit?.customerPhone?.filter { it.isDigit() } ?: "") }
    var customerAddress by remember { mutableStateOf(orderToEdit?.customerAddress ?: "") }

    var selectedShippingType by remember { mutableStateOf(orderToEdit?.shippingType ?: ShippingType.ENTREGA_PESSOAL) }

    var hasDownPayment by remember { mutableStateOf(orderToEdit?.paidAmount?.let { it > BigDecimal.ZERO } ?: false) }
    var downPaymentCents by remember { mutableLongStateOf(CurrencyUtils.bigDecimalToCents(orderToEdit?.paidAmount)) }

    var isDelivered by remember { mutableStateOf(orderToEdit?.isDelivered ?: false) }

    var activeProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    val quantities = remember { mutableStateMapOf<String, Int>() }
    var isLoadingProducts by remember { mutableStateOf(false) }

    var productSearchQuery by remember { mutableStateOf("") }
    var visibleProductCount by remember { mutableIntStateOf(5) }

    LaunchedEffect(Unit) {
        isLoadingProducts = true
        val result = productService.findAllActive()
        if (result.isSuccess) {
            activeProducts = result.value
            if (orderToEdit != null) {
                orderToEdit.items.forEach { item ->
                    quantities[item.productId.toString()] = item.quantity
                }
            }
        }
        isLoadingProducts = false
    }

    LaunchedEffect(productSearchQuery) {
        visibleProductCount = 5
    }

    val selectedItemsCount = quantities.values.sum()
    val totalAmount = remember(quantities.toMap(), activeProducts) {
        activeProducts.fold(BigDecimal.ZERO) { acc, product ->
            val qty = quantities[product.id.toString()] ?: 0
            if (qty > 0) {
                acc.add(product.price.multiply(BigDecimal(qty)))
            } else acc
        }
    }

    val downPaymentAmount = remember(hasDownPayment, downPaymentCents) {
        if (hasDownPayment) CurrencyUtils.centsToBigDecimal(downPaymentCents) else BigDecimal.ZERO
    }

    val remainingBalance = remember(totalAmount, downPaymentAmount) {
        val rem = totalAmount.subtract(downPaymentAmount)
        if (rem < BigDecimal.ZERO) BigDecimal.ZERO else rem
    }

    val filteredProducts = remember(activeProducts, productSearchQuery) {
        if (productSearchQuery.isBlank()) {
            activeProducts
        } else {
            activeProducts.filter { product ->
                product.name.contains(productSearchQuery, ignoreCase = true) ||
                    product.color.contains(productSearchQuery, ignoreCase = true) ||
                    product.size.contains(productSearchQuery, ignoreCase = true)
            }
        }
    }

    val paginatedProducts = remember(filteredProducts, visibleProductCount) {
        filteredProducts.take(visibleProductCount)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (orderToEdit == null) "Cadastrar Novo Pedido" else "Editar Pedido",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Nome do Cliente *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = customerPhoneRaw,
                    onValueChange = { input ->
                        customerPhoneRaw = input.filter { it.isDigit() }.take(11)
                    },
                    label = { Text("Telefone / WhatsApp (opcional)") },
                    placeholder = { Text("(11) 99999-9999") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    visualTransformation = PhoneVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = customerAddress,
                    onValueChange = { customerAddress = it },
                    label = { Text("Endereço do Cliente (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Tipo de Frete / Envio *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShippingType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedShippingType == type,
                            onClick = { selectedShippingType = type },
                            label = { Text(type.label) }
                        )
                    }
                }

                HorizontalDivider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { hasDownPayment = !hasDownPayment }
                ) {
                    Checkbox(
                        checked = hasDownPayment,
                        onCheckedChange = { hasDownPayment = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Houve pagamento de sinal / entrada?")
                }

                if (hasDownPayment) {
                    OutlinedTextField(
                        value = CurrencyUtils.formatCentsWithoutSymbol(downPaymentCents),
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(9)
                            downPaymentCents = digits.toLongOrNull() ?: 0L
                        },
                        label = { Text("Valor Pago / Sinal *") },
                        prefix = { Text("R$ ") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDelivered = !isDelivered }
                ) {
                    Checkbox(
                        checked = isDelivered,
                        onCheckedChange = { isDelivered = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Produto / Pedido já foi entregue?")
                }

                HorizontalDivider()

                Text(
                    text = "Produtos Incluídos no Pedido *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = productSearchQuery,
                    onValueChange = { productSearchQuery = it },
                    placeholder = { Text("Buscar produto...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    trailingIcon = {
                        if (productSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { productSearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isLoadingProducts) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (filteredProducts.isEmpty()) {
                    Text(
                        if (productSearchQuery.isNotBlank()) "Nenhum produto encontrado para \"$productSearchQuery\"."
                        else "Nenhum produto ativo cadastrado no catálogo."
                    )
                } else {
                    paginatedProducts.forEach { product ->
                        val qty = quantities[product.id.toString()] ?: 0
                        ProductOrderSelectRow(
                            product = product,
                            imageStorage = imageStorage,
                            quantity = qty,
                            onQuantityChange = { newQty ->
                                if (newQty <= 0) {
                                    quantities.remove(product.id.toString())
                                } else {
                                    quantities[product.id.toString()] = newQty
                                }
                            }
                        )
                    }

                    if (visibleProductCount < filteredProducts.size) {
                        TextButton(
                            onClick = { visibleProductCount += 5 },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Carregar Mais Produtos (${paginatedProducts.size} de ${filteredProducts.size})")
                        }
                    }
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text("Itens selecionados: $selectedItemsCount", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Total: ${CurrencyUtils.format(totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (hasDownPayment) {
                        Text(
                            "Total Pago: ${CurrencyUtils.format(downPaymentAmount)}",
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            "Saldo Restante: ${CurrencyUtils.format(remainingBalance)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customerName.isBlank()) {
                        Toast.makeText(context, "Informe o nome do cliente.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedItemsCount <= 0) {
                        Toast.makeText(context, "Selecione pelo menos 1 produto.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    scope.launch {
                        val itemsInput = activeProducts.mapNotNull { product ->
                            val qty = quantities[product.id.toString()] ?: 0
                            if (qty > 0) {
                                CreateOrderItemInput(
                                    productId = product.id,
                                    quantity = qty
                                )
                            } else null
                        }

                        val formattedPhone = PhoneVisualTransformation.format(customerPhoneRaw)

                        val result = if (orderToEdit == null) {
                            orderService.createOrder(
                                customerName = customerName,
                                customerPhone = formattedPhone.ifBlank { null },
                                customerAddress = customerAddress.ifBlank { null },
                                shippingType = selectedShippingType,
                                downPayment = downPaymentAmount,
                                isDelivered = isDelivered,
                                itemsInput = itemsInput
                            )
                        } else {
                            orderService.updateOrder(
                                orderId = orderToEdit.id,
                                customerName = customerName,
                                customerPhone = formattedPhone.ifBlank { null },
                                customerAddress = customerAddress.ifBlank { null },
                                shippingType = selectedShippingType,
                                paidAmount = downPaymentAmount,
                                isDelivered = isDelivered,
                                itemsInput = itemsInput
                            )
                        }

                        if (result.isSuccess) {
                            Toast.makeText(
                                context,
                                if (orderToEdit == null) "Pedido cadastrado!" else "Pedido atualizado!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onOrderSaved()
                        } else {
                            Toast.makeText(context, result.error.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            ) {
                Text("Salvar Pedido")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ProductOrderSelectRow(
    product: Product,
    imageStorage: ProductImageStorage,
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            val imageFile = remember(product.photoPath) {
                imageStorage.getFileFromRelativePath(product.photoPath)
            }
            AsyncImage(
                model = imageFile,
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("${product.color} | ${CurrencyUtils.format(product.price)}", style = MaterialTheme.typography.bodySmall)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onQuantityChange(quantity - 1) },
                enabled = quantity > 0
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Diminuir")
            }
            Text(
                text = "$quantity",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
            IconButton(onClick = { onQuantityChange(quantity + 1) }) {
                Icon(Icons.Default.Add, contentDescription = "Aumentar")
            }
        }
    }
}
