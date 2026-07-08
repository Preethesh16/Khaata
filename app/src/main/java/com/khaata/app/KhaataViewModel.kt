package com.khaata.app

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.khaata.app.agent.KhaataAgent
import com.khaata.app.agent.OmniFlashManager
import com.khaata.app.data.BillLine
import com.khaata.app.data.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen { MAIN, CAMERA, SUMMARY }

class KhaataViewModel(app: Application) : AndroidViewModel(app) {

    private val khaataApp = app as KhaataApp
    val agent = KhaataAgent(app, khaataApp.db, viewModelScope)

    val screen = MutableStateFlow(Screen.MAIN)

    val billLines: StateFlow<List<BillLine>> =
        khaataApp.db.billDao().observeCurrentBill()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val billTotal: StateFlow<Double> =
        billLines.map { lines -> lines.sumOf { it.totalPrice } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val lowStockItems: StateFlow<List<Item>> =
        khaataApp.db.itemDao().observeAll()
            .map { items -> items.filter { it.stockQty < 3 } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val isOnline: StateFlow<Boolean> = agent.connectivity.isOnline

    val pendingScanConfirmation =
        MutableStateFlow<OmniFlashManager.ProductIdentification?>(null)

    init {
        agent.warmUp()
    }

    fun onMicPressed() = agent.startListening()
    fun onMicReleased() { /* recognizer auto-stops on end of speech; live session keeps running */ }
    fun stopAgent() = agent.stopListening()

    fun onFrameCaptured(bitmap: Bitmap) {
        viewModelScope.launch {
            when (val result = agent.scanProduct(bitmap)) {
                is KhaataAgent.ScanResult.Added -> screen.value = Screen.MAIN
                is KhaataAgent.ScanResult.NeedsConfirmation -> pendingScanConfirmation.value = result.id
                is KhaataAgent.ScanResult.NotIdentified -> { /* agent already spoke */ }
            }
        }
    }

    fun confirmScan(confirmed: Boolean) {
        val id = pendingScanConfirmation.value ?: return
        pendingScanConfirmation.value = null
        if (confirmed) {
            viewModelScope.launch {
                agent.addItemByName(id.productName, id.quantity)
                screen.value = Screen.MAIN
            }
        }
    }

    fun clearBill() {
        viewModelScope.launch { agent.tools.clearBill() }
    }

    fun finishBill() {
        screen.value = Screen.SUMMARY
    }

    fun newCustomer() {
        viewModelScope.launch {
            agent.tools.clearBill()
            screen.value = Screen.MAIN
        }
    }

    override fun onCleared() {
        agent.shutdown()
    }
}
