package com.virtualsandbox.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sandbox_spaces")
data class SandboxSpaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    @ColumnInfo(name = "storage_root")
    val storageRoot: String,
    val profile: String,
    @ColumnInfo(name = "network_isolation")
    val networkIsolation: Boolean,
    @ColumnInfo(name = "last_launched_at")
    val lastLaunchedAt: Long?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
