package com.virtualsandbox.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.virtualsandbox.app.data.local.entity.SandboxSpaceEntity
import com.virtualsandbox.app.data.local.entity.SandboxSpaceWithApps
import kotlinx.coroutines.flow.Flow

@Dao
interface SandboxSpaceDao {
    @Transaction
    @Query("SELECT * FROM sandbox_spaces ORDER BY created_at DESC")
    fun observeSpacesWithApps(): Flow<List<SandboxSpaceWithApps>>

    @Transaction
    @Query("SELECT * FROM sandbox_spaces WHERE id = :id")
    fun observeSpaceWithApps(id: Long): Flow<SandboxSpaceWithApps?>

    @Query("SELECT * FROM sandbox_spaces ORDER BY created_at DESC")
    fun observeSpaces(): Flow<List<SandboxSpaceEntity>>

    @Query("SELECT * FROM sandbox_spaces WHERE id = :id")
    fun observeSpace(id: Long): Flow<SandboxSpaceEntity?>

    @Query("SELECT * FROM sandbox_spaces WHERE id = :id")
    suspend fun getSpace(id: Long): SandboxSpaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(space: SandboxSpaceEntity): Long

    @Update
    suspend fun update(space: SandboxSpaceEntity)

    @Query("DELETE FROM sandbox_spaces WHERE id = :id")
    suspend fun delete(id: Long)
}
