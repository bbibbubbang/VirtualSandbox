package com.virtualsandbox.app.di

import android.content.Context
import androidx.room.Room
import com.virtualsandbox.app.data.local.VirtualSandboxDatabase
import com.virtualsandbox.app.data.local.dao.SandboxSpaceDao
import com.virtualsandbox.app.data.local.dao.VirtualAppDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): VirtualSandboxDatabase = Room.databaseBuilder(
        context,
        VirtualSandboxDatabase::class.java,
        "virtual_sandbox.db",
    ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideSandboxSpaceDao(database: VirtualSandboxDatabase): SandboxSpaceDao =
        database.sandboxSpaceDao()

    @Provides
    fun provideVirtualAppDao(database: VirtualSandboxDatabase): VirtualAppDao =
        database.virtualAppDao()
}
