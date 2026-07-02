package com.example.semp.models;
import java.util.List;

public class PedidoRequest {
    public String remetente;
    public String email;
    public String unidade;
    public String data_reserva;
    public String prioridade;
    public String motivo;
    public String data_postagem;
    public String codigo_pedido;
    public List<ProdutoPedido> produtos;

    public static class ProdutoPedido {
        public String codigo_produto;
        public String nome_produto;
        public int quantidade_produto;
        public String unidade_produto;
        public String pedido_produto;
        // Adicione os campos vazios que a API espera
        public String descricao_produto = "";
        public String descricao_detalhada_produto = "";
        public String cor_produto = "";
        public String marca_produto = "";

        public ProdutoPedido(String codigo_produto, String nome_produto, int quantidade_produto, String unidade_produto, String pedido_produto) {
            this.codigo_produto = codigo_produto;
            this.nome_produto = nome_produto;
            this.quantidade_produto = quantidade_produto;
            this.unidade_produto = unidade_produto;
            this.pedido_produto = pedido_produto;
        }
    }

    public PedidoRequest(String remetente, String email, String unidade, String data_reserva, List<ProdutoPedido> produtos, String prioridade, String motivo, String data_postagem, String codigo_pedido) {
        this.remetente = remetente; this.email = email; this.unidade = unidade;
        this.data_reserva = data_reserva; this.produtos = produtos; this.prioridade = prioridade;
        this.motivo = motivo; this.data_postagem = data_postagem; this.codigo_pedido = codigo_pedido;
    }
}