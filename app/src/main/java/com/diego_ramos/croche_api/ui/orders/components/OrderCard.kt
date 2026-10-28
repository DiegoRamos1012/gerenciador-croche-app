package com.diego_ramos.croche_api.ui.orders.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diego_ramos.croche_api.common.util.CurrencyUtils
import com.diego_ramos.croche_api.common.util.PhoneVisualTransformation
import com.diego_ramos.croche_api.modules.order.domain.Order
import com.diego_ramos.croche_api.modules.order.domain.OrderStatus
import com.diego_ramos.croche_api.modules.order.domain.ShippingType
import com.diego_ramos.croche_api.modules.orderItem.domain.OrderItem
import java.math.BigDecimal
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Composable
fun OrderCard(
    order: Order,
    onEditOrder: () -> Unit,
    onRegisterPayment: () -> Unit,
    onToggleDelivery: () -> Unit,
    onCancelOrder: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val formattedDate = remember(order.createdAt) {
        try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            formatter.format(order.createdAt.atZone(ZoneId.systemDefault()))
        } catch (_: Exception) {
            ""
        }
    }

    val isCancelled = order.status == OrderStatus.CANCELADO

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = order.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OrderStatusBadge(status = order.status)
                    }

                    if (!order.customerPhone.isNullOrBlank()) {
                        Text(
                            text = "Tel: ${PhoneVisualTransformation.format(order.customerPhone)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (!order.customerAddress.isNullOrBlank()) {
                        Text(
                            text = "Endereço: ${order.customerAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShippingBadge(shippingType = order.shippingType)
                        Spacer(modifier = Modifier.width(6.dp))
                        DeliveryBadge(isDelivered = order.isDelivered)
                    }

                    if (formattedDate.isNotBlank()) {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.format(order.total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCancelled) Color.Gray else MaterialTheme.colorScheme.primary
                    )

                    if (order.paidAmount > BigDecimal.ZERO) {
                        Text(
                            text = "Pago: ${CurrencyUtils.format(order.paidAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    if (order.remainingBalance > BigDecimal.ZERO && !isCancelled) {
                        Text(
                            text = "Falta: ${CurrencyUtils.format(order.remainingBalance)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (order.isFullyPaid && !isCancelled) {
                        Text(
                            text = "Totalmente Pago",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Row {
                        if (!isCancelled) {
                            IconButton(onClick = onEditOrder) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar Pedido")
                            }
                        }
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expandir"
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Itens do Pedido:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    order.items.forEach { item ->
                        OrderItemRow(item = item)
                    }

                    if (!isCancelled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (order.remainingBalance > BigDecimal.ZERO) {
                                TextButton(onClick = onRegisterPayment) {
                                    Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Novo Pagamento")
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            Row {
                                TextButton(onClick = onToggleDelivery) {
                                    Text(if (order.isDelivered) "Marcar Pendente" else "Marcar Entregue")
                                }
                                OutlinedButton(onClick = onCancelOrder) {
                                    Text("Cancelar", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun OrderItemRow(item: OrderItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${item.quantity}x ${item.productName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Cor: ${item.productColor} | Tam: ${item.productSize} (${CurrencyUtils.format(item.unitPrice)} un.)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = CurrencyUtils.format(item.subtotal),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun OrderStatusBadge(status: OrderStatus) {
    val isCancelled = status == OrderStatus.CANCELADO
    val backgroundColor = if (isCancelled) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)
    val textColor = if (isCancelled) Color(0xFFB71C1C) else Color(0xFF1B5E20)
    val text = if (isCancelled) "CANCELADO" else "CONCLUÍDO"

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ShippingBadge(shippingType: ShippingType) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocalShipping,
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = shippingType.label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun DeliveryBadge(isDelivered: Boolean) {
    val backgroundColor = if (isDelivered) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
    val textColor = if (isDelivered) Color(0xFF2E7D32) else Color(0xFFE65100)
    val text = if (isDelivered) "Entregue" else "Entrega Pendente"

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isDelivered) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
