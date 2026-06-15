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
import com.example.semp.models.PedidosPendentes;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisualizarPedidoActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;

    // Variáveis seguras
    private String nivelSeguro = "0";
    private String unidadeSegura = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_pedido);

        // Resgate pelo SharedPreferences
        SharedPreferences prefs = getSharedPreferences("SessaoApp", Context.MODE_PRIVATE);
        nivelSeguro = prefs.getString("nivelContaAtual", "0");
        unidadeSegura = prefs.getString("unidadeAtual", "");

        drawerLayout = findViewById(R.id.drawerLayout);
        ImageView btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) btnMenu.setOnClickListener(v -> {
            if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
        });

        MenuSidebarHelper.configurarNavegacao(this, drawerLayout);

        recyclerView = findViewById(R.id.rvMeusPedidos);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            carregarPedidos();
        }
    }

    private void carregarPedidos() {
        RetrofitClient.getApi().getMeusPedidos(
                null,
                nivelSeguro,
                unidadeSegura
        ).enqueue(new Callback<List<PedidosPendentes>>() {
            @Override
            public void onResponse(Call<List<PedidosPendentes>> call, Response<List<PedidosPendentes>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        Toast.makeText(VisualizarPedidoActivity.this, "Nenhum pedido encontrado para esta unidade.", Toast.LENGTH_SHORT).show();
                    } else {
                        recyclerView.setAdapter(new MeusPedidosAdapter(response.body()));
                    }
                }
            }
            @Override
            public void onFailure(Call<List<PedidosPendentes>> call, Throwable t) {
                Toast.makeText(VisualizarPedidoActivity.this, "Erro de Conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }
}