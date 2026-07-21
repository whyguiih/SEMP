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
import androidx.core.view.GravityCompat;
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
    private View layoutMenuPastas, layoutSubPasta;
    private RecyclerView rvPasta;
    private PedidoAdapter adapterAtual;
    private TextView tvTituloPasta;
    private EditText etPesquisaPasta;
    private SharedPreferences prefsOcultos;
    private String usuarioAtual = "";
    
    private List<PedidosPendentes> listaPendentes = new ArrayList<>();
    private List<PedidosPendentes> listaRetornos = new ArrayList<>();
    private List<PedidosPendentes> listaRastreio = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autorizar_pedidos);

        drawerLayout = findViewById(R.id.drawerLayoutAutorizar);
        layoutMenuPastas = findViewById(R.id.layoutMenuPastas);
        layoutSubPasta = findViewById(R.id.layoutSubPasta);
        rvPasta = findViewById(R.id.rvPasta);
        tvTituloPasta = findViewById(R.id.tvTituloPasta);
        etPesquisaPasta = findViewById(R.id.etPesquisaPasta);

        SharedPreferences prefsSessao = getSharedPreferences("SessaoApp", Context.MODE_PRIVATE);
        usuarioAtual = prefsSessao.getString("usuarioLogado", MainActivity.usuarioLogado);
        prefsOcultos = getSharedPreferences("PedidosOcultos", MODE_PRIVATE);

        configurarNavegacaoMenu();
        
        rvPasta.setLayoutManager(new LinearLayoutManager(this));

        // Cliques nas Pastas
        findViewById(R.id.cardAguardando).setOnClickListener(v -> abrirPasta("Aguardando Autorização", listaPendentes, 0));
        findViewById(R.id.cardRetornos).setOnClickListener(v -> abrirPasta("Retornos Solicitados", listaRetornos, 2));
        findViewById(R.id.cardRastreio).setOnClickListener(v -> abrirPasta("Aguardando Rastreio", listaRastreio, 1));

        // Voltar para o Menu
        findViewById(R.id.btnVoltarMenu).setOnClickListener(v -> {
            layoutSubPasta.setVisibility(View.GONE);
            layoutMenuPastas.setVisibility(View.VISIBLE);
            etPesquisaPasta.setText("");
        });

        // Pesquisa dentro da pasta
        etPesquisaPasta.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (adapterAtual != null) adapterAtual.filtrar(s.toString());
            }
        });

        ImageView btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        buscarPedidos();
    }

    private void abrirPasta(String titulo, List<PedidosPendentes> lista, int tipo) {
        tvTituloPasta.setText(titulo);
        layoutMenuPastas.setVisibility(View.GONE);
        layoutSubPasta.setVisibility(View.VISIBLE);
        
        adapterAtual = new PedidoAdapter(lista, tipo, (pedido, acao) -> {
            if (acao == 99) {
                ocultarPedidoDaTela(pedido.id_emprestimo);
            } else if (acao == 3) {
                processarAutorizacao(pedido.id_emprestimo, 4); // Ciente do retorno
            } else {
                processarAutorizacao(pedido.id_emprestimo, acao); // Autorizar ou Recusar
            }
        });
        rvPasta.setAdapter(adapterAtual);
    }

    private void buscarPedidos() {
        SharedPreferences prefsSessao = getSharedPreferences("SessaoApp", Context.MODE_PRIVATE);
        String usuario = prefsSessao.getString("usuarioLogado", "");
        String nivel = prefsSessao.getString("nivelContaAtual", "0");
        String unidade = prefsSessao.getString("unidadeAtual", "");

        RetrofitClient.getApi().getMeusPedidos(usuario, nivel, unidade).enqueue(new Callback<List<PedidosPendentes>>() {
            @Override
            public void onResponse(Call<List<PedidosPendentes>> call, Response<List<PedidosPendentes>> response) {
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<PedidosPendentes> todos = response.body();
                    listaPendentes.clear();
                    listaRetornos.clear();
                    listaRastreio.clear();

                    Set<String> ocultos = prefsOcultos.getStringSet("ocultos_" + usuarioAtual, new HashSet<>());

                    for (PedidosPendentes p : todos) {
                        if (p.aprovacao == 3) {
                            listaRetornos.add(p);
                        } else if (p.aprovacao == 0) {
                            listaPendentes.add(p);
                        } else if (p.aprovacao == 1) {
                            if (!ocultos.contains(String.valueOf(p.id_emprestimo))) {
                                listaRastreio.add(p);
                            }
                        }
                    }
                    Collections.sort(listaPendentes, (p1, p2) -> Integer.compare(obterPesoPrioridade(p1.prioridade), obterPesoPrioridade(p2.prioridade)));
                    
                    // Se estiver com uma pasta aberta, atualiza a lista exibida agora
                    if (layoutSubPasta.getVisibility() == View.VISIBLE && adapterAtual != null) {
                        adapterAtual.notifyDataSetChanged();
                    }
                }
            }
            @Override public void onFailure(Call<List<PedidosPendentes>> call, Throwable t) {
                Toast.makeText(AutorizarPedidosActivity.this, "Falha de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processarAutorizacao(int idEmprestimo, int novoStatus) {
        AutorizarRequest request = new AutorizarRequest(idEmprestimo, novoStatus);
        RetrofitClient.getApi().autorizarPedido(request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                String msg = (novoStatus == 1) ? "Aprovado" : (novoStatus == 2) ? "Recusado" : "Confirmado";
                mostrarAlertaWeb(findViewById(android.R.id.content), "Pedido " + msg + " com sucesso!", "#1a4b9f");
                buscarPedidos();
                // Volta para o menu para dar feedback visual de atualização
                layoutSubPasta.setVisibility(View.GONE);
                layoutMenuPastas.setVisibility(View.VISIBLE);
            }
            @Override public void onFailure(Call<GenericResponse> call, Throwable t) {}
        });
    }

    private void ocultarPedidoDaTela(int idEmprestimo) {
        Set<String> ocultos = new HashSet<>(prefsOcultos.getStringSet("ocultos_" + usuarioAtual, new HashSet<>()));
        ocultos.add(String.valueOf(idEmprestimo));
        prefsOcultos.edit().putStringSet("ocultos_" + usuarioAtual, ocultos).apply();
        buscarPedidos();
        layoutSubPasta.setVisibility(View.GONE);
        layoutMenuPastas.setVisibility(View.VISIBLE);
    }

    private void mostrarAlertaWeb(View view, String mensagem, String corHexa) {
        Snackbar snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(Color.parseColor(corHexa));
        snackbar.show();
    }

    private int obterPesoPrioridade(String p) {
        if (p == null) return 4;
        switch (p.toLowerCase()) {
            case "alto": return 1;
            case "intermediário": case "médio": return 2;
            case "baixo": return 3;
            default: return 4;
        }
    }

    private void configurarNavegacaoMenu() {
        MenuSidebarHelper.configurarNavegacao(this, drawerLayout);
    }
}