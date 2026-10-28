package com.diego_ramos.croche_api.ui.orders.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.diego_ramos.croche_api.common.util.CurrencyUtils
import com.diego_ramos.croche_api.modules.order.domain.Order
import java.math.BigDecimal

@Composable
fun RegisterPaymentDialog(
    order: Order,
    onDismiss: () -> Unit,
    onConfirmPayment: (BigDecimal) -> Unit
) {
    var paymentCents by remember { mutableLongStateOf(0L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Novo Pagamento", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Cliente: ${order.customerName}")
                Text("Total do Pedido: ${CurrencyUtils.format(order.total)}")
                Text("Valor Já Pago: ${CurrencyUtils.format(order.paidAmount)}", color = Color(0xFF2E7D32))
                Text("Saldo Restante: ${CurrencyUtils.format(order.remainingBalance)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                OutlinedTextField(
                    value = CurrencyUtils.formatCentsWithoutSymbol(paymentCents),
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(9)
                        paymentCents = digits.toLongOrNull() ?: 0L
                    },
                    label = { Text("Valor do Pagamento Atual *") },
                    prefix = { Text("R$ ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val paymentAmount = CurrencyUtils.centsToBigDecimal(paymentCents)
                    if (paymentAmount <= BigDecimal.ZERO) {
                        return@Button
                    }
                    onConfirmPayment(paymentAmount)
                }
            ) {
                Text("Confirmar Pagamento")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
