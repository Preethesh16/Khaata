package com.khaata.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import com.khaata.app.data.BillLine
import com.khaata.app.ui.theme.OfflineOrange
import com.khaata.app.ui.theme.OnlineGreen
import com.khaata.app.ui.theme.Saffron
import com.khaata.app.ui.theme.WarnRed
import com.khaata.app.viewmodel.BillViewModel

@Composable
fun MainScreen(vm: BillViewModel, onScan: () -> Unit, onDone: () -> Unit) {
    val lines by vm.billLines.collectAsState()
    val total by vm.total.collectAsState()
    val isOnline by vm.isOnline.collectAsState()
    val listening by vm.listening.collectAsState()
    val status by vm.statusMessage.collectAsState()
    val partial by vm.partialTranscript.collectAsState()
    val engine by vm.engineLabel.collectAsState()
    val lowStock by vm.lowStockItems.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Zone 1: status bar ──────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeBadge(isOnline = isOnline, engine = engine)
            Spacer(Modifier.weight(1f))
            Text("KHAATA · खाता", style = MaterialTheme.typography.titleMedium, color = Saffron)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { vm.clearBill() }) {
                Icon(Icons.Filled.Delete, contentDescription = "Clear bill", tint = Color.Gray)
            }
        }

        // Low stock strip
        if (lowStock.isNotEmpty()) {
            Text(
                text = "⚠ LOW STOCK: " + lowStock.joinToString { it.nameEnglish },
                color = WarnRed,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WarnRed.copy(alpha = 0.12f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // ── Zone 2: animated bill list ─────────────────────────────
        val listState = rememberLazyListState()
        LaunchedEffect(lines.size) {
            if (lines.isNotEmpty()) listState.animateScrollToItem(lines.size - 1)
        }
        Box(modifier = Modifier.weight(1f)) {
            if (lines.isEmpty()) {
                Text(
                    text = "बिल खाली है · Bill is empty\n\n🎤 Mic दबाकर बोलिए",
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(lines, key = { it.id }) { line ->
                        BillLineRow(line)
                    }
                }
            }
        }

        // Total bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TOTAL", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Spacer(Modifier.weight(1f))
            Text("₹${fmt(total)}", style = MaterialTheme.typography.headlineLarge, color = Saffron)
        }

        // ── Zone 3: mic + actions ──────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (partial.isNotBlank()) "\"$partial\"" else status,
                color = if (listening) OnlineGreen else Color.LightGray,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(10.dp))
            MicButton(listening = listening, onClick = { vm.toggleListening() })
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (listening) "सुन रहा हूँ…" else "बोलो अपना order",
                color = Color.Gray,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                OutlinedButton(onClick = onScan) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Saffron)
                    Spacer(Modifier.width(8.dp))
                    Text("SCAN", fontSize = 18.sp)
                }
                OutlinedButton(onClick = onDone) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = OnlineGreen)
                    Spacer(Modifier.width(8.dp))
                    Text("DONE", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun ModeBadge(isOnline: Boolean, engine: String) {
    val color = if (isOnline) OnlineGreen else OfflineOrange
    val label = if (isOnline) "ONLINE" else "OFFLINE"
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(6.dp))
            Text(label, color = color, fontSize = 16.sp)
        }
        if (engine.isNotBlank()) {
            Text(engine, color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun BillLineRow(line: BillLine) {
    // Each line slides in as the agent adds it
    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(350)) + fadeIn()
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✅ ", fontSize = 20.sp)
                Column(Modifier.weight(1f)) {
                    Text(line.itemName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${fmt(line.quantity)} ${line.unit}",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
                Text("₹${fmt(line.totalPrice)}", style = MaterialTheme.typography.titleMedium, color = Saffron)
            }
        }
    }
}

@Composable
fun MicButton(listening: Boolean, onClick: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "pulse")
    val scale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = if (listening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "pulseScale"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(96.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(if (listening) Saffron else Saffron.copy(alpha = 0.85f))
            .clickable { onClick() }
    ) {
        Icon(
            Icons.Filled.Mic,
            contentDescription = "Speak",
            tint = Color.White,
            modifier = Modifier.size(44.dp)
        )
    }
}

internal fun fmt(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString() else "%.2f".format(v)
