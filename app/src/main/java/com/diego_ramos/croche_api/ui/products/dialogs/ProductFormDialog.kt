package com.diego_ramos.croche_api.ui.products.dialogs

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.diego_ramos.croche_api.common.util.CurrencyUtils
import com.diego_ramos.croche_api.modules.product.data.local.image.ProductImageStorage
import com.diego_ramos.croche_api.modules.product.domain.Product
import com.diego_ramos.croche_api.ui.products.ImageCropDialog
import java.math.BigDecimal
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ProductFormDialog(
    productToEdit: Product? = null,
    imageStorage: ProductImageStorage,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String, String, BigDecimal, String) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }
    var color by remember { mutableStateOf(productToEdit?.color ?: "") }
    var size by remember { mutableStateOf(productToEdit?.size ?: "") }
    var priceCents by remember {
        mutableLongStateOf(CurrencyUtils.bigDecimalToCents(productToEdit?.price))
    }
    var savedPhotoPath by remember { mutableStateOf<String?>(productToEdit?.photoPath) }

    var bitmapToCrop by remember { mutableStateOf<Bitmap?>(null) }
    var showCropDialog by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            try {
                val decoded = imageStorage.decodeBitmapFromUri(sourceUri)
                if (decoded != null) {
                    bitmapToCrop = decoded
                    showCropDialog = true
                } else {
                    Toast.makeText(
                        context,
                        "Não foi possível carregar a imagem.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (ex: Exception) {
                Toast.makeText(context, "Erro ao abrir imagem: ${ex.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    if (showCropDialog && bitmapToCrop != null) {
        ImageCropDialog(
            bitmap = bitmapToCrop!!,
            onDismiss = {
                showCropDialog = false
                bitmapToCrop = null
            },
            onCropConfirmed = { croppedBitmap ->
                val newPath = imageStorage.saveBitmapPhoto(croppedBitmap)
                savedPhotoPath = newPath
                showCropDialog = false
                bitmapToCrop = null
                Toast.makeText(context, "Foto recortada com sucesso!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (productToEdit == null) "Novo Produto" else "Editar Produto") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            pickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (savedPhotoPath != null) {
                        val imageFile = remember(savedPhotoPath) {
                            imageStorage.getFileFromRelativePath(savedPhotoPath!!)
                        }
                        AsyncImage(
                            model = imageFile,
                            contentDescription = "Foto do Produto",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Selecionar e Enquadrar Foto *",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Produto *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Cor *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Tamanho (ex.: P, M, 30cm) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = CurrencyUtils.formatCentsWithoutSymbol(priceCents),
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(9)
                        priceCents = digits.toLongOrNull() ?: 0L
                    },
                    label = { Text("Preço do Produto *") },
                    prefix = { Text("R$ ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = CurrencyUtils.centsToBigDecimal(priceCents)
                    val photo = savedPhotoPath

                    if (photo == null) {
                        Toast.makeText(context, "Por favor, selecione uma foto para o produto.", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (price <= BigDecimal.ZERO) {
                        Toast.makeText(context, "Informe um preço válido maior que zero.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    onConfirm(
                        name,
                        description.ifBlank { null },
                        color,
                        size,
                        price,
                        photo
                    )
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
