package com.example.semp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.semp.models.GenericResponse;
import com.example.semp.models.PedidoRequest;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FazerPedidoActivity extends AppCompatActivity {

    private EditText etEmail, etDataReserva, etJustificativa, etNomeUsuario;
    private Spinner spinnerPrioridade;
    private Button btnConfirmar;
    private List<Integer> listaIdsParaPedido = new ArrayList<>();
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

        if (getIntent().hasExtra("IDS_SELECIONADOS")) {
            listaIdsParaPedido = getIntent().getIntegerArrayListExtra("IDS_SELECIONADOS");
        }

        etNomeUsuario = findViewById(R.id.etNomeUsuario);
        etEmail = findViewById(R.id.etEmail);
        etDataReserva = findViewById(R.id.etDataReserva);
        etJustificativa = findViewById(R.id.etJustificativa);
        spinnerPrioridade = findViewById(R.id.spinnerPrioridade);
        btnConfirmar = findViewById(R.id.btnConfirmarEmprestimo);
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

        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> finish());
        }

        btnConfirmar.setOnClickListener(v -> efetivarPedido());
    }

    private void abrirCalendario() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    String dataFormatada = year + "-" + String.format(Locale.getDefault(), "%02d", (month + 1)) + "-" + String.format(Locale.getDefault(), "%02d", day);
                    etDataReserva.setText(dataFormatada);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void efetivarPedido() {
        if (listaIdsParaPedido == null || listaIdsParaPedido.isEmpty()) {
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

        // GERA O CÓDIGO DO PEDIDO
        String codigoPedidoGerado = SempUtils.gerarCodigoSemp(unidadeSegura, 3);

        // MONTA A LISTA DE PRODUTOS COM CÓDIGOS Semp INDIVIDUAIS
        List<PedidoRequest.ProdutoPedido> produtosFormatados = new ArrayList<>();
        for (Integer id : listaIdsParaPedido) {
            // Aqui você idealmente resgata o nome e a quantidade exata do carrinho.
            // Como o ID é o que temos, vamos estruturar a base:
            String codigoProdutoGerado = SempUtils.gerarCodigoSemp(unidadeSegura, 2);

            produtosFormatados.add(new PedidoRequest.ProdutoPedido(
                    codigoProdutoGerado,
                    "Nome do Produto (Busque do Carrinho)",
                    1, // Quantidade
                    unidadeSegura,
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
                if (response.isSuccessful() && response.body() != null && response.body().sucesso) {
                    Toast.makeText(FazerPedidoActivity.this, "Pedido efetivado! Cód: " + codigoPedidoGerado, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(FazerPedidoActivity.this, "Erro no servidor.", Toast.LENGTH_LONG).show();
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