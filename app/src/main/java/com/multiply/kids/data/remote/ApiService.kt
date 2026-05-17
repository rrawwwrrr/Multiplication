package com.multiply.kids.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class SessionRequest(
    val device_id: String,
    val child_name: String,
    val table_number: Int,
    val score: Int,
    val total: Int,
    val duration_sec: Float,
)

data class SessionResponse(
    val id: Int,
    val device_id: String,
    val child_name: String,
    val table_number: Int,
    val score: Int,
    val total: Int,
    val duration_sec: Float,
    val created_at: String,
)

data class BestScore(
    val score: Int,
    val total: Int,
    val percent: Float,
)

data class StatsResponse(
    val child_name: String,
    val total_sessions: Int,
    val total_correct: Int,
    val total_questions: Int,
    val best_per_table: Map<String, BestScore>,
    val recent_sessions: List<SessionResponse>,
)

interface ApiService {

    @POST("api/sessions")
    suspend fun postSession(@Body session: SessionRequest): SessionResponse

    @GET("api/stats/{deviceId}")
    suspend fun getStats(@Path("deviceId") deviceId: String): StatsResponse
}
