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

    // Variáveis seguras da sessão
    private String usuarioSeguro = "Usuario";
    private String unidadeSegura = "Unidade Central";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fazer_pedido);

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

        // Validações melhoradas
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Insira um e-mail válido");
            etEmail.requestFocus();
            return;
        }

        if (dataReserva.isEmpty()) {
            etDataReserva.setError("Selecione uma data");
            return;
        }

        // Bloqueia duplo clique
        btnConfirmar.setEnabled(false);
        btnConfirmar.setText("Enviando...");

        String dataPostagemComHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // --- INÍCIO DA CORREÇÃO ---
        String prioridadeSelecionada = spinnerPrioridade.getSelectedItem() != null ? spinnerPrioridade.getSelectedItem().toString() : "Baixo";
        String prioridadeParaDB = "baixo"; // Valor padrão em minúsculo

        // Converte o valor visual para o formato exigido pelo banco de dados
        switch (prioridadeSelecionada) {
            case "Alto":
                prioridadeParaDB = "alto";
                break;
            case "Médio":
                prioridadeParaDB = "intermediário";
                break;
            case "Baixo":
            default:
                prioridadeParaDB = "baixo";
                break;
        }
        // --- FIM DA CORREÇÃO ---

        PedidoRequest request = new PedidoRequest(
                usuarioSeguro,
                email,
                unidadeSegura,
                dataReserva,
                listaIdsParaPedido,
                prioridadeParaDB, // <-- Usa a variável corrigida aqui
                justificativa.isEmpty() ? "Solicitação de Empréstimo" : justificativa,
                dataPostagemComHora
        );

        Log.d("DEBUG_PEDIDO", "JSON_COMPLETO: " + new Gson().toJson(request));

        RetrofitClient.getApi().fazerPedido(request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                btnConfirmar.setEnabled(true);
                btnConfirmar.setText("Confirmar Pedido");

                if (response.isSuccessful() && response.body() != null && Boolean.TRUE.equals(response.body().sucesso)) {
                    Toast.makeText(FazerPedidoActivity.this, "Pedido efetivado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish(); // Volta ao carrinho
                } else {
                    String msg = "Erro no servidor.";
                    if (response.body() != null && response.body().mensagem != null) {
                        msg = response.body().mensagem;
                    } else if (response.errorBody() != null) {
                        try {
                            String errorStr = response.errorBody().string();
                            if (errorStr.contains("check constraint failed")) {
                                msg = "Verifique se todos os campos estão preenchidos corretamente.";
                            } else {
                                msg = errorStr;
                            }
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(FazerPedidoActivity.this, "Erro: " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                btnConfirmar.setEnabled(true);
                btnConfirmar.setText("Confirmar Pedido");
                Toast.makeText(FazerPedidoActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}