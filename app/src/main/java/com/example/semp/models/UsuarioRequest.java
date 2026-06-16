package com.example.semp.models;

public class UsuarioRequest {
    public String usuario;
    public String senha;
    public int nivel_conta;
    public String unidade;
    public String foto; // 👉 NOVA VARIÁVEL

    public UsuarioRequest(String usuario, String senha, int nivel_conta, String unidade, String foto) {
        this.usuario = usuario;
        this.senha = senha;
        this.nivel_conta = nivel_conta;
        this.unidade = unidade;
        this.foto = foto;
    }

    public UsuarioRequest(String usuario, String senha, String nivel_conta_str, String unidade, String foto) {
        this.usuario = usuario;
        this.senha = senha;
        this.unidade = unidade;
        this.foto = foto;

        try {
            this.nivel_conta = Integer.parseInt(nivel_conta_str);
        } catch (NumberFormatException e) {
            this.nivel_conta = 0;
        }
    }
}