package com.example.semp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import com.example.semp.models.Produto;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstoqueActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private List<Produto> listaOriginalProdutos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estoque);

        drawerLayout = findViewById(R.id.drawerLayout);
        ImageView btnMenu = findViewById(R.id.btnMenu);
        EditText etPesquisa = findViewById(R.id.etPesquisa);
        RecyclerView recyclerViewEstoque = findViewById(R.id.recyclerViewEstoque);

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
            buscarProdutosAPI(recyclerViewEstoque);
        }

        if (etPesquisa != null) {
            etPesquisa.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    String termo = s.toString().toLowerCase();
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
            });
        }
    }

    private void buscarProdutosAPI(RecyclerView recyclerView) {
        RetrofitClient.getApi().getProdutos().enqueue(new Callback<List<Produto>>() {
            @Override
            public void onResponse(Call<List<Produto>> call, Response<List<Produto>> response) {
                if (isDestroyed() || isFinishing()) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    listaOriginalProdutos = response.body();
                    recyclerView.setAdapter(new EstoqueAdapter(listaOriginalProdutos, EstoqueActivity.this));
                }
            }

            @Override
            public void onFailure(Call<List<Produto>> call, Throwable t) {
                if (!isDestroyed() && !isFinishing()) {
                    Toast.makeText(EstoqueActivity.this, "Erro ao buscar produtos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void abrirDetalhesProduto(Produto produto) {
        Intent intent = new Intent(this, ProdutoDetalheActivity.class);
        intent.putExtra("PRODUTO_ID", produto.id_estoque != 0 ? String.valueOf(produto.id_estoque) : "");
        intent.putExtra("PRODUTO_NOME", produto.nome);
        intent.putExtra("PRODUTO_CODIGO", produto.codigo);
        intent.putExtra("PRODUTO_DESC", produto.descricao);
        intent.putExtra("PRODUTO_QTD", produto.quant);
        startActivity(intent);
    }

    private void configurarNavegacaoMenu() {
        MenuSidebarHelper.configurarNavegacao(this, drawerLayout);
    }
}