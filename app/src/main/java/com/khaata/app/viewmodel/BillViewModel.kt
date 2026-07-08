package com.khaata.app.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.khaata.app.KhaataApp
import com.khaata.app.agent.ProductIdentification
import com.khaata.app.data.BillLine
import com.khaata.app.data.Item
import com.khaata.app.util.SpeechManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ScanState(
    val inProgress: Boolean = false,
    val pendingConfirm: ProductIdentification? = null,
    val message: String = ""
)

class BillViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as KhaataApp
    private val speech = SpeechManager(application)

    val billLines: StateFlow<List<BillLine>> = app.db.billDao().observeCurrentBill()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val total: StateFlow<Double> = app.db.billDao().observeCurrentBill()
        .map { lines -> lines.sumOf { it.totalPrice } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val lowStockItems: StateFlow<List<Item>> = app.db.itemDao().observeLowStock()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val isOnline: StateFlow<Boolean> = app.connectivity.isOnline

    private val _listening = MutableStateFlow(false)
    val listening: StateFlow<Boolean> = _listening

    private val _statusMessage = MutableStateFlow("Mic दबाकर बोलिए · Tap the mic and speak")
    val statusMessage: StateFlow<String> = _statusMessage

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript

    private val _engineLabel = MutableStateFlow("")
    val engineLabel: StateFlow<String> = _engineLabel

    private val _scanState = MutableStateFlow(ScanState())
    val scanState: StateFlow<ScanState> = _scanState

    private val _navigateToSummary = MutableStateFlow(false)
    val navigateToSummary: StateFlow<Boolean> = _navigateToSummary

    init {
        speech.listener = object : SpeechManager.Listener {
            override fun onPartial(text: String) { _partialTranscript.value = text }
            override fun onFinal(text: String) {
                _partialTranscript.value = ""
                onTranscript(text)
            }
            override fun onError(message: String) {
                _partialTranscript.value = ""
                _statusMessage.value = message
            }
            override fun onListeningChanged(l: Boolean) { _listening.value = l }
        }
    }

    fun toggleListening() {
        if (_listening.value) {
            speech.stop()
            _listening.value = false
        } else {
            app.tts.stop()
            _statusMessage.value = "सुन रहा हूँ…"
            speech.startListening()
        }
    }

    fun onTranscript(text: String) {
        _statusMessage.value = "🎙 \"$text\""
        viewModelScope.launch(Dispatchers.IO) {
            val result = app.agent.processUtterance(text)
            _engineLabel.value = app.agent.lastEngine
            _statusMessage.value = result.statusMessage
            app.tts.speak(result.spokenReply)
            if (result.showSummary) _navigateToSummary.value = true
        }
    }

    fun onFrameCaptured(bitmap: Bitmap) {
        _scanState.value = ScanState(inProgress = true, message = "पहचान रहा हूँ… Identifying…")
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { app.omniFlash.identifyProduct(bitmap) }
                .onSuccess { product ->
                    when {
                        product.confidence > 0.7 -> {
                            val result = app.agent.processScan(product)
                            app.tts.speak(result.spokenReply)
                            _scanState.value = ScanState(message = result.statusMessage)
                        }
                        product.confidence >= 0.4 ->
                            _scanState.value = ScanState(pendingConfirm = product,
                                message = "क्या यह ${product.productName} है?")
                        else ->
                            _scanState.value = ScanState(
                                message = "पहचान नहीं पाया — बोलकर बताइए · Couldn't identify, please speak")
                    }
                }
                .onFailure {
                    _scanState.value = ScanState(
                        message = "Scan needs internet (Gemini vision). Offline में बोलकर add करें।")
                }
        }
    }

    fun confirmScan() {
        val product = _scanState.value.pendingConfirm ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val result = app.agent.processScan(product)
            app.tts.speak(result.spokenReply)
            _scanState.value = ScanState(message = result.statusMessage)
        }
    }

    fun dismissScan() { _scanState.value = ScanState() }

    fun clearBill() {
        viewModelScope.launch(Dispatchers.IO) {
            app.tools.clearBill()
            _statusMessage.value = "🆕 नया बिल · New bill"
            app.tts.speak("Naya bill shuru")
        }
    }

    fun finishBill() { _navigateToSummary.value = true }

    fun summaryShown() { _navigateToSummary.value = false }

    fun shareText(): String {
        val lines = billLines.value
        val sb = StringBuilder("🧾 *Khaata Bill*\n")
        lines.forEach {
            sb.append("• ${it.itemName} ×${fmt(it.quantity)} ${it.unit} = ₹${fmt(it.totalPrice)}\n")
        }
        sb.append("\n*TOTAL: ₹${fmt(total.value)}*\nधन्यवाद 🙏")
        return sb.toString()
    }

    private fun fmt(v: Double): String =
        if (v == v.toLong().toDouble()) v.toLong().toString() else "%.2f".format(v)

    override fun onCleared() {
        speech.stop()
        super.onCleared()
    }
}
