package com.example.semp

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val usuario: String, val senha: String)

data class LoginResponse(
    val sucesso: Boolean,
    val mensagem: String,
    val usuario: String?,
    val nivel_conta: Int?,
    val unidade: String?
)

interface ApiService {
    @POST("/login")
    fun fazerLogin(@Body request: LoginRequest): Call<LoginResponse>
}

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.33:3000/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}