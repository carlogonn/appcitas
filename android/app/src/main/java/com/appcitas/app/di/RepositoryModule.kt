package com.appcitas.app.di

import com.appcitas.app.data.repository.AuthRepositoryImpl
import com.appcitas.app.data.repository.ChatRepositoryImpl
import com.appcitas.app.data.repository.IcebreakerRepositoryImpl
import com.appcitas.app.data.repository.MatchRepositoryImpl
import com.appcitas.app.data.repository.UserRepositoryImpl
import com.appcitas.app.data.repository.VerificationRepositoryImpl
import com.appcitas.app.domain.repository.AuthRepository
import com.appcitas.app.domain.repository.ChatRepository
import com.appcitas.app.domain.repository.IcebreakerRepository
import com.appcitas.app.domain.repository.MatchRepository
import com.appcitas.app.domain.repository.UserRepository
import com.appcitas.app.domain.repository.VerificationRepository
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindMatchRepository(impl: MatchRepositoryImpl): MatchRepository

    @Binds
    @Singleton
    abstract fun bindVerificationRepository(impl: VerificationRepositoryImpl): VerificationRepository

    @Binds
    @Singleton
    abstract fun bindIcebreakerRepository(impl: IcebreakerRepositoryImpl): IcebreakerRepository
}
