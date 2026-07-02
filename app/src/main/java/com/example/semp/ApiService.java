package com.example.semp;

import retrofit2.http.Header;
import com.example.semp.models.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("login")
    Call<LoginResponse> fazerLogin(@Body LoginRequest request);

    @GET("produtos")
    Call<List<Produto>> getProdutos();

    @POST("pedidos/solicitar")
    Call<GenericResponse> fazerPedido(@Body PedidoRequest request);

    @GET("pedidos")
    Call<List<PedidosPendentes>> getMeusPedidos(
            @Query("usuario") String usuario,
            @Query("nivel") String nivel,
            @Query("unidade") String unidade
    );

    @GET("pedidos/pendentes")
    Call<List<PedidosPendentes>> getPedidosPendentes();

    @GET("pedidos/todos")
    Call<List<PedidosPendentes>> getTodosPedidos(
            @Query("unidade") String unidade,
            @Query("nivel") String nivel,
            @Query("usuario") String usuario
    );

    @POST("pedidos/autorizar")
    Call<GenericResponse> autorizarPedido(@Body AutorizarRequest request);

    @POST("usuario/cadastrar")
    Call<GenericResponse> cadastrarUsuario(@Body UsuarioRequest request);

    @POST("produto/atualizar")
    Call<GenericResponse> atualizarProduto(@Body UpdateProdutoRequest request);

    @POST("produto/deletar")
    Call<GenericResponse> deletarProduto(@Body DeleteProdutoRequest request);

    @POST("produto/cadastrar")
    Call<GenericResponse> cadastrarProduto(@Body ProdutoRequest request);

    @POST("carrinho/adicionar")
    Call<GenericResponse> adicionarAoCarrinho(
            @Header("X-Usuario-ID") String usuario,
            @Body CarrinhoRequest request
    );

    @GET("carrinho")
    Call<List<Produto>> getCarrinho(
            @Header("X-Usuario-ID") String usuario
    );

    @POST("carrinho/remover")
    Call<GenericResponse> removerDoCarrinho(
            @Header("X-Usuario-ID") String usuario,
            @Body CarrinhoRequest request
    );


    // Para a tela de Rastreio
    @POST("pedido/rastreio")
    Call<GenericResponse> registrarRastreio(@Body RastreioRequest request);

    @GET("rastreio/todos")
    Call<List<Rastreio>> getTodosRastreios();

    // Para a tela de Itens Emprestados
    @GET("pedidos/emprestados")
    Call<List<PedidosPendentes>> getItensEmprestados(@Query("unidade") String unidade);

    @POST("pedidos/solicitar_retorno")
    Call<GenericResponse> solicitarRetorno(@Body RetornoRequest request);


}