package com.example.ecowatt.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class RegisterRequest(
    val login: String,
    val senha: String,
    val nomeCompleto: String,
    val senhaConfirmacao: String
)

data class LoginRequest(
    val login: String,
    val senha: String
)

data class LoginResponse(
    val token: String,
    val idUsuario: Int,
    val login: String
)

data class SensorRequest(
    val tipoSensor: String,
    val status: String,
    val nomeSensor: String,
    val produtoConectado: String,
    val descricao: String?,
    val localizacao: String?,
    val usuarioId: Int
)

data class SensorResponse(
    val id: Int,
    val nomeSensor: String,
    val tipoSensor: String,
    val status: String,
    val produtoConectado: String,
    val localizacao: String,
    val descricao: String
)

data class SensorResponsePage(
    val content: List<SensorResponse>,
    val totalPages: Int,
    val totalElements: Int,
    val number: Int,
    val size: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)

interface ApiService {
    @POST("/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<String>

    @POST("/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/sensores")
    suspend fun registerSensor(@Body request: SensorRequest): Response<SensorResponse>

    @GET("sensores/{id}")
    suspend fun getSensorById(@Path("id") userId: Int): Response<SensorResponse>
    @GET("sensores/all/{usuarioId}")
    suspend fun getSensorsByUserId(@Path("usuarioId") userId: Int): Response<SensorResponsePage>

    @PUT("/sensores/{id}")
    suspend fun updateSensor(@Path("id") id: Int, @Body sensorRequest: SensorRequest): Response<SensorResponse>

    @DELETE("/sensores/{id}")
    suspend fun deleteSensor(@Path("id") id: Int): Response<Unit>

}
