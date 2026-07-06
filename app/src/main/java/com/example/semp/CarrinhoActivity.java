package com.example.semp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import com.example.semp.models.*;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarrinhoActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private List<Produto> itensCarrinhoCompleto = new java.util.ArrayList<>();
    private java.util.List<Integer> idsSelecionadosParaPedido = new java.util.ArrayList<>();
    private String usuarioSeguro = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrinho);

        SharedPreferences prefs = getSharedPreferences("SessaoApp", Context.MODE_PRIVATE);
        usuarioSeguro = prefs.getString("usuarioLogado", "");

        drawerLayout = findViewById(R.id.drawerLayoutCarrinho);
        ImageView btnMenu = findViewById(R.id.btnMenu);
        Button btnFinalizarPedido = findViewById(R.id.btnFinalizarPedido);
        RecyclerView recyclerViewCarrinho = findViewById(R.id.recyclerViewCarrinho);

        if (recyclerViewCarrinho != null) recyclerViewCarrinho.setLayoutManager(new LinearLayoutManager(this));

        View mainView = findViewById(R.id.mainContentLayout);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        if (btnMenu != null) btnMenu.setOnClickListener(v -> {
            if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
        });

        if (btnFinalizarPedido != null) {
            btnFinalizarPedido.setEnabled(false);
            btnFinalizarPedido.setOnClickListener(v -> {
                java.util.List<Produto> selecionados = new java.util.ArrayList<>();
                for (Produto p : itensCarrinhoCompleto) {
                    if (idsSelecionadosParaPedido.contains(p.id_estoque)) {
                        selecionados.add(p);
                    }
                }
                
                Intent intent = new Intent(CarrinhoActivity.this, FazerPedidoActivity.class);
                intent.putExtra("ITENS_SELECIONADOS", new Gson().toJson(selecionados));
                startActivity(intent);
            });
        }

        configurarNavegacaoMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView recyclerViewCarrinho = findViewById(R.id.recyclerViewCarrinho);
        buscarItensCarrinhoAPI(recyclerViewCarrinho);
    }

    private void buscarItensCarrinhoAPI(RecyclerView recyclerView) {
        if (recyclerView == null) return;
        TextView tvCarrinhoVazio = findViewById(R.id.tvCarrinhoVazio);
        Button btnFinalizarPedido = findViewById(R.id.btnFinalizarPedido);

        RetrofitClient.getApi().getProdutos().enqueue(new Callback<List<Produto>>() {
            @Override
            public void onResponse(Call<List<Produto>> call, Response<List<Produto>> responseProd) {
                List<Produto> todosProdutos = responseProd.body();

                RetrofitClient.getApi().getCarrinho(usuarioSeguro).enqueue(new Callback<List<Produto>>() {
                    @Override
                    public void onResponse(Call<List<Produto>> call, Response<List<Produto>> responseCarrinho) {
                        if (isDestroyed() || isFinishing()) return;

                        if (responseCarrinho.isSuccessful() && responseCarrinho.body() != null) {
                            List<Produto> itensCarrinho = responseCarrinho.body();
                            itensCarrinhoCompleto = itensCarrinho;

                            // OTIMIZAÇÃO: Uso de HashMap para evitar O(n*m) - Crucial para estoques grandes
                            if (todosProdutos != null) {
                                HashMap<String, Produto> mapaProdutos = new HashMap<>();
                                for (Produto p : todosProdutos) {
                                    if (p.nome != null) mapaProdutos.put(p.nome.toLowerCase(), p);
                                }

                                for (Produto itemC : itensCarrinho) {
                                    if (itemC.nome != null) {
                                        Produto pEstoque = mapaProdutos.get(itemC.nome.toLowerCase());
                                        if (pEstoque != null) {
                                            itemC.id_estoque = pEstoque.id_estoque;
                                            itemC.codigo = pEstoque.codigo;
                                            itemC.descricao = pEstoque.descricao;
                                            itemC.descricao_detalhada = pEstoque.descricao_detalhada;
                                            itemC.cor = pEstoque.cor;
                                            itemC.marca_ref = pEstoque.marca_ref;
                                            itemC.uni_natal = pEstoque.uni_natal;
                                            itemC.quant = pEstoque.quant;
                                            itemC.foto = pEstoque.foto;
                                        }
                                    }
                                }
                            }

                            if (itensCarrinho.isEmpty()) {
                                recyclerView.setVisibility(View.GONE);
                                if (tvCarrinhoVazio != null) tvCarrinhoVazio.setVisibility(View.VISIBLE);
                                if (btnFinalizarPedido != null) {
                                    btnFinalizarPedido.setVisibility(View.GONE);
                                    btnFinalizarPedido.setEnabled(false);
                                }
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                                if (tvCarrinhoVazio != null) tvCarrinhoVazio.setVisibility(View.GONE);
                                if (btnFinalizarPedido != null) btnFinalizarPedido.setVisibility(View.VISIBLE);

                                recyclerView.setAdapter(new CarrinhoAdapter(itensCarrinho, new CarrinhoAdapter.OnCarrinhoActionListener() {
                                    @Override
                                    public void onEditClick(Produto produto) {
                                        Intent intent = new Intent(CarrinhoActivity.this, ProdutoDetalheActivity.class);
                                        intent.putExtra("PRODUTO_ID", String.valueOf(produto.id_estoque));
                                        intent.putExtra("PRODUTO_NOME", produto.nome);
                                        intent.putExtra("PRODUTO_CODIGO", produto.codigo);
                                        intent.putExtra("PRODUTO_DESC", produto.descricao);
                                        intent.putExtra("PRODUTO_QTD", String.valueOf(produto.quant));
                                        intent.putExtra("PRODUTO_DESC_DETALHADA", produto.descricao_detalhada);
                                        intent.putExtra("PRODUTO_COR", produto.cor);
                                        intent.putExtra("PRODUTO_MARCA", produto.marca_ref);
                                        intent.putExtra("PRODUTO_UNI_NATAL", produto.uni_natal);
                                        intent.putExtra("PRODUTO_FOTO", produto.foto != null ? produto.foto : "");

                                        int qtdNoCarrinho = produto.quantidade > 0 ? produto.quantidade : (produto.carrinho > 0 ? produto.carrinho : 1);
                                        intent.putExtra("PRODUTO_QTD_CARRINHO", String.valueOf(qtdNoCarrinho));
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onDeleteClick(Produto produto) {
                                        int qtdParaRemover = produto.quantidade > 0 ? produto.quantidade : produto.carrinho;
                                        RetrofitClient.getApi().removerDoCarrinho(usuarioSeguro, new CarrinhoRequest(produto.nome, qtdParaRemover)).enqueue(new Callback<GenericResponse>() {
                                            @Override
                                            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                                                if(response.isSuccessful() && response.body() != null && response.body().sucesso) {
                                                    Toast.makeText(CarrinhoActivity.this, "Item removido!", Toast.LENGTH_SHORT).show();
                                                    buscarItensCarrinhoAPI(recyclerView);
                                                }
                                            }
                                            @Override
                                            public void onFailure(Call<GenericResponse> call, Throwable t) {
                                                Toast.makeText(CarrinhoActivity.this, "Erro ao remover", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onSelectionChanged(java.util.List<Integer> ids) {
                                        idsSelecionadosParaPedido = ids;
                                        if (btnFinalizarPedido != null) {
                                            btnFinalizarPedido.setEnabled(!idsSelecionadosParaPedido.isEmpty());
                                        }
                                    }
                                }));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Produto>> call, Throwable t) {
                        Toast.makeText(CarrinhoActivity.this, "Erro ao buscar carrinho", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Produto>> call, Throwable t) {}
        });
    }

    private void configurarNavegacaoMenu() {
        MenuSidebarHelper.configurarNavegacao(this, drawerLayout);
    }
}