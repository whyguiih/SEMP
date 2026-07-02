package com.example.semp.models;

public class PedidosPendentes {
    public int id_emprestimo;
    public String nome; // É o remetente / solicitante
    public String unidade;
    public String nome_produto;
    public int quant;
    public String prioridade;
    public String motivo;
    public String data_reserva;
    public int aprovacao;

    public int processamento;

    public String codigo_pedido;
    public String codigo_produto;
    public String data_postagem;
}