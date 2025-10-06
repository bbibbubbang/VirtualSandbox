package com.virtualsandbox.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.virtualsandbox.app.data.local.entity.VirtualAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VirtualAppDao {
    @Query("SELECT * FROM virtual_apps WHERE space_id = :spaceId ORDER BY install_time DESC")
    fun observeApps(spaceId: Long): Flow<List<VirtualAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: VirtualAppEntity): Long

    @Update
    suspend fun update(app: VirtualAppEntity)

    @Query("DELETE FROM virtual_apps WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM virtual_apps WHERE space_id = :spaceId")
    suspend fun deleteBySpace(spaceId: Long)
}
