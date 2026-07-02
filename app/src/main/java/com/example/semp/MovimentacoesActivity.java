package com.example.semp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.semp.models.Rastreio;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private String dataFiltro = "";
    private TextView tvSaidas, tvChegadas;

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
        tvSaidas = findViewById(R.id.tvSaidas);
        tvChegadas = findViewById(R.id.tvChegadas);

        rvSaidasHoje.setLayoutManager(new LinearLayoutManager(this));
        rvChegadasHoje.setLayoutManager(new LinearLayoutManager(this));

        EditText etDataFiltro = findViewById(R.id.etDataFiltro);
        Button btnLimpar = findViewById(R.id.btnLimparFiltro);

        dataFiltro = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (etDataFiltro != null) {
            etDataFiltro.setText(dataFiltro);
            etDataFiltro.setOnClickListener(v -> abrirCalendario(etDataFiltro));
        }

        if (btnLimpar != null) {
            btnLimpar.setOnClickListener(v -> {
                dataFiltro = "";
                etDataFiltro.setText("");
                carregarMovimentacoes();
            });
        }

        carregarMovimentacoes();
    }

    private void abrirCalendario(EditText editText) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    dataFiltro = year + "-" + String.format(Locale.getDefault(), "%02d", (month + 1)) + "-" + String.format(Locale.getDefault(), "%02d", day);
                    editText.setText(dataFiltro);
                    carregarMovimentacoes();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void carregarMovimentacoes() {
        if (dataFiltro.isEmpty()) {
            tvSaidas.setText("Todas as Saídas");
            tvChegadas.setText("Todas as Chegadas");
        } else {
            tvSaidas.setText("Saídas em " + dataFiltro);
            tvChegadas.setText("Chegadas em " + dataFiltro);
        }

        RetrofitClient.getApi().getTodosRastreios().enqueue(new Callback<List<Rastreio>>() {
            @Override
            public void onResponse(Call<List<Rastreio>> call, Response<List<Rastreio>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Rastreio> todosRastreios = response.body();

                    // MÁGICA 1: AGRUPAR O HISTÓRICO
                    HashMap<String, List<Rastreio>> pacotes = new HashMap<>();
                    for (Rastreio r : todosRastreios) {
                        if (!pacotes.containsKey(r.codigo)) pacotes.put(r.codigo, new ArrayList<>());
                        pacotes.get(r.codigo).add(r);
                    }

                    List<Rastreio> saidasFiltradas = new ArrayList<>();
                    List<Rastreio> chegadasFiltradas = new ArrayList<>();

                    for (String codigo : pacotes.keySet()) {
                        List<Rastreio> historico = pacotes.get(codigo);
                        Rastreio atual = historico.get(historico.size() - 1);

                        // Ignora se já retornou (concluído)
                        if (historico.size() > 1 && atual.unidade_original.equalsIgnoreCase(atual.unidade_destino)) {
                            continue;
                        }

                        if (dataFiltro.isEmpty() || (atual.data_saida != null && atual.data_saida.equals(dataFiltro))) {
                            if (atual.unidade_original.equalsIgnoreCase(minhaUnidade)) {
                                saidasFiltradas.add(atual);
                            }
                        }

                        if (dataFiltro.isEmpty() || (atual.data_entrada != null && atual.data_entrada.equals(dataFiltro))) {
                            if (atual.unidade_destino.equalsIgnoreCase(minhaUnidade)) {
                                chegadasFiltradas.add(atual);
                            }
                        }
                    }

                    rvSaidasHoje.setAdapter(new RastreioAdapter(saidasFiltradas));
                    rvChegadasHoje.setAdapter(new RastreioAdapter(chegadasFiltradas));
                }
            }

            @Override
            public void onFailure(Call<List<Rastreio>> call, Throwable t) {
                Toast.makeText(MovimentacoesActivity.this, "Erro ao carregar.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}