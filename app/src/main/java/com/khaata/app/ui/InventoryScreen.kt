package com.khaata.app.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khaata.app.CameraMode
import com.khaata.app.KhaataViewModel
import com.khaata.app.Screen
import com.khaata.app.data.Item
import com.khaata.app.ui.theme.CardBg
import com.khaata.app.ui.theme.OnlineGreen
import com.khaata.app.ui.theme.Saffron
import com.khaata.app.ui.theme.TextSecondary
import com.khaata.app.ui.theme.WarnRed

@Composable
fun InventoryScreen(viewModel: KhaataViewModel) {
    val items by viewModel.allItems.collectAsState()
    val editing by viewModel.editingItem.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { viewModel.screen.value = Screen.MAIN }) {
                    Text("←", fontSize = 26.sp, color = TextSecondary)
                }
                Text("📦 INVENTORY", style = MaterialTheme.typography.titleLarge, color = Saffron)
                Spacer(Modifier.weight(1f))
                Text("${items.size} items", color = TextSecondary, fontSize = 18.sp)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Button(
                    onClick = { viewModel.startAddItem() },
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("➕ ADD ITEM", fontSize = 18.sp) }
                Button(
                    onClick = { viewModel.openCamera(CameraMode.STOCK) },
                    colors = ButtonDefaults.buttonColors(containerColor = CardBg),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("📷 SCAN TO STOCK", fontSize = 18.sp) }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(items, key = { it.id }) { item ->
                    Surface(
                        color = CardBg,
                        shape = RoundedCornerShape(12.dp),
                        onClick = { viewModel.startEditItem(item) }
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(item.nameEnglish, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                                Text(item.nameHindi, fontSize = 16.sp, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${formatQty(item.unitPrice)}/${item.unit}", fontSize = 18.sp, color = Saffron)
                                Text(
                                    "stock: ${formatQty(item.stockQty)}",
                                    fontSize = 16.sp,
                                    color = if (item.stockQty < 3) WarnRed else OnlineGreen
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text("✏️", fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }

    editing?.let { item ->
        ItemEditDialog(
            item = item,
            isNew = viewModel.allItems.value.none { it.id == item.id },
            onSave = { viewModel.saveItem(it) },
            onDismiss = { viewModel.dismissItemDialog() }
        )
    }
}

@Composable
fun ItemEditDialog(item: Item, isNew: Boolean, onSave: (Item) -> Unit, onDismiss: () -> Unit) {
    var nameEn by remember { mutableStateOf(item.nameEnglish) }
    var nameHi by remember { mutableStateOf(item.nameHindi) }
    var price by remember { mutableStateOf(if (item.unitPrice == 0.0) "" else formatQty(item.unitPrice)) }
    var stock by remember { mutableStateOf(formatQty(item.stockQty)) }
    var unit by remember { mutableStateOf(item.unit) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = {
            Text(
                if (isNew) "Naya item" else "Item badlo",
                fontSize = 22.sp, color = Color.White
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = nameEn, onValueChange = { nameEn = it },
                    label = { Text("Name (English)") }, singleLine = true)
                OutlinedTextField(value = nameHi, onValueChange = { nameHi = it },
                    label = { Text("Naam (Hindi, optional)") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = price, onValueChange = { price = it },
                        label = { Text("₹ Price") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = stock, onValueChange = { stock = it },
                        label = { Text("Stock") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("kg", "pkt", "piece", "litre", "dozen").forEach { u ->
                        Surface(
                            color = if (unit == u) Saffron else Color.DarkGray,
                            shape = RoundedCornerShape(10.dp),
                            onClick = { unit = u }
                        ) {
                            Text(u, Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 16.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameEn.isNotBlank()) {
                        onSave(
                            item.copy(
                                nameEnglish = nameEn.trim(),
                                nameHindi = nameHi.trim().ifBlank { nameEn.trim() },
                                nameKannada = item.nameKannada.ifBlank { nameEn.trim() },
                                unitPrice = price.toDoubleOrNull() ?: 0.0,
                                stockQty = stock.toDoubleOrNull() ?: 0.0,
                                unit = unit
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Saffron)
            ) { Text("✅ SAVE", fontSize = 18.sp) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary, fontSize = 18.sp) }
        }
    )
}
