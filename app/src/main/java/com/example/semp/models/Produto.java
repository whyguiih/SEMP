package com.example.semp.models;
import com.google.gson.annotations.SerializedName;

public class Produto {
    public int id_estoque;

    @SerializedName("quantidade")
    public int quantidade;

    public String nome;
    public String codigo;
    public String codigo_fisico;
    public String descricao;
    public String descricao_detalhada;
    public String cor;
    public int quant;
    public String uni_intermediarias;
    public String marca_ref;
    public String uni_natal;
    public int carrinho;
    public String pedido;
    public String foto;

    public int estoque_real;
    public int altura;
    public int comprimento;

    @SerializedName("data_reserva")
    public String periodo_reserva;

    // 👉 ADICIONE ESTAS DUAS LINHAS:
    @SerializedName("uni_atual")
    public String unidade_atual;
}