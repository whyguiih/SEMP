package com.example.semp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.semp.models.GenericResponse;
import com.example.semp.models.PedidoRequest;
import com.example.semp.models.Produto;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FazerPedidoActivity extends AppCompatActivity {

    private EditText etEmail, etDataReserva, etJustificativa, etNomeUsuario, etCodigoPedido;
    private Spinner spinnerPrioridade;
    private Button btnConfirmar, btnGerarCodigo;
    private List<Produto> listaProdutosParaPedido = new ArrayList<>();
    private DrawerLayout drawerLayout;

    // Variáveis seguras da sessão
    private String usuarioSeguro = "Usuario";
    private String unidadeSegura = "Unidade Central";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fazer_pedido);

        drawerLayout = findViewById(R.id.drawerLayoutFazerPedido);
        View btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
            });
        }
        MenuSidebarHelper.configurarNavegacao(this, drawerLayout);

        // Busca dados da sessão de forma segura (previne NullPointerException)
        SharedPreferences prefs = getSharedPreferences("SessaoApp", Context.MODE_PRIVATE);
        usuarioSeguro = prefs.getString("usuarioLogado", "Usuario");
        unidadeSegura = prefs.getString("unidadeAtual", "Unidade Central");

        if (getIntent().hasExtra("ITENS_SELECIONADOS")) {
            String json = getIntent().getStringExtra("ITENS_SELECIONADOS");
            if (json != null) {
                java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<Produto>>() {}.getType();
                listaProdutosParaPedido = new Gson().fromJson(json, listType);
            }
        }

        etNomeUsuario = findViewById(R.id.etNomeUsuario);
        etEmail = findViewById(R.id.etEmail);
        etDataReserva = findViewById(R.id.etDataReserva);
        etJustificativa = findViewById(R.id.etJustificativa);
        etCodigoPedido = findViewById(R.id.etCodigoPedido);
        spinnerPrioridade = findViewById(R.id.spinnerPrioridade);
        btnConfirmar = findViewById(R.id.btnConfirmarEmprestimo);
        btnGerarCodigo = findViewById(R.id.btnGerarCodigoPedido);
        View btnVoltar = findViewById(R.id.btnVoltarCarrinho);

        if (etNomeUsuario != null) {
            etNomeUsuario.setText(usuarioSeguro);
        }

        if (spinnerPrioridade != null) {
            String[] prioridades = {"Baixo", "Médio", "Alto"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, prioridades);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPrioridade.setAdapter(adapter);
        }

        if (etDataReserva != null) {
            etDataReserva.setOnClickListener(v -> abrirCalendario());
        }

        if (btnGerarCodigo != null) {
            btnGerarCodigo.setOnClickListener(v -> {
                String idEstado = prefs.getString("id_estado", "X");
                String idRegiao = prefs.getString("id_regiao", "X");
                int idUnidade = prefs.getInt("id_unidade", 0);
                
                String novoCodigo = SempUtils.gerarCodigoPedidoModerno(idEstado, idRegiao, idUnidade);
                etCodigoPedido.setText(novoCodigo);
                Toast.makeText(this, "Código do Pedido Gerado!", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> finish());
        }

        btnConfirmar.setOnClickListener(v -> efetivarPedido());
    }

    private void abrirCalendario() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Selecione o período da reserva");

        final MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection.first != null && selection.second != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String dataInicio = sdf.format(new Date(selection.first));
                String dataFim = sdf.format(new Date(selection.second));
                
                String periodo = dataInicio + " até " + dataFim;
                etDataReserva.setText(periodo);
            }
        });
        picker.show(getSupportFragmentManager(), "RANGE_PICKER");
    }

    private void efetivarPedido() {
        if (listaProdutosParaPedido == null || listaProdutosParaPedido.isEmpty()) {
            Toast.makeText(this, "Nenhum produto selecionado no carrinho!", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = etEmail.getText().toString().trim();
        String dataReserva = etDataReserva.getText().toString().trim();
        String justificativa = etJustificativa.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Insira um e-mail válido");
            etEmail.requestFocus();
            return;
        }

        if (dataReserva.isEmpty()) {
            etDataReserva.setError("Selecione uma data");
            return;
        }

        btnConfirmar.setEnabled(false);
        btnConfirmar.setText("Enviando...");

        String dataPostagemComHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        String prioridadeSelecionada = spinnerPrioridade.getSelectedItem() != null ? spinnerPrioridade.getSelectedItem().toString() : "Baixo";
        String prioridadeParaDB = "baixo";
        switch (prioridadeSelecionada) {
            case "Alto": prioridadeParaDB = "alto"; break;
            case "Médio": prioridadeParaDB = "intermediário"; break;
        }

        // GERA O CÓDIGO DO PEDIDO (SE NÃO TIVER SIDO GERADO MANUALMENTE)
        String codigoPedidoGerado = etCodigoPedido.getText().toString().trim();
        if (codigoPedidoGerado.isEmpty()) {
            Toast.makeText(this, "Por favor, gere o código do pedido antes de confirmar!", Toast.LENGTH_SHORT).show();
            return;
        }

        // MONTA A LISTA DE PRODUTOS COM DADOS REAIS
        List<PedidoRequest.ProdutoPedido> produtosFormatados = new ArrayList<>();
        SharedPreferences sessao = getSharedPreferences("SessaoApp", Context.MODE_PRIVATE);
        for (Produto p : listaProdutosParaPedido) {
            // Usa o código do produto que já existe ou gera um novo prefixado com 2
            String idEst = sessao.getString("id_estado", "X");
            String idReg = sessao.getString("id_regiao", "X");
            int idUni = sessao.getInt("id_unidade", 0);
            String codigoProdutoGerado = SempUtils.gerarCodigoProdutoModerno(idEst, idReg, idUni);
            
            // Pega a quantidade correta do carrinho
            int qtdSolicitada = p.quantidade > 0 ? p.quantidade : (p.carrinho > 0 ? p.carrinho : 1);

            produtosFormatados.add(new PedidoRequest.ProdutoPedido(
                    p.codigo != null ? p.codigo : codigoProdutoGerado,
                    p.nome != null ? p.nome : "Produto Sem Nome",
                    qtdSolicitada,
                    p.unidade_atual != null ? p.unidade_atual : unidadeSegura,
                    codigoPedidoGerado
            ));
        }

        PedidoRequest request = new PedidoRequest(
                usuarioSeguro, email, unidadeSegura, dataReserva,
                produtosFormatados, prioridadeParaDB,
                justificativa.isEmpty() ? "Solicitação de Empréstimo" : justificativa,
                dataPostagemComHora, codigoPedidoGerado
        );

        RetrofitClient.getApi().fazerPedido(request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                btnConfirmar.setEnabled(true);
                btnConfirmar.setText("Confirmar Pedido");
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().sucesso) {
                        Toast.makeText(FazerPedidoActivity.this, "Pedido efetivado! Cód: " + codigoPedidoGerado, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        // EXIBE A MENSAGEM DE CONFLITO VINDA DO SERVIDOR
                        String msgErro = response.body().mensagem != null ? response.body().mensagem : "Erro ao processar reserva.";
                        Toast.makeText(FazerPedidoActivity.this, msgErro, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(FazerPedidoActivity.this, "Erro no servidor: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                btnConfirmar.setEnabled(true);
                btnConfirmar.setText("Confirmar Pedido");
                Toast.makeText(FazerPedidoActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }
}