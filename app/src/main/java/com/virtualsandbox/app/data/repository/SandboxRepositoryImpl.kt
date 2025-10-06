package com.virtualsandbox.app.data.repository

import com.virtualsandbox.app.core.SandboxFileManager
import com.virtualsandbox.app.data.local.dao.SandboxSpaceDao
import com.virtualsandbox.app.data.local.dao.VirtualAppDao
import com.virtualsandbox.app.data.local.entity.SandboxSpaceEntity
import com.virtualsandbox.app.data.local.entity.SandboxSpaceWithApps
import com.virtualsandbox.app.data.local.entity.VirtualAppEntity
import com.virtualsandbox.app.domain.model.SandboxProfile
import com.virtualsandbox.app.domain.model.SandboxSpace
import com.virtualsandbox.app.domain.model.VirtualApp
import com.virtualsandbox.app.domain.repository.SandboxRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SandboxRepositoryImpl @Inject constructor(
    private val spaceDao: SandboxSpaceDao,
    private val virtualAppDao: VirtualAppDao,
    private val fileManager: SandboxFileManager,
) : SandboxRepository {

    override fun observeSpaces(): Flow<List<SandboxSpace>> =
        spaceDao.observeSpacesWithApps().map { items ->
            items.map { it.toDomain() }
        }

    override fun observeSpace(spaceId: Long): Flow<SandboxSpace?> =
        spaceDao.observeSpaceWithApps(spaceId).map { it?.toDomain() }

    override suspend fun createSpace(space: SandboxSpace): Long = withContext(Dispatchers.IO) {
        val storageRoot = fileManager.ensureSpaceDirectory(space.storageRoot, space.name)
        val entity = space.copy(storageRoot = storageRoot).toEntity()
        spaceDao.insert(entity)
    }

    override suspend fun updateSpace(space: SandboxSpace) = withContext(Dispatchers.IO) {
        val storageRoot = fileManager.ensureSpaceDirectory(space.storageRoot, space.name)
        val updated = space.copy(storageRoot = storageRoot)
        spaceDao.update(updated.toEntity())
    }

    override suspend fun deleteSpace(spaceId: Long) = withContext(Dispatchers.IO) {
        val existing = spaceDao.getSpace(spaceId)
        spaceDao.delete(spaceId)
        existing?.storageRoot?.let { fileManager.deleteSpaceDirectory(it) }
    }

    override suspend fun addVirtualApp(spaceId: Long, app: VirtualApp): Long =
        withContext(Dispatchers.IO) {
            val entity = app.toEntity(spaceId)
            virtualAppDao.insert(entity)
        }

    override suspend fun removeVirtualApp(appId: Long) = withContext(Dispatchers.IO) {
        virtualAppDao.delete(appId)
    }

    override suspend fun clearSpace(spaceId: Long) = withContext(Dispatchers.IO) {
        virtualAppDao.deleteBySpace(spaceId)
        spaceDao.getSpace(spaceId)?.storageRoot?.let { fileManager.clearSpaceDirectory(it) }
    }

    private fun SandboxSpaceWithApps.toDomain(): SandboxSpace = SandboxSpace(
        id = space.id,
        name = space.name,
        description = space.description,
        storageRoot = space.storageRoot,
        profile = SandboxProfile.fromKey(space.profile),
        networkIsolation = space.networkIsolation,
        lastLaunchedAt = space.lastLaunchedAt,
        createdAt = space.createdAt,
        installedApps = apps.map { it.toDomain() },
    )

    private fun SandboxSpace.toEntity(): SandboxSpaceEntity =
        SandboxSpaceEntity(
            id = id,
            name = name,
            description = description,
            storageRoot = storageRoot,
            profile = profile.name,
            networkIsolation = networkIsolation,
            lastLaunchedAt = lastLaunchedAt,
            createdAt = createdAt,
        )

    private fun VirtualAppEntity.toDomain(): VirtualApp = VirtualApp(
        id = id,
        spaceId = spaceId,
        packageName = packageName,
        displayName = displayName,
        iconUri = iconUri,
        installTime = installTime,
        permissionSnapshot = permissionSnapshot.split("|").filter { it.isNotEmpty() },
    )

    private fun VirtualApp.toEntity(spaceId: Long): VirtualAppEntity = VirtualAppEntity(
        id = id,
        spaceId = spaceId,
        packageName = packageName,
        displayName = displayName,
        iconUri = iconUri,
        installTime = installTime,
        permissionSnapshot = permissionSnapshot.joinToString(separator = "|"),
    )
}
