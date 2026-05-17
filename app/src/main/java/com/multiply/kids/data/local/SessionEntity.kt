package com.multiply.kids.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deviceId: String,
    val childName: String,
    val tableNumber: Int,
    val score: Int,
    val total: Int,
    val durationSec: Float,
    val synced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
