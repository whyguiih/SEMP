package com.example.semp;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SempUtils {
    public static String gerarCodigoSemp(String nomeUnidade, int tipoEntidade) {
        String unidadeFormatada = nomeUnidade != null ? nomeUnidade.trim().toLowerCase() : "default";

        Map<String, String> mapaUnidades = new HashMap<>();
        mapaUnidades.put("garibaldi", "9511");
        mapaUnidades.put("farroupilha", "9521");
        mapaUnidades.put("encantado", "9531");
        mapaUnidades.put("ceit", "9510");
        mapaUnidades.put("galvanotek", "9520");

        String prefixoBase = mapaUnidades.containsKey(unidadeFormatada) ? mapaUnidades.get(unidadeFormatada) : "9590";
        String prefixo = prefixoBase + tipoEntidade; // 2 para Produto, 3 para Pedido

        String caracteresPermitidos = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder parteAleatoria = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            parteAleatoria.append(caracteresPermitidos.charAt(random.nextInt(caracteresPermitidos.length())));
        }

        return prefixo + "-" + parteAleatoria.toString();
    }
}