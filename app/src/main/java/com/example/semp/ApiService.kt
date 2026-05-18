package com.example.semp

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// 1. O que vamos enviar para a API (usuário e senha)
data class LoginRequest(val usuario: String, val senha: String)

// 2. O que a API vai nos devolver (a resposta em JSON)
data class LoginResponse(
    val sucesso: Boolean,
    val mensagem: String,
    val usuario: String?,
    val nivel_conta: Int?,
    val unidade: String?
)

// 3. Onde definimos as rotas
interface ApiService {
    @POST("/login")
    fun fazerLogin(@Body request: LoginRequest): Call<LoginResponse>
}

// 4. O cliente que gerencia a conexão
object RetrofitClient {
    // Mude para o seu IP. Como estava 192.168.0.117 no seu código, coloquei o mesmo aqui.
    // Lembre-se de colocar a porta :3000 no final
    private const val BASE_URL = "http://192.168.0.131:3000/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}