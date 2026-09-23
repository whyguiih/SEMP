package com.example.semp.models;

public class AutorizarRequest {
    public int id_emprestimo;
    public int novoStatus;

    public AutorizarRequest(int id_emprestimo, int novoStatus) {
        this.id_emprestimo = id_emprestimo;
        this.novoStatus = novoStatus;
    }
}