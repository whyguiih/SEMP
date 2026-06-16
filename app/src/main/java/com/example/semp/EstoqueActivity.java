package com.example.semp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semp.models.PedidosPendentes;
import com.example.semp.models.Produto;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstoqueActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private List<Produto> listaOriginalProdutos = new ArrayList<>();
    private RecyclerView recyclerViewEstoque;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estoque);

        drawerLayout = findViewById(R.id.drawerLayout);
        ImageView btnMenu = findViewById(R.id.btnMenu);
        EditText etPesquisa = findViewById(R.id.etPesquisa);
        recyclerViewEstoque = findViewById(R.id.recyclerViewEstoque);

        View mainView = findViewById(R.id.mainContentLayout);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START);
                    else drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        configurarNavegacaoMenu();

        if (recyclerViewEstoque != null) {
            recyclerViewEstoque.setLayoutManager(new LinearLayoutManager(this));
            buscarProdutosAPI();
        }

        if (etPesquisa != null) {
            etPesquisa.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    filtrarLista(s.toString().trim());
                }
            });
        }

        // ==========================================
        // CHAMA A VERIFICAÇÃO DE NOTIFICAÇÕES AQUI
        // ==========================================
        verificarNotificacoes();
    }

    // ==========================================
    // MÉTODOS DE NOTIFICAÇÃO E ALERTA
    // ==========================================
    // 1. MÉTODO PARA O ALERTA ESTILO WEB (AGORA NO TOPO DA TELA)
    // 1. MÉTODO DE ALERTA CORRIGIDO
    // =========================================================
    // 1. ALERTA NO TOPO DA TELA (COM FALLBACK PARA TOAST)
    // =========================================================
    private void mostrarAlertaWeb(String mensagem, String corHexa) {
        try {
            View rootView = findViewById(android.R.id.content);
            com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar.make(rootView, mensagem, com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(android.graphics.Color.parseColor(corHexa));

            android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) snackbarView.getLayoutParams();
            params.gravity = android.view.Gravity.TOP;
            params.topMargin = 120;
            snackbarView.setLayoutParams(params);

            TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setTextColor(android.graphics.Color.WHITE);
            textView.setTextSize(16);
            textView.setMaxLines(3);
            snackbar.show();
        } catch (Exception e) {
            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show(); // Fallback se a tela bugar
        }
    }

    // =========================================================
    // 2. VERIFICAÇÃO DE NOTIFICAÇÕES (LÓGICA CORRIGIDA POR ID)
    // =========================================================
    private void verificarNotificacoes() {
        SharedPreferences prefsSessao = getSharedPreferences("SessaoApp", Context.MODE_PRIVATE);
        String usuarioAtual = prefsSessao.getString("usuarioLogado", "");

        // Leitura à prova de falhas do nível
        int nivelConta = 0;
        try {
            nivelConta = Integer.parseInt(prefsSessao.getString("nivelContaAtual", "0"));
        } catch (Exception e) {
            nivelConta = prefsSessao.getInt("nivelContaAtual", 0);
        }
        final int nivelFinal = nivelConta;

        // Usa a rota que sabemos com 100% de certeza que funciona no seu banco
        RetrofitClient.getApi().getPedidosPendentes().enqueue(new Callback<List<PedidosPendentes>>() {
            @Override
            public void onResponse(Call<List<PedidosPendentes>> call, Response<List<PedidosPendentes>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PedidosPendentes> lista = response.body();
                    SharedPreferences prefs = getSharedPreferences("NotificacoesApp", MODE_PRIVATE);

                    // ========================================================
                    // LÓGICA PARA OS ADMINISTRADORES (NÍVEL 1 E 2)
                    // ========================================================
                    if (nivelFinal == 1 || nivelFinal == 2) {
                        // Puxa qual foi o MAIOR ID de pedido que esse admin já viu na vida dele
                        int ultimoIdVisto = prefs.getInt("ultimo_id_visto_" + usuarioAtual, 0);
                        int novos = 0;
                        int maiorIdDaLista = ultimoIdVisto;

                        for (PedidosPendentes p : lista) {
                            // Se o ID desse pedido for MAIOR que o último visto, é um pedido novo de verdade!
                            if (p.id_emprestimo > ultimoIdVisto) {
                                novos++;
                            }
                            // Descobre qual é o maior ID da lista agora para salvar na memória
                            if (p.id_emprestimo > maiorIdDaLista) {
                                maiorIdDaLista = p.id_emprestimo;
                            }
                        }

                        // Se encontrou pedidos com ID novo, dispara o alerta!
                        if (novos > 0) {
                            String msg = (novos == 1) ? "Você tem 1 novo pedido aguardando autorização!" : "Você tem " + novos + " novos pedidos aguardando autorização!";
                            mostrarAlertaWeb(msg, "#e06c00");

                            // Atualiza a memória com o novo MAIOR ID, assim não repete
                            prefs.edit().putInt("ultimo_id_visto_" + usuarioAtual, maiorIdDaLista).apply();
                        }
                    }

                    // ========================================================
                    // LÓGICA PARA O USUÁRIO COMUM (NÍVEL 0)
                    // ========================================================
                    if (nivelFinal == 0) {
                        SharedPreferences.Editor editor = prefs.edit();
                        boolean houveMudanca = false;

                        for (PedidosPendentes pedido : lista) {
                            if (pedido.nome != null && pedido.nome.equals(usuarioAtual)) {
                                String id = String.valueOf(pedido.id_emprestimo);
                                int statusAtual = pedido.aprovacao;

                                if (statusAtual == 1 || statusAtual == 2) {
                                    int statusSalvo = prefs.getInt("status_" + id, 0);
                                    if (statusSalvo != statusAtual) {
                                        if (statusAtual == 1) {
                                            mostrarAlertaWeb("🎉 Seu pedido de '" + pedido.nome_produto + "' foi APROVADO!", "#27ae60");
                                        } else {
                                            mostrarAlertaWeb("❌ Seu pedido de '" + pedido.nome_produto + "' foi RECUSADO.", "#e74c3c");
                                        }
                                        editor.putInt("status_" + id, statusAtual);
                                        houveMudanca = true;
                                    }
                                }
                            }
                        }
                        if (houveMudanca) editor.apply();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<PedidosPendentes>> call, Throwable t) {
                // Fica silencioso para não incomodar o usuário se a internet piscar
            }
        });
    }
    // ==========================================
    // MÉTODOS ORIGINAIS DA TELA
    // ==========================================
    private void filtrarLista(String textoPequisa) {
        if (listaOriginalProdutos == null) return;

        String termo = textoPequisa.toLowerCase();
        List<Produto> filtrados = new ArrayList<>();

        for (Produto p : listaOriginalProdutos) {
            if ((p.nome != null && p.nome.toLowerCase().contains(termo)) ||
                    (p.codigo != null && p.codigo.toLowerCase().contains(termo))) {
                filtrados.add(p);
            }
        }

        if (recyclerViewEstoque != null) {
            recyclerViewEstoque.setAdapter(new EstoqueAdapter(filtrados, EstoqueActivity.this));
        }
    }

    private void buscarProdutosAPI() {
        RetrofitClient.getApi().getProdutos().enqueue(new Callback<List<Produto>>() {
            @Override
            public void onResponse(Call<List<Produto>> call, Response<List<Produto>> response) {
                if (isDestroyed() || isFinishing()) return;

                if (response.isSuccessful() && response.body() != null) {
                    listaOriginalProdutos = response.body();
                    recyclerViewEstoque.setAdapter(new EstoqueAdapter(listaOriginalProdutos, EstoqueActivity.this));
                } else {
                    Toast.makeText(EstoqueActivity.this, "Nenhum produto encontrado.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Produto>> call, Throwable t) {
                if (!isDestroyed() && !isFinishing()) {
                    Toast.makeText(EstoqueActivity.this, "Erro de rede ao buscar produtos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void configurarNavegacaoMenu() {
        MenuSidebarHelper.configurarNavegacao(this, drawerLayout);
    }
}