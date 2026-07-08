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

@Entity(tableName = "bill_lines")
data class BillLine(
    @PrimaryKey(autoGenerate = true) val lineId: Long = 0,
    val itemId: Int,
    val itemName: String,     // display name at time of billing
    val quantity: Double,
    val unit: String,
    val totalPrice: Double,
    val addedAt: Long = System.currentTimeMillis()
)
