package com.khaata.app

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.khaata.app.agent.AppLanguage
import com.khaata.app.agent.KhaataAgent
import com.khaata.app.agent.OmniFlashManager
import com.khaata.app.data.BillLine
import com.khaata.app.data.BillWithTotal
import com.khaata.app.data.CatalogSeeder
import com.khaata.app.data.Item
import com.khaata.app.data.SoldItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class Screen { MAIN, CAMERA, SUMMARY, INVENTORY, INSIGHTS }
enum class CameraMode { BILL, STOCK }

@OptIn(ExperimentalCoroutinesApi::class)
class KhaataViewModel(app: Application) : AndroidViewModel(app) {

    private val khaataApp = app as KhaataApp
    private val db = khaataApp.db
    val agent = KhaataAgent(app, db, viewModelScope)

    val screen = MutableStateFlow(Screen.MAIN)
    val cameraMode = MutableStateFlow(CameraMode.BILL)

    val isOnline: StateFlow<Boolean> = agent.connectivity.isOnline

    // ----- language -----
    val language = MutableStateFlow(AppLanguage.HINDI)
    fun cycleLanguage() {
        language.value = language.value.next()
        agent.language = language.value
    }

    // ----- current (open) bill -----
    private val openBill = db.billDao().observeOpenBill()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val billLines: StateFlow<List<BillLine>> = openBill
        .flatMapLatest { bill ->
            if (bill == null) flowOf(emptyList()) else db.billDao().observeLines(bill.billId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val billTotal: StateFlow<Double> =
        billLines.map { lines -> lines.sumOf { it.totalPrice } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    // ----- inventory -----
    val allItems: StateFlow<List<Item>> =
        db.itemDao().observeAll()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val lowStockItems: StateFlow<List<Item>> =
        db.itemDao().observeLowStock(3.0)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Item being edited in the inventory dialog; null = dialog closed. */
    val editingItem = MutableStateFlow<Item?>(null)

    fun startAddItem() {
        viewModelScope.launch {
            val newId = db.itemDao().maxId() + 1
            editingItem.value = Item(newId, "", "", "", 0.0, "pkt", 0.0)
        }
    }

    fun startEditItem(item: Item) { editingItem.value = item }

    fun saveItem(item: Item) {
        viewModelScope.launch {
            db.itemDao().insertAll(listOf(item))   // REPLACE on conflict = upsert
            agent.catalog.refresh()
            editingItem.value = null
        }
    }

    fun dismissItemDialog() { editingItem.value = null }

    // ----- insights -----
    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val soldToday: StateFlow<List<SoldItem>> =
        db.billDao().observeSoldSince(startOfToday())
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val billHistory: StateFlow<List<BillWithTotal>> =
        db.billDao().observeBillHistory()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ----- voice -----
    fun onMicPressed() = agent.startListening()
    fun stopAgent() = agent.stopListening()

    // ----- camera (two modes) -----
    val pendingScanConfirmation =
        MutableStateFlow<OmniFlashManager.ProductIdentification?>(null)

    fun openCamera(mode: CameraMode) {
        cameraMode.value = mode
        screen.value = Screen.CAMERA
    }

    fun onFrameCaptured(bitmap: Bitmap) {
        viewModelScope.launch {
            when (cameraMode.value) {
                CameraMode.BILL -> when (val result = agent.scanProduct(bitmap)) {
                    is KhaataAgent.ScanResult.Added -> screen.value = Screen.MAIN
                    is KhaataAgent.ScanResult.NeedsConfirmation -> pendingScanConfirmation.value = result.id
                    is KhaataAgent.ScanResult.NotIdentified -> { /* agent already spoke */ }
                }
                CameraMode.STOCK -> {
                    val id = agent.identifyForStock(bitmap)
                    // Prefill the editable inventory dialog: existing item or a new one
                    val existing = id?.let { agent.catalog.fuzzyMatch(it.productName) }
                    if (existing != null) {
                        editingItem.value = existing
                    } else {
                        val newId = db.itemDao().maxId() + 1
                        editingItem.value = Item(
                            id = newId,
                            nameHindi = "", nameKannada = "",
                            nameEnglish = id?.productName ?: "",
                            unitPrice = 0.0,
                            unit = id?.unit ?: "pkt",
                            stockQty = id?.quantity ?: 0.0
                        )
                    }
                    screen.value = Screen.INVENTORY
                }
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

    // ----- bill lifecycle -----
    val customerName = MutableStateFlow("")

    fun clearBill() {
        viewModelScope.launch { agent.tools.clearBill() }
    }

    fun finishBill() {
        screen.value = Screen.SUMMARY
    }

    /** Close this customer's bill (archives it in history) and start fresh. */
    fun newCustomer() {
        viewModelScope.launch {
            agent.tools.closeBill(customerName.value)
            customerName.value = ""
            screen.value = Screen.MAIN
        }
    }

    /** Pre-seed a clean demo state: fresh catalog stock + empty open bill. */
    fun resetDemo() {
        viewModelScope.launch {
            agent.tools.clearBill()
            db.itemDao().insertAll(CatalogSeeder.CATALOG)
            agent.catalog.refresh()
        }
    }

    override fun onCleared() {
        agent.shutdown()
    }
}
