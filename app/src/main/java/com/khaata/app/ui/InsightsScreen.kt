package com.khaata.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khaata.app.KhaataViewModel
import com.khaata.app.Screen
import com.khaata.app.ui.theme.CardBg
import com.khaata.app.ui.theme.OnlineGreen
import com.khaata.app.ui.theme.Saffron
import com.khaata.app.ui.theme.TextSecondary
import com.khaata.app.ui.theme.WarnRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InsightsScreen(viewModel: KhaataViewModel) {
    val soldToday by viewModel.soldToday.collectAsState()
    val restock by viewModel.lowStockItems.collectAsState()
    val history by viewModel.billHistory.collectAsState()
    val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { viewModel.screen.value = Screen.MAIN }) {
                        Text("←", fontSize = 26.sp, color = TextSecondary)
                    }
                    Text("📊 AAJ KA HISAAB", style = MaterialTheme.typography.titleLarge, color = Saffron)
                }
            }

            // ---- sold today ----
            item {
                val revenue = soldToday.sumOf { it.revenue }
                Surface(color = CardBg, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Aaj ki kamai", fontSize = 18.sp, color = TextSecondary)
                            Text("₹${formatQty(revenue)}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = OnlineGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${soldToday.size} items", fontSize = 18.sp, color = TextSecondary)
                            Text("${history.count { it.closedAt != null }} bills", fontSize = 18.sp, color = TextSecondary)
                        }
                    }
                }
            }

            item { Text("🛒 AAJ BIKA (sold today)", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp)) }
            if (soldToday.isEmpty()) {
                item { Text("Abhi tak kuch nahi bika.", color = TextSecondary, fontSize = 18.sp) }
            }
            items(soldToday, key = { it.itemId }) { sold ->
                Surface(color = CardBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Text(sold.itemName, fontSize = 19.sp, modifier = Modifier.weight(1f))
                        Text("${formatQty(sold.totalQty)}${sold.unit}", fontSize = 19.sp, color = TextSecondary)
                        Spacer(Modifier.weight(0.2f))
                        Text("₹${formatQty(sold.revenue)}", fontSize = 19.sp, color = Saffron)
                    }
                }
            }

            // ---- restock ----
            item { Text("⚠️ BHARNA HAI (restock, stock < 3)", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp)) }
            if (restock.isEmpty()) {
                item { Text("Sab stock theek hai ✅", color = OnlineGreen, fontSize = 18.sp) }
            }
            items(restock, key = { "r${it.id}" }) { item ->
                Surface(color = WarnRed.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Text(item.nameEnglish, fontSize = 19.sp, color = WarnRed, modifier = Modifier.weight(1f))
                        Text("bacha: ${formatQty(item.stockQty)} ${item.unit}", fontSize = 18.sp, color = WarnRed)
                    }
                }
            }

            // ---- bill history ----
            item { Text("🧾 CUSTOMER BILLS (aaj ke)", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp)) }
            if (history.isEmpty()) {
                item { Text("Koi bill nahi bana abhi.", color = TextSecondary, fontSize = 18.sp) }
            }
            items(history, key = { "b${it.billId}" }) { bill ->
                Surface(color = CardBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                bill.customerName.ifBlank { "Bill #${bill.billId}" } +
                                        if (bill.closedAt == null) "  (chalu)" else "",
                                fontSize = 19.sp,
                                color = if (bill.closedAt == null) OnlineGreen else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${timeFmt.format(Date(bill.createdAt))} · ${bill.lineCount} items",
                                fontSize = 15.sp, color = TextSecondary
                            )
                        }
                        Text("₹${formatQty(bill.total)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Saffron)
                    }
                }
            }

            item { Spacer(Modifier.padding(bottom = 16.dp)) }
        }
    }
}
