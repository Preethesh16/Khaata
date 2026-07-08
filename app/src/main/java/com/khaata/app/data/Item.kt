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
