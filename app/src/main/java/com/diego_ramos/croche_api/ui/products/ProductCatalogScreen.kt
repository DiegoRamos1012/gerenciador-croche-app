package com.diego_ramos.croche_api.ui.products

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.diego_ramos.croche_api.common.util.CurrencyUtils
import com.diego_ramos.croche_api.modules.order.domain.Order
import com.diego_ramos.croche_api.modules.order.service.OrderService
import com.diego_ramos.croche_api.modules.product.data.local.image.ProductImageStorage
import com.diego_ramos.croche_api.modules.product.domain.Product
import com.diego_ramos.croche_api.modules.product.service.ProductService
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ProductCatalogScreen(
    productService: ProductService,
    orderService: OrderService,
    imageStorage: ProductImageStorage
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableIntStateOf(0) }
    var activeProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var inactiveProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var visibleItemCount by remember { mutableIntStateOf(10) }

    var selectionMode by remember { mutableStateOf(false) }
    val selectedProductIds = remember { mutableStateListOf<String>() }

    var productToForm by remember { mutableStateOf<Product?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }
    var productToFocus by remember { mutableStateOf<Product?>(null) }

    var singleProductToDelete by remember { mutableStateOf<Product?>(null) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    var productPendingDeactivation by remember { mutableStateOf<Product?>(null) }
    var pendingOrdersWarningList by remember { mutableStateOf<List<Order>>(emptyList()) }

    fun loadProducts() {
        scope.launch {
            isLoading = true
            val activeResult = productService.findAllActive()
            if (activeResult.isSuccess) activeProducts = activeResult.value

            val inactiveResult = productService.findAllInactive()
            if (inactiveResult.isSuccess) inactiveProducts = inactiveResult.value

            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadProducts()
    }

    LaunchedEffect(activeTab, searchQuery) {
        visibleItemCount = 10
        if (activeTab == 0) {
            selectionMode = false
            selectedProductIds.clear()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Pesquisar produto por nome, cor, tamanho...") },
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

            if (selectionMode && activeTab == 1) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${selectedProductIds.size} selecionado(s)",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = {
                                    if (selectedProductIds.size == inactiveProducts.size) {
                                        selectedProductIds.clear()
                                    } else {
                                        selectedProductIds.clear()
                                        selectedProductIds.addAll(inactiveProducts.map { it.id.toString() })
                                    }
                                }
                            ) {
                                Text(
                                    if (selectedProductIds.size == inactiveProducts.size) "Desmarcar Todos"
                                    else "Selecionar Todos"
                                )
                            }

                            if (selectedProductIds.isNotEmpty()) {
                                IconButton(onClick = { showBatchDeleteDialog = true }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Excluir Selecionados",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            TextButton(
                                onClick = {
                                    selectionMode = false
                                    selectedProductIds.clear()
                                }
                            ) {
                                Text("Cancelar")
                            }
                        }
                    }
                }
            } else {
                TabRow(selectedTabIndex = activeTab) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Ativos (${activeProducts.size})") }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Inativos (${inactiveProducts.size})") }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val sourceList = if (activeTab == 0) activeProducts else inactiveProducts
                val filteredList = remember(sourceList, searchQuery) {
                    if (searchQuery.isBlank()) {
                        sourceList
                    } else {
                        sourceList.filter { product ->
                            product.name.contains(searchQuery, ignoreCase = true) ||
                                product.color.contains(searchQuery, ignoreCase = true) ||
                                product.size.contains(searchQuery, ignoreCase = true) ||
                                (product.description?.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) == true)
                        }
                    }
                }

                val paginatedList = filteredList.take(visibleItemCount)

                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (searchQuery.isNotBlank()) "Nenhum produto encontrado para \"$searchQuery\"."
                            else if (activeTab == 0) "Nenhum produto ativo cadastrado."
                            else "Nenhum produto inativo."
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(paginatedList, key = { it.id.toString() }) { product ->
                            val isSelected = selectedProductIds.contains(product.id.toString())

                            ProductCard(
                                product = product,
                                imageStorage = imageStorage,
                                selectionMode = selectionMode && activeTab == 1,
                                isSelected = isSelected,
                                onSelectToggle = {
                                    if (isSelected) {
                                        selectedProductIds.remove(product.id.toString())
                                        if (selectedProductIds.isEmpty()) selectionMode = false
                                    } else {
                                        selectedProductIds.add(product.id.toString())
                                    }
                                },
                                onLongClick = {
                                    if (activeTab == 1) {
                                        selectionMode = true
                                        if (!selectedProductIds.contains(product.id.toString())) {
                                            selectedProductIds.add(product.id.toString())
                                        }
                                    }
                                },
                                onEditProduct = {
                                    productToForm = product
                                    showFormDialog = true
                                },
                                onViewFocus = {
                                    productToFocus = product
                                },
                                onToggleStatus = {
                                    scope.launch {
                                        if (product.isActive) {
                                            val checkPending = orderService.findPendingDeliveryOrdersForProduct(product.id)
                                            if (checkPending.isSuccess && checkPending.value.isNotEmpty()) {
                                                productPendingDeactivation = product
                                                pendingOrdersWarningList = checkPending.value
                                            } else {
                                                productService.deactivate(product.id)
                                                loadProducts()
                                            }
                                        } else {
                                            productService.activate(product.id)
                                            loadProducts()
                                        }
                                    }
                                },
                                onDeleteInactive = {
                                    singleProductToDelete = product
                                }
                            )
                        }

                        if (visibleItemCount < filteredList.size) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    OutlinedButton(
                                        onClick = { visibleItemCount += 10 }
                                    ) {
                                        Text("Carregar Mais (${paginatedList.size} de ${filteredList.size})")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!selectionMode) {
            FloatingActionButton(
                onClick = {
                    productToForm = null
                    showFormDialog = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Produto")
            }
        }
    }

    if (showFormDialog) {
        ProductFormDialog(
            productToEdit = productToForm,
            imageStorage = imageStorage,
            onDismiss = {
                showFormDialog = false
                productToForm = null
            },
            onConfirm = { name, desc, color, size, price, photoPath ->
                scope.launch {
                    val editing = productToForm
                    val result = if (editing == null) {
                        productService.create(
                            name = name,
                            description = desc,
                            color = color,
                            size = size,
                            price = price,
                            photoPath = photoPath
                        )
                    } else {
                        productService.update(
                            id = editing.id,
                            name = name,
                            description = desc,
                            color = color,
                            size = size,
                            price = price,
                            photoPath = photoPath
                        )
                    }

                    if (result.isSuccess) {
                        Toast.makeText(
                            context,
                            if (editing == null) "Produto cadastrado!" else "Produto atualizado!",
                            Toast.LENGTH_SHORT
                        ).show()
                        showFormDialog = false
                        productToForm = null
                        loadProducts()
                    } else {
                        Toast.makeText(context, result.error.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    if (productToFocus != null) {
        ProductFocusCardDialog(
            product = productToFocus!!,
            imageStorage = imageStorage,
            onDismiss = { productToFocus = null }
        )
    }

    if (productPendingDeactivation != null) {
        val prod = productPendingDeactivation!!
        AlertDialog(
            onDismissRequest = {
                productPendingDeactivation = null
                pendingOrdersWarningList = emptyList()
            },
            title = { Text("Aviso — Pedidos com Entrega Pendente", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("O produto \"${prod.name}\" está associado a ${pendingOrdersWarningList.size} pedido(s) com entrega pendente:")
                    pendingOrdersWarningList.take(3).forEach { ord ->
                        Text("• Cliente: ${ord.customerName} (${CurrencyUtils.format(ord.total)})", fontWeight = FontWeight.SemiBold)
                    }
                    if (pendingOrdersWarningList.size > 3) {
                        Text("... e mais ${pendingOrdersWarningList.size - 3} pedido(s).")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Deseja realmente desativar este produto do catálogo?")
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        scope.launch {
                            productService.deactivate(prod.id)
                            productPendingDeactivation = null
                            pendingOrdersWarningList = emptyList()
                            loadProducts()
                        }
                    }
                ) {
                    Text("Desativar Mesmo Assim")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        productPendingDeactivation = null
                        pendingOrdersWarningList = emptyList()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (singleProductToDelete != null) {
        val target = singleProductToDelete!!
        var deletePhotoChecked by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { singleProductToDelete = null },
            title = { Text("Excluir Permanentemente") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Deseja excluir permanentemente o produto \"${target.name}\"? Essa ação apagará o registro do banco de dados.")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { deletePhotoChecked = !deletePhotoChecked }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = deletePhotoChecked,
                            onCheckedChange = { deletePhotoChecked = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Excluir também o arquivo de foto do dispositivo",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        scope.launch {
                            val result = productService.deleteInactive(
                                id = target.id,
                                imageStorage = imageStorage,
                                deletePhoto = deletePhotoChecked
                            )
                            if (result.isSuccess) {
                                Toast.makeText(
                                    context,
                                    "Produto excluído definitivamente.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                singleProductToDelete = null
                                loadProducts()
                            } else {
                                Toast.makeText(context, result.error.message, Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { singleProductToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showBatchDeleteDialog) {
        val count = selectedProductIds.size
        var batchDeletePhotoChecked by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            title = { Text("Excluir $count Produto(s)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Deseja excluir permanentemente os $count produtos inativos selecionados?")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { batchDeletePhotoChecked = !batchDeletePhotoChecked }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = batchDeletePhotoChecked,
                            onCheckedChange = { batchDeletePhotoChecked = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Excluir também os arquivos de foto do dispositivo",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        scope.launch {
                            val uuids = selectedProductIds.mapNotNull {
                                try {
                                    Uuid.parse(it)
                                } catch (_: Exception) {
                                    null
                                }
                            }
                            val result = productService.deleteInactiveBatch(
                                ids = uuids,
                                imageStorage = imageStorage,
                                deletePhoto = batchDeletePhotoChecked
                            )
                            if (result.isSuccess) {
                                Toast.makeText(
                                    context,
                                    "${result.value} produto(s) excluído(s).",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showBatchDeleteDialog = false
                                selectionMode = false
                                selectedProductIds.clear()
                                loadProducts()
                            } else {
                                Toast.makeText(context, result.error.message, Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                    }
                ) {
                    Text("Excluir Todos")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBatchDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

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
