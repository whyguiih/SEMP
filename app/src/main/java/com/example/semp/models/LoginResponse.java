package com.example.semp.models;

public class LoginResponse {
    public boolean sucesso;
    public String mensagem;
    public String usuario;
    public String nivel_conta;
    public String unidade;
    
    // Novos campos vindos da unidade do usuário
    public String estado_identificador;
    public String regiao_identificador;
    public Integer unidade_identificador;
}