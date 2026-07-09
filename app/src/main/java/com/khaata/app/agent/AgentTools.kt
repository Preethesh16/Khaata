package com.khaata.app.agent

import com.khaata.app.data.AppDatabase
import com.khaata.app.data.Bill
import com.khaata.app.data.BillLine
import com.khaata.app.data.CatalogRepository
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * The 7 agent tools. Both Gemini Live (online) and Gemma (offline) call these —
 * same functions, different brains. All bill operations target the currently
 * OPEN bill (one bill per customer; DONE closes it, a new one opens on demand).
 */
class AgentTools(
    private val db: AppDatabase,
    private val catalog: CatalogRepository
) {

    /** The open bill's id, creating a fresh bill if none is open. */
    suspend fun currentBillId(): Long =
        db.billDao().getOpenBill()?.billId ?: db.billDao().createBill(Bill())

    suspend fun lookupPrice(itemName: String, quantity: Double): JsonObject {
        val item = catalog.fuzzyMatch(itemName)
        return buildJsonObject {
            put("found", item != null)
            if (item != null) {
                put("itemId", item.id)
                put("itemName", item.nameEnglish)
                put("itemNameHindi", item.nameHindi)
                put("unitPrice", item.unitPrice)
                put("unit", item.unit)
                put("totalPrice", item.unitPrice * quantity)
                put("stockAvailable", item.stockQty >= quantity)
                put("stockRemaining", item.stockQty)
            } else {
                put("message", "Item not found in catalog. Ask the shopkeeper to repeat.")
            }
        }
    }

    suspend fun addToBill(itemId: Int, quantity: Double): JsonObject {
        val item = db.itemDao().getById(itemId)
            ?: return buildJsonObject {
                put("success", false)
                put("message", "Unknown itemId $itemId")
            }
        val billId = currentBillId()
        val totalPrice = item.unitPrice * quantity
        db.billDao().insert(
            BillLine(
                billId = billId,
                itemId = item.id,
                itemName = item.nameHindi,
                quantity = quantity,
                unit = item.unit,
                totalPrice = totalPrice
            )
        )
        return buildJsonObject {
            put("success", true)
            put("itemName", item.nameEnglish)
            put("lineTotal", totalPrice)
            put("billTotal", db.billDao().getTotal(billId))
        }
    }

    suspend fun updateStock(itemId: Int, quantitySold: Double): JsonObject {
        db.itemDao().decrementStock(itemId, quantitySold)
        catalog.refresh()
        val newStock = db.itemDao().getStock(itemId) ?: 0.0
        return buildJsonObject {
            put("newStockLevel", newStock)
            put("lowStockWarning", newStock < 3)
        }
    }

    suspend fun checkStock(itemName: String): JsonObject {
        val item = catalog.fuzzyMatch(itemName)
        return buildJsonObject {
            put("found", item != null)
            if (item != null) {
                put("itemName", item.nameEnglish)
                put("stockQty", item.stockQty)
                put("unit", item.unit)
            }
        }
    }

    suspend fun getSummary(): JsonObject {
        val billId = currentBillId()
        val lines = db.billDao().getLines(billId)
        return buildJsonObject {
            put("itemCount", lines.size)
            put("total", lines.sumOf { it.totalPrice })
        }
    }

    suspend fun removeLastItem(): JsonObject {
        val billId = currentBillId()
        val last = db.billDao().getLastLine(billId)
            ?: return buildJsonObject {
                put("success", false)
                put("message", "Bill is empty")
            }
        db.billDao().deleteLine(last.lineId)
        db.itemDao().incrementStock(last.itemId, last.quantity)
        catalog.refresh()
        return buildJsonObject {
            put("success", true)
            put("removedItem", last.itemName)
            put("newTotal", db.billDao().getTotal(billId))
        }
    }

    suspend fun clearBill(): JsonObject {
        db.billDao().clearLines(currentBillId())
        return buildJsonObject { put("success", true) }
    }

    /** Close the open bill (customer done, bill archived) — used by the DONE flow. */
    suspend fun closeBill(customerName: String): Long? {
        val open = db.billDao().getOpenBill() ?: return null
        db.billDao().closeBill(open.billId, customerName.ifBlank { "Customer" })
        return open.billId
    }
}
