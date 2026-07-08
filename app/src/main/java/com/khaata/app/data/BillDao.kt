package com.khaata.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    @Insert
    suspend fun insert(line: BillLine): Long

    @Query("SELECT * FROM bill_lines ORDER BY createdAt ASC")
    fun observeCurrentBill(): Flow<List<BillLine>>

    @Query("SELECT * FROM bill_lines ORDER BY createdAt ASC")
    suspend fun getCurrentBill(): List<BillLine>

    @Query("SELECT COALESCE(SUM(totalPrice), 0.0) FROM bill_lines")
    suspend fun getTotal(): Double

    @Query("SELECT * FROM bill_lines ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastLine(): BillLine?

    @Query("DELETE FROM bill_lines WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM bill_lines")
    suspend fun clearCurrentBill()
}
