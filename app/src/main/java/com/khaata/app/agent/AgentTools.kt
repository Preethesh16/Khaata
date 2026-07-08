package com.khaata.app.agent

import com.khaata.app.data.AppDatabase
import com.khaata.app.data.BillLine
import com.khaata.app.data.CatalogRepository
import com.khaata.app.data.Item

data class LookupResult(
    val found: Boolean,
    val item: Item? = null,
    val totalPrice: Double = 0.0,
    val stockAvailable: Boolean = false,
    val stockRemaining: Double = 0.0
)

data class BillOpResult(val success: Boolean, val billTotal: Double, val message: String = "")

/**
 * The 7 agent tools from the master plan. Every path — Gemini function call,
 * Gemma offline parse, camera scan — funnels into these.
 */
class AgentTools(
    private val db: AppDatabase,
    private val catalog: CatalogRepository
) {

    // Tool 1: lookup_price
    suspend fun lookupPrice(itemName: String, quantity: Double): LookupResult {
        val item = catalog.fuzzyMatch(itemName) ?: return LookupResult(found = false)
        return LookupResult(
            found = true,
            item = item,
            totalPrice = item.unitPrice * quantity,
            stockAvailable = item.stockQty >= quantity,
            stockRemaining = item.stockQty
        )
    }

    // Tool 2 + 3: add_to_bill + update_stock (atomic from the agent's view)
    suspend fun addToBill(item: Item, quantity: Double): BillOpResult {
        val total = item.unitPrice * quantity
        db.billDao().insert(
            BillLine(
                itemId = item.id,
                itemName = item.nameEnglish,
                quantity = quantity,
                unit = item.unit,
                totalPrice = total
            )
        )
        db.itemDao().decrementStock(item.id, quantity)
        catalog.refresh()
        val newStock = db.itemDao().getStock(item.id) ?: 0.0
        val warn = if (newStock < 3) " (stock kam hai: $newStock ${item.unit})" else ""
        return BillOpResult(true, db.billDao().getTotal(), warn)
    }

    // Tool 4: check_stock
    suspend fun checkStock(itemName: String): Pair<Item?, Double> {
        val item = catalog.fuzzyMatch(itemName)
        return item to (item?.stockQty ?: 0.0)
    }

    // Tool 5: get_summary
    suspend fun getSummary(): Pair<List<BillLine>, Double> {
        val lines = db.billDao().getCurrentBill()
        return lines to lines.sumOf { it.totalPrice }
    }

    // Tool 6: remove_last_item (restores stock)
    suspend fun removeLastItem(): BillOpResult {
        val last = db.billDao().getLastLine()
            ?: return BillOpResult(false, 0.0, "Bill khaali hai")
        db.billDao().deleteById(last.id)
        db.itemDao().incrementStock(last.itemId, last.quantity)
        catalog.refresh()
        return BillOpResult(true, db.billDao().getTotal(), last.itemName)
    }

    // Tool 7: clear_bill (restores stock for all lines — new customer)
    suspend fun clearBill(): BillOpResult {
        db.billDao().getCurrentBill().forEach { line ->
            db.itemDao().incrementStock(line.itemId, line.quantity)
        }
        db.billDao().clearCurrentBill()
        catalog.refresh()
        return BillOpResult(true, 0.0)
    }
}
