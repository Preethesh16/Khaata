package com.khaata.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bill_lines")
data class BillLine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Int,
    val itemName: String,      // display name at time of billing
    val quantity: Double,
    val unit: String,
    val totalPrice: Double,
    val createdAt: Long = System.currentTimeMillis()
)
