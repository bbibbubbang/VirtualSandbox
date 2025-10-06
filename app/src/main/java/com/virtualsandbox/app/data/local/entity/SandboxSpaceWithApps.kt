package com.virtualsandbox.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SandboxSpaceWithApps(
    @Embedded
    val space: SandboxSpaceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "space_id",
    )
    val apps: List<VirtualAppEntity>,
)
