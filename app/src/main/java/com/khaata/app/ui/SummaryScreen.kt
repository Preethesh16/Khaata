package com.khaata.app.ui

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khaata.app.ui.theme.Saffron
import com.khaata.app.viewmodel.BillViewModel

@Composable
fun SummaryScreen(vm: BillViewModel, onNewBill: () -> Unit, onBack: () -> Unit) {
    val lines by vm.billLines.collectAsState()
    val total by vm.total.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("🧾 बिल · Bill Summary", style = MaterialTheme.typography.titleLarge, color = Saffron)
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(lines, key = { it.id }) { line ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(line.itemName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text("×${fmt(line.quantity)} ${line.unit}", color = Color.Gray, fontSize = 18.sp)
                    Spacer(Modifier.width(16.dp))
                    Text("₹${fmt(line.totalPrice)}", style = MaterialTheme.typography.titleMedium, color = Saffron)
                }
            }
        }

        HorizontalDivider(color = Color.DarkGray)
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("${lines.size} items", color = Color.Gray, fontSize = 18.sp)
                Text("TOTAL", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Text("₹${fmt(total)}", style = MaterialTheme.typography.headlineLarge, color = Saffron)
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, vm.shareText())
                    }
                    context.startActivity(Intent.createChooser(share, "Share bill"))
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("SHARE", fontSize = 18.sp)
            }
            Button(onClick = onNewBill, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("नया बिल", fontSize = 18.sp)
            }
        }
    }
}
