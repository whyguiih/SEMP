package com.example.semp

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginRequest(val usuario: String, val senha: String)
data class LoginResponse(val sucesso: Boolean, val mensagem: String, val usuario: String?, val nivel_conta: String?, val unidade: String?)
data class Produto(val id_estoque: Int?, val nome: String?, val codigo: String?, val descricao: String?, val quant: String?, val carrinho: Int?)
data class PedidoRequest(val nome: String, val email: String, val unidade: String, val data_reserva: String)
data class GenericResponse(val sucesso: Boolean, val mensagem: String?)

data class UsuarioRequest(val usuario: String, val senha: String, val nivel_conta: String, val unidade: String)
data class UpdateProdutoRequest(val id: Int, val coluna: String, val valor: String)
data class DeleteProdutoRequest(val codigo: String)
data class PedidoPendente(val id_emprestimo: Int, val nome: String, val unidade_natal: String, val data_reserva: String)
data class AutorizarRequest(val id_emprestimo: Int, val novoStatus: Int)

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

    // CORREÇÃO: Transforma o envio em Form-Data nativo do PHP!
    @FormUrlEncoded
    @POST("adicionar_carrinho.php")
    fun adicionarAoCarrinho(
        @Field("id_produto") id_produto: String,
        @Field("quantidade") quantidade: Int
    ): Call<GenericResponse>
}

object RetrofitClient {
    private const val BASE_URL = "https://api-estoque.whyguiih.workers.dev/"
    val api: ApiService by lazy { Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(ApiService::class.java) }
}