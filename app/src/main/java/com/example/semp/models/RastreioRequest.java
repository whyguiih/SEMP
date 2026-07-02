package com.example.semp.models;

public class RastreioRequest {
    public String codigo;
    public String unidade_original;
    public String unidade_destino;
    public String data_saida;
    public String data_entrada;

    public RastreioRequest(String codigo, String unidade_original, String unidade_destino, String data_saida, String data_entrada) {
        this.codigo = codigo;
        this.unidade_original = unidade_original;
        this.unidade_destino = unidade_destino;
        this.data_saida = data_saida;
        this.data_entrada = data_entrada;
    }
}
