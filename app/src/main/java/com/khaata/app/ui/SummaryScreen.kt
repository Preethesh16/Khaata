package com.khaata.app.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khaata.app.KhaataViewModel
import com.khaata.app.Screen
import com.khaata.app.ui.theme.CardBg
import com.khaata.app.ui.theme.OnlineGreen
import com.khaata.app.ui.theme.Saffron
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SummaryScreen(viewModel: KhaataViewModel) {
    val billLines by viewModel.billLines.collectAsState()
    val total by viewModel.billTotal.collectAsState()
    val context = LocalContext.current

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text("BILL SUMMARY", style = MaterialTheme.typography.titleLarge, color = Saffron)
            Text(
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date()),
                fontSize = 18.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(24.dp))

            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(billLines, key = { it.lineId }) { line ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            "${line.itemName} × ${formatQty(line.quantity)}${line.unit}",
                            fontSize = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text("₹${formatQty(line.totalPrice)}", fontSize = 22.sp)
                    }
                }
            }

            HorizontalDivider(color = Color.DarkGray)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("${billLines.size} items", fontSize = 20.sp, color = Color.Gray)
                    Text("TOTAL", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "₹${formatQty(total)}",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Saffron
                )
            }
            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val text = buildString {
                            appendLine("🧾 Khaata Bill")
                            billLines.forEach {
                                appendLine("${it.itemName} x ${formatQty(it.quantity)}${it.unit} = ₹${formatQty(it.totalPrice)}")
                            }
                            appendLine("—")
                            appendLine("TOTAL: ₹${formatQty(total)}")
                        }
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(send, "Share bill"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("📤 SHARE", fontSize = 20.sp) }
                Button(
                    onClick = { viewModel.newCustomer() },
                    colors = ButtonDefaults.buttonColors(containerColor = OnlineGreen),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("🆕 NAYI BILL", fontSize = 20.sp, color = Color.Black) }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.screen.value = Screen.MAIN },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) { Text("← Wapas", fontSize = 18.sp, color = Color.Gray) }
        }
    }
}
