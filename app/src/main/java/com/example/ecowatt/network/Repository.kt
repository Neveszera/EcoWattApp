package com.example.ecowatt.network

class AuthRepository(private val api: ApiService) {
    suspend fun registerUser(request: RegisterRequest) = api.registerUser(request)
    suspend fun loginUser(request: LoginRequest) = api.loginUser(request)
}
