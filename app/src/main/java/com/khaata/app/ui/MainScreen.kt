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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khaata.app.KhaataViewModel
import com.khaata.app.Screen
import com.khaata.app.agent.KhaataAgent
import com.khaata.app.ui.theme.CardBg
import com.khaata.app.ui.theme.OfflineOrange
import com.khaata.app.ui.theme.OnlineGreen
import com.khaata.app.ui.theme.Saffron
import com.khaata.app.ui.theme.TextSecondary
import com.khaata.app.ui.theme.WarnRed

@Composable
fun MainScreen(viewModel: KhaataViewModel) {
    val isOnline by viewModel.isOnline.collectAsState()
    val billLines by viewModel.billLines.collectAsState()
    val total by viewModel.billTotal.collectAsState()
    val agentState by viewModel.agent.state.collectAsState()
    val agentMessage by viewModel.agent.agentMessage.collectAsState()
    val lowStock by viewModel.lowStockItems.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // ── Zone 1: status bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeBadge(isOnline)
                Spacer(Modifier.weight(1f))
                Text("KHAATA", style = MaterialTheme.typography.titleLarge, color = Saffron)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { viewModel.clearBill() }) {
                    Text("🗑️", fontSize = 24.sp)
                }
            }

            if (lowStock.isNotEmpty()) {
                Surface(
                    color = WarnRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "⚠️ Stock kam: " + lowStock.joinToString { it.nameEnglish },
                        color = WarnRed,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // ── Zone 2: animated bill list ──
            val listState = rememberLazyListState()
            LaunchedEffect(billLines.size) {
                if (billLines.isNotEmpty()) listState.animateScrollToItem(billLines.size - 1)
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (billLines.isEmpty()) {
                    item {
                        Text(
                            "Bill khaali hai.\nMic dabao aur order bolo 👇",
                            color = TextSecondary,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(top = 48.dp)
                        )
                    }
                }
                items(billLines, key = { it.lineId }) { line ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInHorizontally(tween(300)) { it } + fadeIn()
                    ) {
                        Surface(color = CardBg, shape = RoundedCornerShape(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✅ ", fontSize = 24.sp)
                                Text(
                                    "${line.itemName} ${formatQty(line.quantity)}${line.unit}",
                                    fontSize = 24.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "₹${formatQty(line.totalPrice)}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Saffron
                                )
                            }
                        }
                    }
                }
            }

            // total row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("TOTAL", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text(
                    "₹${formatQty(total)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Saffron
                )
            }

            if (agentMessage.isNotBlank()) {
                Text(agentMessage, color = TextSecondary, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
            }

            // ── Zone 3: mic + actions ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val listening = agentState is KhaataAgent.AgentState.ListeningOnline ||
                        agentState is KhaataAgent.AgentState.ListeningOffline
                MicButton(listening = listening) {
                    if (listening) viewModel.stopAgent() else viewModel.onMicPressed()
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    when (agentState) {
                        is KhaataAgent.AgentState.ListeningOnline -> "Sun raha hoon… (Gemini Live)"
                        is KhaataAgent.AgentState.ListeningOffline -> "Sun raha hoon… (offline)"
                        is KhaataAgent.AgentState.Processing -> "Soch raha hoon…"
                        else -> "Bolo apna order"
                    },
                    fontSize = 20.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { viewModel.screen.value = Screen.CAMERA },
                        colors = ButtonDefaults.buttonColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("📷  SCAN", fontSize = 20.sp)
                    }
                    Button(
                        onClick = { viewModel.finishBill() },
                        colors = ButtonDefaults.buttonColors(containerColor = OnlineGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("✅  DONE", fontSize = 20.sp, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun ModeBadge(isOnline: Boolean) {
    Surface(
        color = if (isOnline) OnlineGreen.copy(alpha = 0.2f) else OfflineOrange.copy(alpha = 0.2f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(12.dp)
                    .background(if (isOnline) OnlineGreen else OfflineOrange, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (isOnline) "ONLINE" else "OFFLINE",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOnline) OnlineGreen else OfflineOrange
            )
        }
    }
}

@Composable
fun MicButton(listening: Boolean, onClick: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "micPulse")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (listening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "micScale"
    )
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (listening) Saffron else CardBg,
        modifier = Modifier
            .size(96.dp)
            .scale(scale)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text("🎤", fontSize = 40.sp)
        }
    }
}

fun formatQty(q: Double): String =
    if (q == q.toLong().toDouble()) q.toLong().toString() else q.toString()
