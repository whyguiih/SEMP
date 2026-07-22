package com.example.semp.models;

import com.google.gson.annotations.SerializedName;

public class ProdutoRequest {
    public String nome;
    public String codigo;
    
    @SerializedName("codigo_fisico")
    public String codigo_fisico;
    
    public String descricao;
    public int quant;
    public String uni_natal;
    public String marca_ref;
    public String cor;
    public String descricao_detalhada;
    public String foto;
    public String unidade_atual;

    public ProdutoRequest(String nome, String codigo, String codigo_fisico, String descricao, int quant, String uni_natal, String marca_ref, String cor, String descricao_detalhada, String foto, String unidade_atual) {
        this.nome = nome;
        this.codigo = codigo;
        this.codigo_fisico = codigo_fisico;
        this.descricao = descricao;
        this.quant = quant;
        this.uni_natal = uni_natal;
        this.marca_ref = marca_ref;
        this.cor = cor;
        this.descricao_detalhada = descricao_detalhada;
        this.foto = foto;
        this.unidade_atual = unidade_atual;
    }
}