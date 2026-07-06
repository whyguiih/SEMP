package com.example.semp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.semp.models.AutorizarRequest;
import com.example.semp.models.GenericResponse;
import com.example.semp.models.PedidosPendentes;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AutorizarPedidosActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView rvPendentes, rvConfirmados, rvRetornos;
    private PedidoAdapter adapterPendentes, adapterConfirmados, adapterRetornos;
    private TextView tvTituloRetornos;
    private SharedPreferences prefsOcultos;
    private String usuarioAtual = "";
    private EditText etPesquisa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autorizar_pedidos);

        drawerLayout = findViewById(R.id.drawerLayoutAutorizar);
        rvPendentes = findViewById(R.id.rvPendentes);
        rvConfirmados = findViewById(R.id.rvConfirmados);
        rvRetornos = findViewById(R.id.rvRetornos);
        tvTituloRetornos = findViewById(R.id.tvTituloRetornos);

        // Pega o usuário logado para saber de quem são os pedidos ocultos
        SharedPreferences prefsSessao = getSharedPreferences("SessaoApp", Context.MODE_PRIVATE);
        usuarioAtual = prefsSessao.getString("usuarioLogado", MainActivity.usuarioLogado);
        prefsOcultos = getSharedPreferences("PedidosOcultos", MODE_PRIVATE);

        View mainView = findViewById(R.id.mainContentLayout);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        ImageView btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
            });
        }

        configurarNavegacaoMenu();

        if (rvPendentes != null) rvPendentes.setLayoutManager(new LinearLayoutManager(this));
        if (rvConfirmados != null) rvConfirmados.setLayoutManager(new LinearLayoutManager(this));
        if (rvRetornos != null) rvRetornos.setLayoutManager(new LinearLayoutManager(this));

        etPesquisa = findViewById(R.id.etPesquisa);
        if (etPesquisa != null) {
            etPesquisa.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    String texto = s.toString();
                    if (adapterPendentes != null) adapterPendentes.filtrar(texto);
                    if (adapterConfirmados != null) adapterConfirmados.filtrar(texto);
                    if (adapterRetornos != null) adapterRetornos.filtrar(texto);
                }
            });
        }

        buscarPedidosPendentes();
    }

    private void buscarPedidosPendentes() {
        SharedPreferences prefsSessao = getSharedPreferences("SessaoApp", Context.MODE_PRIVATE);
        String usuarioAtual = prefsSessao.getString("usuarioLogado", "");
        String unidadeAtual = prefsSessao.getString("unidadeAtual", "");

        // Retornando para getPedidosPendentes que sabemos que não dá 404
        RetrofitClient.getApi().getPedidosPendentes().enqueue(new Callback<List<PedidosPendentes>>() {
            @Override
            public void onResponse(Call<List<PedidosPendentes>> call, Response<List<PedidosPendentes>> response) {
                if (isDestroyed() || isFinishing()) return;

                if (!response.isSuccessful()) {
                    Toast.makeText(AutorizarPedidosActivity.this, "Erro API: " + response.code(), Toast.LENGTH_LONG).show();
                    return;
                }

                List<PedidosPendentes> todosPedidos = response.body();
                if (todosPedidos != null) {
                    List<PedidosPendentes> pendentes = new ArrayList<>();
                    List<PedidosPendentes> confirmados = new ArrayList<>();
                    List<PedidosPendentes> retornos = new ArrayList<>();

                    Set<String> ocultos = prefsOcultos.getStringSet("ocultos_" + AutorizarPedidosActivity.this.usuarioAtual, new HashSet<>());

                    for (PedidosPendentes p : todosPedidos) {
                        // Filtro de unidade (opcional, mas bom para garantir)
                        String unidadePedido = p.unidade != null ? p.unidade : "";
                        if (!unidadePedido.equalsIgnoreCase(unidadeAtual) && !unidadeAtual.isEmpty()) {
                             // Se quiser ver apenas da sua unidade, descomente a linha abaixo ou mantenha como está para ver todos
                             // continue; 
                        }

                        // Conforme o Worker: aprovacao == 3 significa retorno solicitado
                        if (p.aprovacao == 3) {
                            retornos.add(p);
                        } else if (p.aprovacao == 0) {
                            pendentes.add(p);
                        } else if (p.aprovacao == 1) {
                            if (!ocultos.contains(String.valueOf(p.id_emprestimo))) {
                                confirmados.add(p);
                            }
                        }
                    }

                    Collections.sort(pendentes, (p1, p2) -> Integer.compare(obterPesoPrioridade(p1.prioridade), obterPesoPrioridade(p2.prioridade)));

                    if (rvRetornos != null) {
                        if (!retornos.isEmpty()) {
                            tvTituloRetornos.setVisibility(View.VISIBLE);
                            rvRetornos.setVisibility(View.VISIBLE);
                            adapterRetornos = new PedidoAdapter(retornos, 2, (pedido, acao) -> {
                                if (acao == 3) processarAutorizacao(pedido.id_emprestimo, 4); 
                            });
                            rvRetornos.setAdapter(adapterRetornos);
                        } else {
                            tvTituloRetornos.setVisibility(View.GONE);
                            rvRetornos.setVisibility(View.GONE);
                        }
                    }

                    if (rvPendentes != null) {
                        adapterPendentes = new PedidoAdapter(pendentes, 0, (pedido, novoStatus) -> processarAutorizacao(pedido.id_emprestimo, novoStatus));
                        rvPendentes.setAdapter(adapterPendentes);
                    }
                    if (rvConfirmados != null) {
                        adapterConfirmados = new PedidoAdapter(confirmados, 1, (pedido, acao) -> {
                            if (acao == 99) ocultarPedidoDaTela(pedido.id_emprestimo);
                        });
                        rvConfirmados.setAdapter(adapterConfirmados);
                    }
                    
                    if (etPesquisa != null && !etPesquisa.getText().toString().isEmpty()) {
                        String texto = etPesquisa.getText().toString();
                        if (adapterPendentes != null) adapterPendentes.filtrar(texto);
                        if (adapterConfirmados != null) adapterConfirmados.filtrar(texto);
                        if (adapterRetornos != null) adapterRetornos.filtrar(texto);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<PedidosPendentes>> call, Throwable t) {
                if (!isDestroyed() && !isFinishing()) {
                    Toast.makeText(AutorizarPedidosActivity.this, "Falha de Leitura", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void mostrarAlertaWeb(View view, String mensagem, String corHexa) {
        try {
            Snackbar snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor(corHexa));

            android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) snackbarView.getLayoutParams();
            params.gravity = android.view.Gravity.TOP;
            params.topMargin = 120;
            snackbarView.setLayoutParams(params);

            TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(16);
            textView.setMaxLines(3);
            snackbar.show();
        } catch (Exception e) {
            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
        }
    }

    private int obterPesoPrioridade(String prioridade) {
        if (prioridade == null) return 4;
        switch (prioridade.toLowerCase().trim()) {
            case "alto": return 1;
            case "intermediário":
            case "intermediario":
            case "médio":
            case "medio": return 2;
            case "baixo": return 3;
            default: return 4;
        }
    }

    private void processarAutorizacao(int idEmprestimo, int novoStatus) {
        AutorizarRequest request = new AutorizarRequest(idEmprestimo, novoStatus);
        RetrofitClient.getApi().autorizarPedido(request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                String acao;
                if (novoStatus == 1) acao = "aprovado";
                else if (novoStatus == 2) acao = "recusado";
                else if (novoStatus == 4) acao = "de retorno confirmado";
                else acao = "processado";

                mostrarAlertaWeb(findViewById(android.R.id.content), "Pedido " + acao + " com sucesso!", "#1a4b9f");
                buscarPedidosPendentes();
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(AutorizarPedidosActivity.this, "Erro ao processar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ocultarPedidoDaTela(int idEmprestimo) {
        Set<String> ocultos = new HashSet<>(prefsOcultos.getStringSet("ocultos_" + usuarioAtual, new HashSet<>()));
        ocultos.add(String.valueOf(idEmprestimo));
        prefsOcultos.edit().putStringSet("ocultos_" + usuarioAtual, ocultos).apply();

        mostrarAlertaWeb(findViewById(android.R.id.content), "Pedido removido da sua tela de visualização.", "#555555");
        buscarPedidosPendentes();
    }

    private void configurarNavegacaoMenu() {
        MenuSidebarHelper.configurarNavegacao(this, drawerLayout);
    }
}