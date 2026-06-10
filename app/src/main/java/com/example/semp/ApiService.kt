package com.example.semp

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginRequest(val usuario: String, val senha: String)
data class LoginResponse(val sucesso: Boolean, val mensagem: String, val usuario: String?, val nivel_conta: String?, val unidade: String?)
data class Produto(val id_estoque: Int?, val nome: String?, val codigo: String?, val descricao: String?, val quant: String?, val carrinho: Int?)
data class PedidoRequest(val nome: String, val email: String, val unidade: String, val data_reserva: String)
data class GenericResponse(val sucesso: Boolean, val mensagem: String?)

// CLASSES DE DADOS
data class UsuarioRequest(val usuario: String, val senha: String, val nivel_conta: String, val unidade: String)
data class UpdateProdutoRequest(val id: Int, val coluna: String, val valor: String)
data class DeleteProdutoRequest(val codigo: String)
data class PedidoPendente(val id_emprestimo: Int, val nome: String, val unidade_natal: String, val data_reserva: String)
data class AutorizarRequest(val id_emprestimo: Int, val novoStatus: Int)

// CLASSE DO CARRINHO (QUE ESTAVA FALTANDO)
data class CarrinhoRequest(val id_produto: String, val quantidade: Int)

interface ApiService {
    @POST("/login") fun fazerLogin(@Body request: LoginRequest): Call<LoginResponse>
    @GET("/produtos") fun getProdutos(): Call<List<Produto>>
    @GET("/carrinho") fun getCarrinho(): Call<List<Produto>>
    @POST("/pedido/fazer") fun fazerPedido(@Body request: PedidoRequest): Call<GenericResponse>

    @POST("/usuario/cadastrar") fun cadastrarUsuario(@Body request: UsuarioRequest): Call<GenericResponse>
    @POST("/produto/atualizar") fun atualizarProduto(@Body request: UpdateProdutoRequest): Call<GenericResponse>
    @POST("/produto/deletar") fun deletarProduto(@Body request: DeleteProdutoRequest): Call<GenericResponse>

    @GET("/pedidos/pendentes") fun getPedidosPendentes(): Call<List<PedidoPendente>>
    @POST("/pedidos/autorizar") fun autorizarPedido(@Body request: AutorizarRequest): Call<GenericResponse>

    // ROTA DO CARRINHO (QUE ESTAVA FALTANDO)
    @POST("adicionar_carrinho.php")
    fun adicionarAoCarrinho(@Body request: CarrinhoRequest): Call<GenericResponse>
}

object RetrofitClient {
    private const val BASE_URL = "https://api-estoque.whyguiih.workers.dev/"
    val api: ApiService by lazy { Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(ApiService::class.java) }
}