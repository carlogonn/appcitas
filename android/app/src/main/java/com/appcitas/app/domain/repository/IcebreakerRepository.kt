package com.appcitas.app.domain.repository

import com.appcitas.app.domain.model.IcebreakerTopic

interface IcebreakerRepository {
    suspend fun getTopics(category: String? = null): Result<List<IcebreakerTopic>>
    suspend fun getRandomTopic(): Result<IcebreakerTopic>
    suspend fun getDailyTopic(): Result<IcebreakerTopic>
    suspend fun getCategories(): Result<List<String>>
}
