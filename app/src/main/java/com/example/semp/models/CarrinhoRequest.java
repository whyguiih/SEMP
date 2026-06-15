package com.example.semp.models;

public class CarrinhoRequest {
    public String nome_produto; // TEM que ter esse nome
    public int quantidade;

    public CarrinhoRequest(String nome_produto, int quantidade) {
        this.nome_produto = nome_produto;
        this.quantidade = quantidade;
    }
}