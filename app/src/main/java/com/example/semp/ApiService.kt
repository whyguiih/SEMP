package com.example.semp

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// --- CLASSES DE LOGIN ---
data class LoginRequest(val usuario: String, val senha: String)

data class LoginResponse(
    val sucesso: Boolean,
    val mensagem: String,
    val usuario: String?,
    val nivel_conta: Int?,
    val unidade: String?
)

// --- CLASSES DE PRODUTO / ESTOQUE ---
data class Produto(
    val id_estoque: Int?,
    val nome: String?,
    val codigo: String?,
    val descricao: String?,
    val quant: String?,
    val carrinho: Int?
)

// --- CLASSES DE PEDIDO ---
data class PedidoRequest(
    val nome: String,
    val email: String,
    val unidade: String,
    val data_reserva: String
)

data class GenericResponse(
    val sucesso: Boolean,
    val mensagem: String?
)

// --- INTERFACE DE ROTAS ---
interface ApiService {
    @POST("/login")
    fun fazerLogin(@Body request: LoginRequest): Call<LoginResponse>

    @GET("/produtos")
    fun getProdutos(): Call<List<Produto>>

    @GET("/carrinho")
    fun getCarrinho(): Call<List<Produto>>

    @POST("/pedido/fazer")
    fun fazerPedido(@Body request: PedidoRequest): Call<GenericResponse>
}

// --- CLIENT RETROFIT ---
object RetrofitClient {
    private const val BASE_URL = "https://api-estoque.whyguiih.workers.dev/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}