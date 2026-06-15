package com.example.semp.models;

public class UpdateProdutoRequest {
    public int id;
    public String nome;
    public String codigo;
    public String descricao;
    public Integer quant;
    public String uni_natal;
    public String uni_intermediarias;
    public String cor;
    public String marca_ref;
    public String descricao_detalhada;
    public String foto;

    public UpdateProdutoRequest(int id, String nome, String codigo, String descricao, Integer quant, String uni_natal, String uni_intermediarias, String cor, String marca_ref, String descricao_detalhada, String foto) {
        this.id = id;
        this.nome = nome;
        this.codigo = codigo;
        this.descricao = descricao;
        this.quant = quant;
        this.uni_natal = uni_natal;
        this.uni_intermediarias = uni_intermediarias;
        this.cor = cor;
        this.marca_ref = marca_ref;
        this.descricao_detalhada = descricao_detalhada;
        this.foto = foto;
    }
}