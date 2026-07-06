package com.example.semp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.semp.models.GenericResponse;
import com.example.semp.models.PedidosPendentes;
import com.example.semp.models.RetornoRequest;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItensEmprestadosActivity extends AppCompatActivity {

    private String minhaUnidade = "";
    private RecyclerView rvEmprestados;
    private android.widget.TextView tvEmpty;
    private DrawerLayout drawerLayout;
    private EmprestadosAdapter adapter;
    private EditText etPesquisa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itens_emprestados);

        drawerLayout = findViewById(R.id.drawerLayout);
        ImageView btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
            });
        }
        MenuSidebarHelper.configurarNavegacao(this, drawerLayout);

        SharedPreferences prefs = getSharedPreferences("SessaoApp", Context.MODE_PRIVATE);
        minhaUnidade = prefs.getString("unidadeAtual", "");

        rvEmprestados = findViewById(R.id.rvItensEmprestados);
        tvEmpty = findViewById(R.id.tvEmptyEmprestados);
        etPesquisa = findViewById(R.id.etPesquisa);

        if (etPesquisa != null) {
            etPesquisa.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (adapter != null) adapter.filtrar(s.toString());
                }
            });
        }

        if (rvEmprestados != null) {
            rvEmprestados.setLayoutManager(new LinearLayoutManager(this));
        }

        carregarItensEmprestados();
    }

    private void carregarItensEmprestados() {
        RetrofitClient.getApi().getItensEmprestados(minhaUnidade).enqueue(new Callback<List<PedidosPendentes>>() {
            @Override
            public void onResponse(Call<List<PedidosPendentes>> call, Response<List<PedidosPendentes>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PedidosPendentes> lista = response.body();

                    if (lista.isEmpty()) {
                        if (tvEmpty != null) tvEmpty.setVisibility(android.view.View.VISIBLE);
                        if (rvEmprestados != null) rvEmprestados.setVisibility(android.view.View.GONE);
                    } else {
                        if (tvEmpty != null) tvEmpty.setVisibility(android.view.View.GONE);
                        if (rvEmprestados != null) rvEmprestados.setVisibility(android.view.View.VISIBLE);
                        adapter = new EmprestadosAdapter(lista, pedido -> mostrarSeletorData(pedido.id_emprestimo));
                        rvEmprestados.setAdapter(adapter);

                        if (etPesquisa != null && !etPesquisa.getText().toString().isEmpty()) {
                            adapter.filtrar(etPesquisa.getText().toString());
                        }
                    }
                } else {
                    if (tvEmpty != null) tvEmpty.setVisibility(android.view.View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<PedidosPendentes>> call, Throwable t) {
                Toast.makeText(ItensEmprestadosActivity.this, "Erro de rede.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarSeletorData(int idEmprestimo) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    String dataFormatada = year + "-" + String.format(Locale.getDefault(), "%02d", (month + 1)) + "-" + String.format(Locale.getDefault(), "%02d", day);
                    solicitarRetornoAPI(idEmprestimo, dataFormatada);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void solicitarRetornoAPI(int idEmprestimo, String dataRetorno) {
        RetornoRequest req = new RetornoRequest(idEmprestimo, dataRetorno);
        RetrofitClient.getApi().solicitarRetorno(req).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                Toast.makeText(ItensEmprestadosActivity.this, "Retorno solicitado com sucesso!", Toast.LENGTH_SHORT).show();
                carregarItensEmprestados(); // Atualiza a tela
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(ItensEmprestadosActivity.this, "Falha na solicitação.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
