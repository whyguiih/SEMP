package com.example.continuacao.models; // Confirme se o seu pacote é continuacao ou semp

public class LoginResponse {
    public boolean sucesso;
    public String mensagem;
    public String usuario;      // <-- ESTA É A LINHA QUE ESTAVA EM FALTA!
    public String nivel_conta;
    public String unidade;
}