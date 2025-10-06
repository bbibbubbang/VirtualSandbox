package com.virtualsandbox.app.core

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SandboxFileManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun ensureSpaceDirectory(currentPath: String, name: String): String {
        val baseDir = File(context.filesDir, "virtual_spaces")
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        if (currentPath.isNotEmpty()) {
            val existing = File(currentPath)
            if (existing.exists()) {
                return existing.absolutePath
            }
        }
        val sanitized = name.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        val spaceDir = File(baseDir, "space_${sanitized}_${System.currentTimeMillis()}")
        if (!spaceDir.exists()) {
            spaceDir.mkdirs()
        }
        File(spaceDir, "apps").mkdirs()
        File(spaceDir, "media").mkdirs()
        File(spaceDir, "cache").mkdirs()
        return spaceDir.absolutePath
    }

    fun deleteSpaceDirectory(path: String) {
        val directory = File(path)
        if (directory.exists()) {
            directory.deleteRecursively()
        }
    }

    fun clearSpaceDirectory(path: String) {
        val directory = File(path)
        if (directory.exists()) {
            directory.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    file.deleteRecursively()
                } else {
                    file.delete()
                }
            }
        }
    }

    fun calculateDirectorySize(path: String): Long {
        val directory = File(path)
        if (!directory.exists()) return 0L
        return directory.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }
}
