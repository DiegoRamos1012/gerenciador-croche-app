package com.diego_ramos.croche_api.ui.products.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.diego_ramos.croche_api.common.util.CurrencyUtils
import com.diego_ramos.croche_api.modules.product.data.local.image.ProductImageStorage
import com.diego_ramos.croche_api.modules.product.domain.Product
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalFoundationApi::class)
@Composable
fun ProductCard(
    product: Product,
    imageStorage: ProductImageStorage,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectToggle: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onEditProduct: () -> Unit,
    onViewFocus: () -> Unit,
    onToggleStatus: () -> Unit,
    onDeleteInactive: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (selectionMode) onSelectToggle() else onViewFocus()
                },
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectToggle() }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
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

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cor: ${product.color} | Tam: ${product.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = CurrencyUtils.format(product.price),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                if (!product.description.isNullOrBlank()) {
                    Text(
                        text = product.description!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!selectionMode) {
                Column(horizontalAlignment = Alignment.End) {
                    Row {
                        IconButton(onClick = onViewFocus) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = "Ver Detalhes e Foto"
                            )
                        }
                        IconButton(onClick = onEditProduct) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Produto")
                        }
                        if (!product.isActive) {
                            IconButton(onClick = onDeleteInactive) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Excluir Definitivamente",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    TextButton(onClick = onToggleStatus) {
                        Text(if (product.isActive) "Desativar" else "Reativar")
                    }
                }
            }
        }
    }
}
