package com.example.semp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
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
import java.util.Collections; // ADICIONADO: Importação necessária para ordenar a lista
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AutorizarPedidosActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerViewPedidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autorizar_pedidos);

        drawerLayout = findViewById(R.id.drawerLayoutAutorizar);
        recyclerViewPedidos = findViewById(R.id.recyclerViewPedidos);

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

        if (recyclerViewPedidos != null) {
            recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(this));
            buscarPedidosPendentes();
        }
    }

    private void buscarPedidosPendentes() {
        RetrofitClient.getApi().getPedidosPendentes().enqueue(new Callback<List<PedidosPendentes>>() {
            @Override
            public void onResponse(Call<List<PedidosPendentes>> call, Response<List<PedidosPendentes>> response) {
                if (isDestroyed() || isFinishing()) return;

                List<PedidosPendentes> lista = response.body();
                if (response.isSuccessful() && lista != null) {

                    if (lista.isEmpty()) {
                        Toast.makeText(AutorizarPedidosActivity.this, "Não há pedidos pendentes.", Toast.LENGTH_SHORT).show();
                    } else {
                        // ADICIONADO: Lógica de Ordenação
                        // Ordena a lista baseada no peso da prioridade antes de passar para o Adapter
                        Collections.sort(lista, (p1, p2) -> {
                            // IMPORTANTE: Se o método de pegar a prioridade no seu modelo tiver outro nome
                            // (ex: p1.prioridade em vez de p1.getPrioridade()), altere aqui.
                            int peso1 = obterPesoPrioridade(p1.prioridade);
                            int peso2 = obterPesoPrioridade(p2.prioridade);
                            return Integer.compare(peso1, peso2);
                        });
                    }

                    if (recyclerViewPedidos != null) {
                        recyclerViewPedidos.setAdapter(new PedidoAdapter(lista, (pedido, novoStatus) -> processarAutorizacao(pedido.id_emprestimo, novoStatus)));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<PedidosPendentes>> call, Throwable t) {
                if (!isDestroyed() && !isFinishing()) {
                    Toast.makeText(AutorizarPedidosActivity.this, "Erro ao carregar pedidos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ADICIONADO: Função auxiliar para converter o texto em peso numérico
    private int obterPesoPrioridade(String prioridade) {
        if (prioridade == null) return 4; // Se vier nulo, joga pro final da fila

        switch (prioridade.toLowerCase()) {
            case "alto":
                return 1;
            case "intermediário":
            case "intermediario":
                return 2;
            case "baixo":
                return 3;
            default:
                return 4; // Qualquer outro valor desconhecido vai pro final
        }
    }

    private void processarAutorizacao(int idEmprestimo, int novoStatus) {
        AutorizarRequest request = new AutorizarRequest(idEmprestimo, novoStatus);
        RetrofitClient.getApi().autorizarPedido(request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                String acao = (novoStatus == 1) ? "aprovado" : "recusado";
                Toast.makeText(AutorizarPedidosActivity.this, "Pedido " + acao + " com sucesso!", Toast.LENGTH_SHORT).show();
                buscarPedidosPendentes();
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(AutorizarPedidosActivity.this, "Erro ao processar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarNavegacaoMenu() {
        MenuSidebarHelper.configurarNavegacao(this, drawerLayout);
    }
}