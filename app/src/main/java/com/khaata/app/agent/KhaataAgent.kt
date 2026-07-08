package com.khaata.app.agent

import com.khaata.app.util.BillingMode
import com.khaata.app.util.ConnectivityObserver

/**
 * The orchestrator (the "Managed Agents" layer): hears a transcript,
 * picks the brain (Gemini online / Gemma offline), executes the resulting
 * tool calls against Room, and produces one spoken + one status reply.
 */
class KhaataAgent(
    private val tools: AgentTools,
    private val liveApi: LiveApiManager,
    private val offline: OfflineModelManager,
    private val connectivity: ConnectivityObserver
) {

    var lastEngine: String = "-"
        private set

    suspend fun processUtterance(transcript: String): AgentResult {
        val (actions, modelReply) = when (connectivity.activeMode()) {
            BillingMode.GEMINI_LIVE -> runCatching {
                lastEngine = "Gemini (online)"
                liveApi.parseUtterance(transcript)
            }.getOrElse {
                // Rate limit / preview flakiness / no config -> seamless offline fallback
                lastEngine = "Gemma/rules (fallback)"
                offline.parseUtterance(transcript)
            }
            BillingMode.GEMMA_OFFLINE -> {
                lastEngine = if (offline.engineName == "gemma") "Gemma (on-device)" else "On-device rules"
                offline.parseUtterance(transcript)
            }
        }
        return execute(actions, modelReply)
    }

    suspend fun processScan(product: ProductIdentification): AgentResult =
        execute(listOf(AgentAction.AddItem(product.productName, product.quantity)), "")

    private suspend fun execute(actions: List<AgentAction>, modelReply: String): AgentResult {
        if (actions.isEmpty()) {
            val msg = modelReply.ifBlank { "समझ नहीं आया, दोबारा बोलिए" }
            return AgentResult(msg, msg)
        }

        val spoken = StringBuilder()
        val status = StringBuilder()
        var billChanged = false
        var showSummary = false

        for (action in actions) {
            when (action) {
                is AgentAction.AddItem -> {
                    val lookup = tools.lookupPrice(action.itemName, action.quantity)
                    val item = lookup.item
                    if (!lookup.found || item == null) {
                        spoken.append("${action.itemName} nahi mila. ")
                        status.append("❓ '${action.itemName}' not in catalog. ")
                        continue
                    }
                    if (!lookup.stockAvailable) {
                        spoken.append("${item.nameEnglish} ka stock kam hai, sirf ${trim(lookup.stockRemaining)} ${item.unit} bacha hai. ")
                        status.append("⚠️ ${item.nameEnglish}: only ${trim(lookup.stockRemaining)} ${item.unit} left. ")
                        continue
                    }
                    val op = tools.addToBill(item, action.quantity)
                    billChanged = true
                    spoken.append("${trim(action.quantity)} ${item.unit} ${item.nameEnglish}, ₹${trim(lookup.totalPrice)}. ")
                    status.append("✅ ${item.nameEnglish} ×${trim(action.quantity)} = ₹${trim(lookup.totalPrice)}${op.message}. ")
                }
                is AgentAction.CheckStock -> {
                    val (item, stock) = tools.checkStock(action.itemName)
                    if (item == null) {
                        spoken.append("${action.itemName} nahi mila. ")
                    } else {
                        spoken.append("${item.nameEnglish} ka stock ${trim(stock)} ${item.unit} hai. ")
                        status.append("📦 ${item.nameEnglish}: ${trim(stock)} ${item.unit}. ")
                    }
                }
                AgentAction.RemoveLast -> {
                    val op = tools.removeLastItem()
                    billChanged = op.success
                    spoken.append(if (op.success) "${op.message} hata diya. " else "Bill khaali hai. ")
                    status.append(if (op.success) "🗑️ Removed ${op.message}. " else "Bill empty. ")
                }
                AgentAction.ClearBill -> {
                    tools.clearBill()
                    billChanged = true
                    spoken.append("Naya bill shuru. ")
                    status.append("🆕 New bill. ")
                }
                AgentAction.Summary -> {
                    val (lines, total) = tools.getSummary()
                    showSummary = true
                    spoken.append("Total ${lines.size} item, ₹${trim(total)}. Dhanyavaad! ")
                    status.append("🧾 ${lines.size} items — ₹${trim(total)}. ")
                }
                is AgentAction.Unknown -> {
                    status.append("❓ ${action.raw}. ")
                }
            }
        }

        val finalSpoken = modelReply.ifBlank { spoken.toString() }.trim()
        return AgentResult(
            spokenReply = finalSpoken,
            statusMessage = status.toString().ifBlank { finalSpoken }.trim(),
            billChanged = billChanged,
            showSummary = showSummary
        )
    }

    private fun trim(v: Double): String =
        if (v == v.toLong().toDouble()) v.toLong().toString() else "%.2f".format(v)
}
