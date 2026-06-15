package com.example.semp;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MenuSidebarHelper {
    public static void configurarNavegacao(Activity activity, DrawerLayout drawerLayout) {
        String nivel = MainActivity.nivelContaAtual != null ? MainActivity.nivelContaAtual : "0";

        TextView btnEstoque = activity.findViewById(R.id.menuItemEstoque);
        TextView btnCarrinho = activity.findViewById(R.id.menuItemCarrinho);
        TextView btnVisualizarPedido = activity.findViewById(R.id.menuItemVisualizarPedido);
        TextView btnConfigEstoque = activity.findViewById(R.id.menuItemConfigEstoque); // Cadastrar Produto
        TextView btnAutorizar = activity.findViewById(R.id.menuItemAutorizar);
        TextView btnCadastrarUsuario = activity.findViewById(R.id.menuItemConfigAcesso);

        // Esconde tudo primeiro
        if (btnConfigEstoque != null) btnConfigEstoque.setVisibility(View.GONE);
        if (btnAutorizar != null) btnAutorizar.setVisibility(View.GONE);
        if (btnCadastrarUsuario != null) btnCadastrarUsuario.setVisibility(View.GONE);

        // Libera por nível corretamente de acordo com as novas regras corrigidas:
        // Nível 0: Carrinho + Estoque
        // Nível 1: Carrinho + Estoque + Visualizar Pedidos da Unidade
        // Nível 2: Operacional (Tudo exceto Cadastrar Usuário)
        // Nível 3: Administrador (Estoque, Carrinho, Cadastrar Usuário)

        // Esconde itens restritos por padrão
        if (btnVisualizarPedido != null) btnVisualizarPedido.setVisibility(View.GONE);
        if (btnConfigEstoque != null) btnConfigEstoque.setVisibility(View.GONE);
        if (btnAutorizar != null) btnAutorizar.setVisibility(View.GONE);
        if (btnCadastrarUsuario != null) btnCadastrarUsuario.setVisibility(View.GONE);

        // Nível 0: Apenas Estoque e Carrinho (já visíveis por padrão)
        
        // Nível 1: Carrinho + Estoque + Visualizar Pedidos da Unidade + Cadastrar Produto
        if (nivel.equals("1")) {
            if (btnVisualizarPedido != null) btnVisualizarPedido.setVisibility(View.VISIBLE);
            if (btnConfigEstoque != null) btnConfigEstoque.setVisibility(View.VISIBLE);
        }

        // Nível 2: Operacional (Visualizar, Configurar Estoque, Autorizar)
        if (nivel.equals("2")) {
            if (btnVisualizarPedido != null) btnVisualizarPedido.setVisibility(View.VISIBLE);
            if (btnConfigEstoque != null) btnConfigEstoque.setVisibility(View.VISIBLE);
            if (btnAutorizar != null) btnAutorizar.setVisibility(View.VISIBLE);
        }
        
        // Nível 3: Admin (Cadastrar Usuário)
        if (nivel.equals("3")) {
            if (btnCadastrarUsuario != null) btnCadastrarUsuario.setVisibility(View.VISIBLE);
        }

        // Ações de clique garantidas (Sem 'Em breve')
        if (btnEstoque != null) btnEstoque.setOnClickListener(v -> redirecionar(activity, EstoqueActivity.class, drawerLayout));
        if (btnCarrinho != null) btnCarrinho.setOnClickListener(v -> redirecionar(activity, CarrinhoActivity.class, drawerLayout));
        if (btnVisualizarPedido != null) btnVisualizarPedido.setOnClickListener(v -> redirecionar(activity, VisualizarPedidoActivity.class, drawerLayout));
        if (btnConfigEstoque != null) btnConfigEstoque.setOnClickListener(v -> redirecionar(activity, CadastrarProdutoActivity.class, drawerLayout));
        if (btnAutorizar != null) btnAutorizar.setOnClickListener(v -> redirecionar(activity, AutorizarPedidosActivity.class, drawerLayout));
        if (btnCadastrarUsuario != null) btnCadastrarUsuario.setOnClickListener(v -> redirecionar(activity, CadastrarUsuarioActivity.class, drawerLayout));

        // Botão Sair - Desloga suavemente e volta pro Login sem dar "Crash"
        View btnSair = activity.findViewById(R.id.menuItemSair);
        if (btnSair != null) {
            btnSair.setOnClickListener(v -> {
                MainActivity.salvarSessao("", "", "0");
                Intent intent = new Intent(activity, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
            });
        }
    }

    private static void redirecionar(Activity atual, Class<?> destino, DrawerLayout drawerLayout) {
        if (!atual.getClass().equals(destino)) {
            atual.startActivity(new Intent(atual, destino));
            atual.finish(); // Mata a tela anterior para não empilhar
        } else if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
}