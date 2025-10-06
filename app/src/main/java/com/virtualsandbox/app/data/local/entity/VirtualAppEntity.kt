package com.virtualsandbox.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "virtual_apps",
    foreignKeys = [
        ForeignKey(
            entity = SandboxSpaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["space_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["space_id"]),
        Index(value = ["package_name", "space_id"], unique = true)
    ]
)
data class VirtualAppEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "space_id")
    val spaceId: Long,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "display_name")
    val displayName: String,
    @ColumnInfo(name = "icon_uri")
    val iconUri: String?,
    @ColumnInfo(name = "install_time")
    val installTime: Long,
    @ColumnInfo(name = "permission_snapshot")
    val permissionSnapshot: String,
)
