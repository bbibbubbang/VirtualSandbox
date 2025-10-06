package com.virtualsandbox.app.domain.repository

import com.virtualsandbox.app.domain.model.SandboxSpace
import com.virtualsandbox.app.domain.model.VirtualApp
import kotlinx.coroutines.flow.Flow

interface SandboxRepository {
    fun observeSpaces(): Flow<List<SandboxSpace>>
    fun observeSpace(spaceId: Long): Flow<SandboxSpace?>

    suspend fun createSpace(space: SandboxSpace): Long
    suspend fun updateSpace(space: SandboxSpace)
    suspend fun deleteSpace(spaceId: Long)

    suspend fun addVirtualApp(spaceId: Long, app: VirtualApp): Long
    suspend fun removeVirtualApp(appId: Long)
    suspend fun clearSpace(spaceId: Long)
}
