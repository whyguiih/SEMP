package com.example.continuacao.models;

public class UsuarioRequest {
    public String usuario;
    public String senha;
    public int nivel_conta;
    public String unidade;

    // Construtor 1: Para quando o código envia um número (int)
    public UsuarioRequest(String usuario, String senha, int nivel_conta, String unidade) {
        this.usuario = usuario;
        this.senha = senha;
        this.nivel_conta = nivel_conta;
        this.unidade = unidade;
    }

    // Construtor 2: Para quando o código envia um texto (String) - CORRIGE O SEU ERRO!
    public UsuarioRequest(String usuario, String senha, String nivel_conta_str, String unidade) {
        this.usuario = usuario;
        this.senha = senha;
        this.unidade = unidade;

        // Tenta converter o texto num número de forma segura
        try {
            this.nivel_conta = Integer.parseInt(nivel_conta_str);
        } catch (NumberFormatException e) {
            this.nivel_conta = 0; // Se houver algum erro ou vier vazio, assume nível 0 (comum)
        }
    }
}