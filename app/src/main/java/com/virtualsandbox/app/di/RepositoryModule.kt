package com.virtualsandbox.app.di

import com.virtualsandbox.app.data.repository.SandboxRepositoryImpl
import com.virtualsandbox.app.domain.repository.SandboxRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSandboxRepository(
        impl: SandboxRepositoryImpl,
    ): SandboxRepository
}
