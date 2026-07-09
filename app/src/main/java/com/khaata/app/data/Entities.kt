package com.khaata.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey val id: Int,
    val nameHindi: String,
    val nameKannada: String,
    val nameEnglish: String,
    val unitPrice: Double,
    val unit: String,        // "kg", "pkt", "piece", "litre", "dozen"
    val stockQty: Double
)

/** One customer's bill. Open bill = closedAt == null. */
@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true) val billId: Long = 0,
    val customerName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null
)

@Entity(tableName = "bill_lines")
data class BillLine(
    @PrimaryKey(autoGenerate = true) val lineId: Long = 0,
    val billId: Long,
    val itemId: Int,
    val itemName: String,     // display name at time of billing
    val quantity: Double,
    val unit: String,
    val totalPrice: Double,
    val addedAt: Long = System.currentTimeMillis()
)

/** Aggregated row for the insights screen: what sold today. */
data class SoldItem(
    val itemId: Int,
    val itemName: String,
    val unit: String,
    val totalQty: Double,
    val revenue: Double
)

/** Bill + its total, for the history list. */
data class BillWithTotal(
    val billId: Long,
    val customerName: String,
    val createdAt: Long,
    val closedAt: Long?,
    val total: Double,
    val lineCount: Int
)
