package com.appcitas.app.data.repository

import com.appcitas.app.domain.model.IcebreakerTopic
import com.appcitas.app.domain.repository.IcebreakerRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IcebreakerRepositoryImpl @Inject constructor() : IcebreakerRepository {

    // Local cache of icebreaker topics
    private val topics = listOf(
        // Viajes
        IcebreakerTopic("1", "¿Cuál es el lugar más increíble que has visitado?", "Viajes", true),
        IcebreakerTopic("2", "Si pudieras viajar a cualquier lugar del mundo, ¿a dónde irías?", "Viajes", true),
        IcebreakerTopic("3", "¿Cuál es tu próxima destino de viaje soñado?", "Viajes", true),

        // Comida
        IcebreakerTopic("4", "¿Cuál es tu comida favorita?", "Comida", true),
        IcebreakerTopic("5", "¿Sabes cocinar algún platillo especial?", "Comida", true),
        IcebreakerTopic("6", "¿Cuál es el mejor restaurante que has visitado?", "Comida", true),

        // Música
        IcebreakerTopic("7", "¿Cuál es tu género de música favorito?", "Música", true),
        IcebreakerTopic("8", "¿A qué concierto te gustaría ir?", "Música", true),
        IcebreakerTopic("9", "¿Cuál es tu canción favorita en este momento?", "Música", true),

        // Películas
        IcebreakerTopic("10", "¿Cuál es tu película favorita de todos los tiempos?", "Películas", true),
        IcebreakerTopic("11", "¿Qué serie estás viendo ahora?", "Películas", true),
        IcebreakerTopic("12", "¿Cuál es tu género de películas favorito?", "Películas", true),

        // Deportes
        IcebreakerTopic("13", "¿Practicas algún deporte?", "Deportes", true),
        IcebreakerTopic("14", "¿Cuál es tu equipo de fútbol favorito?", "Deportes", true),
        IcebreakerTopic("15", "¿Te gustaría hacer senderismo o escalar montañas?", "Deportes", true),

        // Entretenimiento
        IcebreakerTopic("16", "¿Cuál es tu hobby favorito?", "Entretenimiento", true),
        IcebreakerTopic("17", "¿Qué tipo de libros te gustan?", "Entretenimiento", true),
        IcebreakerTopic("18", "¿Prefieres una noche de película o salir a bailar?", "Entretenimiento", true),

        // Personalidad
        IcebreakerTopic("19", "¿Eres más de madrugada o de nocturno?", "Personalidad", true),
        IcebreakerTopic("20", "¿Cuál es tu mayor sueño en la vida?", "Personalidad", true),
        IcebreakerTopic("21", "¿Cómo te describirían tus amigos?", "Personalidad", true),

        // Relaciones
        IcebreakerTopic("22", "¿Qué valoras más en una persona?", "Relaciones", true),
        IcebreakerTopic("23", "¿Cuál es tu idea perfecta de una primera cita?", "Relaciones", true),
        IcebreakerTopic("24", "¿Buscas algo serio o solo quieres conocer gente nueva?", "Relaciones", true)
    )

    override suspend fun getTopics(category: String?): Result<List<IcebreakerTopic>> {
        return if (category != null) {
            Result.success(topics.filter { it.category == category && it.isActive })
        } else {
            Result.success(topics.filter { it.isActive })
        }
    }

    override suspend fun getRandomTopic(): Result<IcebreakerTopic> {
        val activeTopics = topics.filter { it.isActive }
        return if (activeTopics.isNotEmpty()) {
            Result.success(activeTopics.random())
        } else {
            Result.failure(Exception("No hay temas disponibles"))
        }
    }

    override suspend fun getDailyTopic(): Result<IcebreakerTopic> {
        // Use day of year to get consistent daily topic
        val dayOfYear = java.time.LocalDate.now().dayOfYear
        val index = dayOfYear % topics.size
        return Result.success(topics[index])
    }

    override suspend fun getCategories(): Result<List<String>> {
        return Result.success(topics.map { it.category }.distinct())
    }
}
