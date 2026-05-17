package com.multiply.kids.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert
    suspend fun insert(session: SessionEntity): Long

    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query(
        "SELECT MAX(CAST(score AS FLOAT) / total) FROM sessions WHERE tableNumber = :table"
    )
    suspend fun getBestPercentForTable(table: Int): Float?

    @Query("SELECT * FROM sessions WHERE synced = 0")
    suspend fun getUnsynced(): List<SessionEntity>

    @Query("UPDATE sessions SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)
}
