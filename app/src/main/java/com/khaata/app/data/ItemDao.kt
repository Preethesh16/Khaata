package com.khaata.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Item>)

    @Query("SELECT * FROM items ORDER BY nameEnglish")
    suspend fun getAll(): List<Item>

    @Query("SELECT * FROM items ORDER BY nameEnglish")
    fun observeAll(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getById(id: Int): Item?

    @Query("UPDATE items SET stockQty = stockQty - :qty WHERE id = :id")
    suspend fun decrementStock(id: Int, qty: Double)

    @Query("UPDATE items SET stockQty = stockQty + :qty WHERE id = :id")
    suspend fun incrementStock(id: Int, qty: Double)

    @Query("SELECT stockQty FROM items WHERE id = :id")
    suspend fun getStock(id: Int): Double?

    @Query("SELECT COUNT(*) FROM items")
    suspend fun count(): Int
}
