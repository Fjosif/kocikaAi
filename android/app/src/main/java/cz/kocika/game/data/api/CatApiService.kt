package cz.kocika.game.data.api

import retrofit2.http.Body
import retrofit2.http.POST

data class CatStatusRequest(
    val hunger: Int,
    val energy: Int,
    val hygiene: Int,
    val mood: Int,
    val health: Int,
    val lastAction: String
)

data class CatResponse(
    val message: String
)

data class StoryRequest(
    val mood: Int,
    val style: String
)

data class StoryResponse(
    val storyText: String
)

interface CatApiService {
    @POST("cat/respond")
    suspend fun getCatResponse(@Body request: CatStatusRequest): CatResponse

    @POST("story/generate")
    suspend fun generateStory(@Body request: StoryRequest): StoryResponse
}
