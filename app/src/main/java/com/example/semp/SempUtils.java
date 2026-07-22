package com.example.semp;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SempUtils {
    public static String gerarCodigoSemp(String nomeUnidade, int tipoEntidade) {
        // ... (método antigo mantido para pedidos, se necessário)
        return "9590" + tipoEntidade + "-OLD";
    }

    public static String gerarCodigoPedidoModerno(String estadoIdent, String regiaoIdent, int imendaIdent) {
        String caracteresPermitidos = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        
        String c1 = (estadoIdent != null && !estadoIdent.isEmpty()) ? estadoIdent.substring(0,1) : "X";
        String c2 = (regiaoIdent != null && !regiaoIdent.isEmpty()) ? regiaoIdent.substring(0,1) : "x";
        char c3 = caracteresPermitidos.charAt(random.nextInt(caracteresPermitidos.length()));
        String c4 = String.valueOf(imendaIdent);
        String c5 = "3"; // SEMPRE 3 PARA PEDIDO
        
        String prefixo = c1 + c2 + c3 + c4 + c5;
        
        StringBuilder parteFinal = new StringBuilder("-");
        for (int i = 0; i < 10; i++) {
            parteFinal.append(caracteresPermitidos.charAt(random.nextInt(caracteresPermitidos.length())));
        }
        
        return prefixo + parteFinal.toString();
    }

    public static String gerarCodigoProdutoModerno(String estadoIdent, String regiaoIdent, int imendaIdent) {
        String caracteresPermitidos = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        
        String c1 = (estadoIdent != null && !estadoIdent.isEmpty()) ? estadoIdent.substring(0,1) : "X";
        String c2 = (regiaoIdent != null && !regiaoIdent.isEmpty()) ? regiaoIdent.substring(0,1) : "x";
        char c3 = caracteresPermitidos.charAt(random.nextInt(caracteresPermitidos.length()));
        String c4 = String.valueOf(imendaIdent);
        String c5 = "2"; // SEMPRE 2 PARA PRODUTO
        
        String prefixo = c1 + c2 + c3 + c4 + c5;
        
        StringBuilder parteFinal = new StringBuilder("-");
        for (int i = 0; i < 10; i++) {
            parteFinal.append(caracteresPermitidos.charAt(random.nextInt(caracteresPermitidos.length())));
        }
        
        return prefixo + parteFinal.toString();
    }
}