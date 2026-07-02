package com.example.semp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.semp.models.Rastreio;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovimentacoesActivity extends AppCompatActivity {

    private RecyclerView rvSaidasHoje, rvChegadasHoje;
    private String minhaUnidade = "";
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimentacoes);

        drawerLayout = findViewById(R.id.drawerLayout);
        ImageView btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
            });
        }
        MenuSidebarHelper.configurarNavegacao(this, drawerLayout);

        SharedPreferences prefs = getSharedPreferences("SessaoApp", Context.MODE_PRIVATE);
        String u = prefs.getString("unidadeAtual", "");
        minhaUnidade = (u != null ? u : "").toLowerCase();

        rvSaidasHoje = findViewById(R.id.rvSaidasHoje);
        rvChegadasHoje = findViewById(R.id.rvChegadasHoje);

        rvSaidasHoje.setLayoutManager(new LinearLayoutManager(this));
        rvChegadasHoje.setLayoutManager(new LinearLayoutManager(this));

        carregarMovimentacoes();
    }

    private void carregarMovimentacoes() {
        RetrofitClient.getApi().getTodosRastreios().enqueue(new Callback<List<Rastreio>>() {
            @Override
            public void onResponse(Call<List<Rastreio>> call, Response<List<Rastreio>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Rastreio> todosRastreios = response.body();
                    String hoje = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                    // MÁGICA 1: AGRUPAR O HISTÓRICO
                    HashMap<String, List<Rastreio>> pacotes = new HashMap<>();
                    for (Rastreio r : todosRastreios) {
                        if (!pacotes.containsKey(r.codigo)) pacotes.put(r.codigo, new ArrayList<>());
                        pacotes.get(r.codigo).add(r);
                    }

                    List<Rastreio> saidasHoje = new ArrayList<>();
                    List<Rastreio> chegadasHoje = new ArrayList<>();

                    for (String codigo : pacotes.keySet()) {
                        List<Rastreio> historico = pacotes.get(codigo);
                        Rastreio atual = historico.get(historico.size() - 1);

                        // Ignora se já retornou (concluído)
                        if (historico.size() > 1 && atual.unidade_original.equalsIgnoreCase(atual.unidade_destino)) {
                            continue;
                        }

                        if (atual.data_saida != null && atual.data_saida.equals(hoje) && atual.unidade_original.equalsIgnoreCase(minhaUnidade)) {
                            saidasHoje.add(atual);
                        }

                        if (atual.data_entrada != null && atual.data_entrada.equals(hoje) && atual.unidade_destino.equalsIgnoreCase(minhaUnidade)) {
                            chegadasHoje.add(atual);
                        }
                    }

                    rvSaidasHoje.setAdapter(new RastreioAdapter(saidasHoje));
                    rvChegadasHoje.setAdapter(new RastreioAdapter(chegadasHoje));
                }
            }

            @Override
            public void onFailure(Call<List<Rastreio>> call, Throwable t) {
                Toast.makeText(MovimentacoesActivity.this, "Erro ao carregar.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}