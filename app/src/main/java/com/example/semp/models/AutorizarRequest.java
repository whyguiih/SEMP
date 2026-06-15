package com.example.semp.models;

public class AutorizarRequest {
    public int id_emprestimo;
    public int novoStatus;

    // Construtor que aceita os dois argumentos
    public AutorizarRequest(int id_emprestimo, int novoStatus) {
        this.id_emprestimo = id_emprestimo;
        this.novoStatus = novoStatus;
    }
}