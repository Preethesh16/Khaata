package com.khaata.app.agent

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.liveGenerationConfig
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * ONLINE mode. Gemini Live API via Firebase AI Logic — no backend server.
 * Streams mic audio up, plays model audio back, and routes function calls
 * into AgentTools. startAudioConversation() handles capture + playback.
 */
@OptIn(PublicPreviewAPI::class)
class LiveApiManager(
    private val tools: AgentTools,
    private val onToolActivity: (String) -> Unit
) {
    private var session: LiveSession? = null

    private val systemPrompt = """
        You are Khaata, a billing assistant for small Indian kirana shops.
        Listen to the shopkeeper's order in Hindi, Kannada, or English and update the bill using tools.

        RULES:
        - For each item mentioned: call lookup_price first, then add_to_bill with the returned itemId, then update_stock.
        - Parse quantities in Hindi (do=2, teen=3, ek=1, char=4, paanch=5, adha=0.5, paav=0.25, dedh=1.5),
          Kannada (ondu=1, eradu=2, mooru=3), and English.
        - Match items fuzzily — the tool handles aliases like "Parle" -> Parle-G, "cheeni" -> sugar.
        - If lookup_price returns found=false, tell the shopkeeper "yeh item nahi mila, dobara boliye".
        - If stockAvailable is false, warn the shopkeeper before adding.
        - "khatam", "done", "bas", "total" -> call get_summary and speak the total.
        - "hatao", "remove", "galat" -> call remove_last_item.
        - "nayi bill", "new bill", "naya customer" -> call clear_bill.
        - After each addition, confirm briefly: item, quantity, price. Keep replies under 10 words.
        - Respond in the language the shopkeeper is using. Default: Hindi.
    """.trimIndent()

    private fun functionDeclarations() = listOf(
        FunctionDeclaration(
            "lookup_price",
            "Look up a catalog item by spoken name (Hindi/Kannada/English, fuzzy matched). Returns itemId, price, stock.",
            mapOf(
                "itemName" to Schema.string("Item name as spoken, e.g. 'cheeni', 'Parle', 'Maggi'"),
                "quantity" to Schema.double("Quantity requested, e.g. 2.0. 'adha' = 0.5")
            )
        ),
        FunctionDeclaration(
            "add_to_bill",
            "Add a confirmed item to the current bill.",
            mapOf(
                "itemId" to Schema.integer("itemId returned by lookup_price"),
                "quantity" to Schema.double("Quantity to bill")
            )
        ),
        FunctionDeclaration(
            "update_stock",
            "Decrement stock after an item was added to the bill.",
            mapOf(
                "itemId" to Schema.integer("itemId of the sold item"),
                "quantitySold" to Schema.double("Quantity sold")
            )
        ),
        FunctionDeclaration(
            "check_stock",
            "Check current stock of an item without modifying anything.",
            mapOf("itemName" to Schema.string("Item name as spoken"))
        ),
        FunctionDeclaration(
            "get_summary",
            "Get the current bill summary: item count and total.",
            emptyMap()
        ),
        FunctionDeclaration(
            "remove_last_item",
            "Remove the last-added item from the bill (correction) and restore its stock.",
            emptyMap()
        ),
        FunctionDeclaration(
            "clear_bill",
            "Clear the whole bill for a new customer.",
            emptyMap()
        )
    )

    private fun buildModel() = Firebase.ai(backend = GenerativeBackend.googleAI()).liveModel(
        modelName = MODEL_NAME,
        generationConfig = liveGenerationConfig {
            responseModality = ResponseModality.AUDIO
        },
        tools = listOf(Tool.functionDeclarations(functionDeclarations())),
        systemInstruction = content { text(systemPrompt) }
    )

    private fun handleFunctionCall(call: FunctionCallPart): FunctionResponsePart {
        onToolActivity(call.name)
        val args = call.args
        fun str(key: String) = args[key]?.jsonPrimitive?.content ?: ""
        fun dbl(key: String) = try { args[key]?.jsonPrimitive?.double ?: 1.0 } catch (e: Exception) { 1.0 }
        fun intArg(key: String) = try { args[key]?.jsonPrimitive?.int ?: -1 } catch (e: Exception) { -1 }

        val result = runBlocking {
            when (call.name) {
                "lookup_price" -> tools.lookupPrice(str("itemName"), dbl("quantity"))
                "add_to_bill" -> tools.addToBill(intArg("itemId"), dbl("quantity"))
                "update_stock" -> tools.updateStock(intArg("itemId"), dbl("quantitySold"))
                "check_stock" -> tools.checkStock(str("itemName"))
                "get_summary" -> tools.getSummary()
                "remove_last_item" -> tools.removeLastItem()
                "clear_bill" -> tools.clearBill()
                else -> buildJsonObject { put("error", "unknown tool ${call.name}") }
            }
        }
        Log.d(TAG, "tool ${call.name}(${args}) -> $result")
        return FunctionResponsePart(call.name, result)
    }

    /** Connect and start the full-duplex audio conversation. Requires RECORD_AUDIO granted. */
    suspend fun startSession() {
        stopSession()
        val newSession = buildModel().connect()
        session = newSession
        newSession.startAudioConversation(::handleFunctionCall)
        Log.i(TAG, "Gemini Live session started ($MODEL_NAME)")
    }

    suspend fun stopSession() {
        try {
            session?.stopAudioConversation()
            session?.close()
        } catch (e: Exception) {
            Log.w(TAG, "error closing live session", e)
        }
        session = null
    }

    val isActive: Boolean get() = session != null

    companion object {
        private const val TAG = "KhaataLive"
        // Half-cascade live model: audio in, function calling support.
        const val MODEL_NAME = "gemini-live-2.5-flash-preview"
    }
}
