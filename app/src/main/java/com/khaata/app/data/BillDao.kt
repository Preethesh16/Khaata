package com.khaata.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    // ----- bills (one per customer) -----

    @Insert
    suspend fun createBill(bill: Bill): Long

    @Query("SELECT * FROM bills WHERE closedAt IS NULL ORDER BY createdAt DESC LIMIT 1")
    suspend fun getOpenBill(): Bill?

    @Query("SELECT * FROM bills WHERE closedAt IS NULL ORDER BY createdAt DESC LIMIT 1")
    fun observeOpenBill(): Flow<Bill?>

    @Query("UPDATE bills SET closedAt = :time, customerName = :customerName WHERE billId = :billId")
    suspend fun closeBill(billId: Long, customerName: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE bills SET customerName = :name WHERE billId = :billId")
    suspend fun renameBill(billId: Long, name: String)

    @Query("""
        SELECT b.billId, b.customerName, b.createdAt, b.closedAt,
               COALESCE(SUM(l.totalPrice), 0.0) AS total,
               COUNT(l.lineId) AS lineCount
        FROM bills b LEFT JOIN bill_lines l ON l.billId = b.billId
        GROUP BY b.billId ORDER BY b.createdAt DESC LIMIT 50
    """)
    fun observeBillHistory(): Flow<List<BillWithTotal>>

    // ----- lines -----

    @Insert
    suspend fun insert(line: BillLine): Long

    @Query("SELECT * FROM bill_lines WHERE billId = :billId ORDER BY addedAt ASC")
    fun observeLines(billId: Long): Flow<List<BillLine>>

    @Query("SELECT * FROM bill_lines WHERE billId = :billId ORDER BY addedAt ASC")
    suspend fun getLines(billId: Long): List<BillLine>

    @Query("SELECT COALESCE(SUM(totalPrice), 0.0) FROM bill_lines WHERE billId = :billId")
    suspend fun getTotal(billId: Long): Double

    @Query("SELECT * FROM bill_lines WHERE billId = :billId ORDER BY addedAt DESC LIMIT 1")
    suspend fun getLastLine(billId: Long): BillLine?

    @Query("DELETE FROM bill_lines WHERE lineId = :lineId")
    suspend fun deleteLine(lineId: Long)

    @Query("DELETE FROM bill_lines WHERE billId = :billId")
    suspend fun clearLines(billId: Long)

    // ----- insights -----

    @Query("""
        SELECT l.itemId AS itemId, l.itemName AS itemName, l.unit AS unit,
               SUM(l.quantity) AS totalQty, SUM(l.totalPrice) AS revenue
        FROM bill_lines l WHERE l.addedAt >= :sinceMillis
        GROUP BY l.itemId ORDER BY revenue DESC
    """)
    fun observeSoldSince(sinceMillis: Long): Flow<List<SoldItem>>
}
