package com.khaata.app.agent

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.khaata.app.data.AppDatabase
import com.khaata.app.data.CatalogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * The orchestrator. One entry point — startListening() — and the agent decides:
 * ONLINE  -> Gemini Live API full-duplex voice session (model speaks back itself)
 * OFFLINE -> on-device STT -> Gemma/rule parsing -> same tools -> Android TTS
 * The shopkeeper never notices the switch.
 */
class KhaataAgent(
    context: Context,
    private val db: AppDatabase,
    private val scope: CoroutineScope
) {
    val catalog = CatalogRepository(db)
    val tools = AgentTools(db, catalog)
    val connectivity = ConnectivityObserver(context)
    private val liveApi = LiveApiManager(tools) { toolName -> _lastToolCall.value = toolName }
    private val offlineModel = OfflineModelManager(context, catalog)
    private val speechInput = SpeechInputManager(context)
    private val tts = TtsManager(context)
    val omniFlash = OmniFlashManager()

    sealed class AgentState {
        object Idle : AgentState()
        object ListeningOnline : AgentState()
        object ListeningOffline : AgentState()
        object Processing : AgentState()
        data class Error(val message: String) : AgentState()
    }

    private val _state = MutableStateFlow<AgentState>(AgentState.Idle)
    val state: StateFlow<AgentState> = _state

    private val _lastToolCall = MutableStateFlow("")
    val lastToolCall: StateFlow<String> = _lastToolCall

    private val _agentMessage = MutableStateFlow("")
    val agentMessage: StateFlow<String> = _agentMessage

    /** Active billing language — drives STT recognition and TTS voice. */
    @Volatile var language: AppLanguage = AppLanguage.HINDI

    fun warmUp() {
        scope.launch {
            catalog.refresh()
            offlineModel.initialize()   // pre-warm Gemma so first offline query is fast
        }
    }

    // ---------------- entry point ----------------

    fun startListening() {
        when (connectivity.activeMode()) {
            BillingMode.GEMINI_LIVE -> startOnline()
            BillingMode.GEMMA_OFFLINE -> startOffline()
        }
    }

    fun stopListening() {
        scope.launch { liveApi.stopSession() }
        speechInput.stop()
        _state.value = AgentState.Idle
    }

    // ---------------- online: Gemini Live ----------------

    private fun startOnline() {
        scope.launch {
            try {
                _state.value = AgentState.ListeningOnline
                liveApi.startSession()
            } catch (e: Exception) {
                Log.e(TAG, "Live API failed — degrading to offline mode", e)
                _agentMessage.value = "Online mode failed, switching to offline"
                startOffline()
            }
        }
    }

    // ---------------- offline: STT -> Gemma/rules -> tools -> TTS ----------------

    private fun startOffline() {
        _state.value = AgentState.ListeningOffline
        speechInput.startListening(
            languageTag = language.sttTag,
            onResult = { utterance ->
                _agentMessage.value = "Suna: \"$utterance\""
                scope.launch { processOfflineUtterance(utterance) }
            },
            onError = { message ->
                _state.value = AgentState.Idle
                _agentMessage.value = message
                tts.speak(message)
            }
        )
    }

    suspend fun processOfflineUtterance(utterance: String) {
        _state.value = AgentState.Processing
        try {
            val actions = offlineModel.parseUtterance(utterance)
            for (action in actions) {
                when (action) {
                    is OfflineModelManager.Action.AddItem -> addItemByName(action.itemName, action.quantity)
                    is OfflineModelManager.Action.Summary -> {
                        val summary = tools.getSummary()
                        _lastToolCall.value = "get_summary"
                        val total = summary["total"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                        val count = summary["itemCount"]?.jsonPrimitive?.intOrNull ?: 0
                        speakAndShow("Bill khatam. $count item, total ${rupees(total)}")
                    }
                    is OfflineModelManager.Action.RemoveLast -> {
                        val result = tools.removeLastItem()
                        _lastToolCall.value = "remove_last_item"
                        val removed = result["removedItem"]?.jsonPrimitive?.content
                        speakAndShow(
                            if (removed != null) "$removed hataya. Naya total ${rupees(result["newTotal"]?.jsonPrimitive?.doubleOrNull ?: 0.0)}"
                            else "Bill khaali hai"
                        )
                    }
                    is OfflineModelManager.Action.ClearBill -> {
                        tools.clearBill()
                        _lastToolCall.value = "clear_bill"
                        speakAndShow("Nayi bill shuru")
                    }
                    is OfflineModelManager.Action.Unknown ->
                        speakAndShow("\"${action.raw}\" samajh nahi aaya, dobara boliye")
                }
            }
        } finally {
            _state.value = AgentState.Idle
        }
    }

    /** Shared by offline voice AND camera scan: name -> lookup -> bill -> stock. */
    suspend fun addItemByName(itemName: String, quantity: Double) {
        val lookup = tools.lookupPrice(itemName, quantity)
        _lastToolCall.value = "lookup_price"
        val found = lookup["found"]?.jsonPrimitive?.booleanOrNull == true
        if (!found) {
            speakAndShow("$itemName nahi mila, dobara boliye")
            return
        }
        val itemId = lookup["itemId"]?.jsonPrimitive?.intOrNull ?: return
        val displayName = lookup["itemNameHindi"]?.jsonPrimitive?.content
            ?: lookup["itemName"]?.jsonPrimitive?.content ?: itemName
        val stockOk = lookup["stockAvailable"]?.jsonPrimitive?.booleanOrNull == true
        if (!stockOk) {
            val remaining = lookup["stockRemaining"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            speakAndShow("$displayName ka stock kam hai, sirf $remaining bacha hai")
            return
        }
        val addResult = tools.addToBill(itemId, quantity)
        _lastToolCall.value = "add_to_bill"
        tools.updateStock(itemId, quantity)
        _lastToolCall.value = "update_stock"
        val lineTotal = addResult["lineTotal"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        speakAndShow("$displayName ${formatQty(quantity)} — ${rupees(lineTotal)}")
    }

    // ---------------- camera scan (Omni Flash) ----------------

    sealed class ScanResult {
        data class Added(val name: String) : ScanResult()
        data class NeedsConfirmation(val id: OmniFlashManager.ProductIdentification) : ScanResult()
        object NotIdentified : ScanResult()
    }

    suspend fun scanProduct(frame: Bitmap): ScanResult {
        _state.value = AgentState.Processing
        try {
            val id = omniFlash.identifyProduct(frame) ?: return ScanResult.NotIdentified
            return when {
                id.confidence >= OmniFlashManager.AUTO_ADD_THRESHOLD -> {
                    addItemByName(id.productName, id.quantity)
                    ScanResult.Added(id.productName)
                }
                id.confidence >= OmniFlashManager.CONFIRM_THRESHOLD -> ScanResult.NeedsConfirmation(id)
                else -> {
                    speakAndShow("Pehchan nahi paya, item boliye")
                    ScanResult.NotIdentified
                }
            }
        } finally {
            _state.value = AgentState.Idle
        }
    }

    // ---------------- helpers ----------------

    private fun speakAndShow(message: String) {
        _agentMessage.value = message
        // Online mode: Gemini Live speaks its own audio; TTS only for offline/scan flows
        if (_state.value !is AgentState.ListeningOnline) tts.speak(message, language.ttsLocale)
    }

    /** Camera → identify a product for INVENTORY (no bill changes). */
    suspend fun identifyForStock(frame: Bitmap): OmniFlashManager.ProductIdentification? {
        _state.value = AgentState.Processing
        return try {
            omniFlash.identifyProduct(frame)?.takeIf { it.confidence >= OmniFlashManager.CONFIRM_THRESHOLD }
        } finally {
            _state.value = AgentState.Idle
        }
    }

    private fun rupees(amount: Double): String =
        if (amount == amount.toLong().toDouble()) "${amount.toLong()} rupaye" else "$amount rupaye"

    private fun formatQty(q: Double): String =
        if (q == q.toLong().toDouble()) q.toLong().toString() else q.toString()

    fun shutdown() {
        stopListening()
        tts.shutdown()
    }

    companion object {
        private const val TAG = "KhaataAgent"
    }
}
