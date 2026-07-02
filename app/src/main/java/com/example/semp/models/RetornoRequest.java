package com.example.semp.models;

public class RetornoRequest {
    public int id_emprestimo;
    public String data_retorno;

    public RetornoRequest(int id_emprestimo, String data_retorno) {
        this.id_emprestimo = id_emprestimo;
        this.data_retorno = data_retorno;
    }
}
