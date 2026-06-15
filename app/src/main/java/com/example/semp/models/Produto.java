package com.example.semp.models;
import com.google.gson.annotations.SerializedName;

public class Produto {
    public int id_estoque;
    
    @SerializedName("quantidade")
    public int quantidade; 

    public String nome;
    public String codigo;
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
}