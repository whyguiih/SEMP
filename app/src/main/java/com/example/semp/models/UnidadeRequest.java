package com.example.semp.models;

import com.google.gson.annotations.SerializedName;

public class UnidadeRequest {
    @SerializedName("nome_unidade")
    public String nomeUnidade;
    
    public String estado;
    public String regiao;
    public String identificacao;

    public UnidadeRequest(String nomeUnidade, String estado, String regiao, String identificacao) {
        this.nomeUnidade = nomeUnidade;
        this.estado = estado;
        this.regiao = regiao;
        this.identificacao = identificacao;
    }
}
