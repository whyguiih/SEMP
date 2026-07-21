package com.example.semp.models;

import com.google.gson.annotations.SerializedName;

public class PedidosPendentes {
    public int id_emprestimo;

    @SerializedName(value = "destinatario", alternate = {"nome", "remetente", "solicitante"})
    public String nome; // É o remetente / solicitante / destinatário

    @SerializedName(value = "unidade_natal", alternate = {"unidade", "unidade_destino", "unidade_produto", "unidade_solicitante"})
    public String unidade;

    @SerializedName("nome_produto")
    public String nome_produto;

    @SerializedName(value = "quant", alternate = {"quantidade", "quantidade_produto"})
    public int quant;

    public String prioridade;
    public String motivo;

    @SerializedName(value = "data_reserva", alternate = {"periodo_reserva", "reserva"})
    public String periodo_reserva;

    public int aprovacao;

    public int processamento;

    public String codigo_pedido;
    public String codigo_produto;
    public String data_postagem;

    @SerializedName(value = "data_devolucao", alternate = {"data_retorno", "devolucao", "retorno"})
    public String data_devolucao;
}