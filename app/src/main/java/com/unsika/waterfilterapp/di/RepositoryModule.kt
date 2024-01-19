package com.unsika.waterfilterapp.di

import com.unsika.waterfilterapp.data.Repository
import com.unsika.waterfilterapp.data.RepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    fun bindMyRepository(repository: RepositoryImpl): Repository
}