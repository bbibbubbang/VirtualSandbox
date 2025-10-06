package com.virtualsandbox.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.virtualsandbox.app.data.local.dao.SandboxSpaceDao
import com.virtualsandbox.app.data.local.dao.VirtualAppDao
import com.virtualsandbox.app.data.local.entity.SandboxSpaceEntity
import com.virtualsandbox.app.data.local.entity.VirtualAppEntity

@Database(
    entities = [SandboxSpaceEntity::class, VirtualAppEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class VirtualSandboxDatabase : RoomDatabase() {
    abstract fun sandboxSpaceDao(): SandboxSpaceDao
    abstract fun virtualAppDao(): VirtualAppDao
}
