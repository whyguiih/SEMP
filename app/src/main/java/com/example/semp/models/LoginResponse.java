package com.example.semp.models; // Confirme se o seu pacote é semp ou semp

public class LoginResponse {
    public boolean sucesso;
    public String mensagem;
    public String usuario;      // <-- ESTA É A LINHA QUE ESTAVA EM FALTA!
    public String nivel_conta;
    public String unidade;
}