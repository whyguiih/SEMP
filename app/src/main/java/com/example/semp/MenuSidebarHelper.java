package com.example.semp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MenuSidebarHelper {
    public static void configurarNavegacao(Activity activity, DrawerLayout drawerLayout) {

        // RECUPERA DO SHAREDPREFERENCES (Mais seguro que variável global)
        SharedPreferences prefs = activity.getSharedPreferences("SessaoApp", Context.MODE_PRIVATE);
        String nivel = prefs.getString("nivelContaAtual", "0");

        TextView btnEstoque = activity.findViewById(R.id.menuItemEstoque);
        TextView btnCarrinho = activity.findViewById(R.id.menuItemCarrinho);
        TextView btnVisualizarPedido = activity.findViewById(R.id.menuItemVisualizarPedido);
        TextView btnConfigEstoque = activity.findViewById(R.id.menuItemConfigEstoque);
        TextView btnAutorizar = activity.findViewById(R.id.menuItemAutorizar);
        TextView btnCadastrarUsuario = activity.findViewById(R.id.menuItemConfigAcesso);

        // Esconde itens restritos por padrão
        if (btnVisualizarPedido != null) btnVisualizarPedido.setVisibility(View.GONE);
        if (btnConfigEstoque != null) btnConfigEstoque.setVisibility(View.GONE);
        if (btnAutorizar != null) btnAutorizar.setVisibility(View.GONE);
        if (btnCadastrarUsuario != null) btnCadastrarUsuario.setVisibility(View.GONE);

        // Libera as telas por nível de acesso
        if ("1".equals(nivel)) {
            if (btnVisualizarPedido != null) btnVisualizarPedido.setVisibility(View.VISIBLE);
            if (btnConfigEstoque != null) btnConfigEstoque.setVisibility(View.VISIBLE);
        } else if ("2".equals(nivel)) {
            if (btnVisualizarPedido != null) btnVisualizarPedido.setVisibility(View.VISIBLE);
            if (btnConfigEstoque != null) btnConfigEstoque.setVisibility(View.VISIBLE);
            if (btnAutorizar != null) btnAutorizar.setVisibility(View.VISIBLE);
        } else if ("3".equals(nivel)) {
            if (btnCadastrarUsuario != null) btnCadastrarUsuario.setVisibility(View.VISIBLE);
            // Presumindo que o Admin (3) tenha acesso aos mesmos que o Nível 2
            if (btnVisualizarPedido != null) btnVisualizarPedido.setVisibility(View.VISIBLE);
            if (btnConfigEstoque != null) btnConfigEstoque.setVisibility(View.VISIBLE);
            if (btnAutorizar != null) btnAutorizar.setVisibility(View.VISIBLE);
        }

        // Configuração de Cliques
        if (btnEstoque != null) btnEstoque.setOnClickListener(v -> redirecionar(activity, EstoqueActivity.class, drawerLayout));
        if (btnCarrinho != null) btnCarrinho.setOnClickListener(v -> redirecionar(activity, CarrinhoActivity.class, drawerLayout));
        if (btnVisualizarPedido != null) btnVisualizarPedido.setOnClickListener(v -> redirecionar(activity, VisualizarPedidoActivity.class, drawerLayout));
        if (btnConfigEstoque != null) btnConfigEstoque.setOnClickListener(v -> redirecionar(activity, CadastrarProdutoActivity.class, drawerLayout));
        if (btnAutorizar != null) btnAutorizar.setOnClickListener(v -> redirecionar(activity, AutorizarPedidosActivity.class, drawerLayout));
        if (btnCadastrarUsuario != null) btnCadastrarUsuario.setOnClickListener(v -> redirecionar(activity, CadastrarUsuarioActivity.class, drawerLayout));

        // Botão Sair - Limpa o SharedPreferences de forma segura
        View btnSair = activity.findViewById(R.id.menuItemSair);
        if (btnSair != null) {
            btnSair.setOnClickListener(v -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(activity, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
            });
        }
    }

    private static void redirecionar(Activity atual, Class<?> destino, DrawerLayout drawerLayout) {
        if (!atual.getClass().equals(destino)) {
            atual.startActivity(new Intent(atual, destino));
            atual.finish();
        } else if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
}