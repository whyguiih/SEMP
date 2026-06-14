package com.example.continuacao.models;
import java.util.List;

public class PedidoRequest {
    public String remetente;
    public String email;
    public String unidade;
    public String data_reserva;
    public List<Integer> produtos; // Lista de IDs dos produtos (id_estoque)
    public String prioridade;
    public String motivo;
    public String data_postagem;

    public PedidoRequest(String remetente, String email, String unidade, String data_reserva,
                         List<Integer> produtos, String prioridade, String motivo, String data_postagem) {
        this.remetente = remetente;
        this.email = email;
        this.unidade = unidade;
        this.data_reserva = data_reserva;
        this.produtos = produtos;
        this.prioridade = prioridade;
        this.motivo = motivo;
        this.data_postagem = data_postagem;
    }
}