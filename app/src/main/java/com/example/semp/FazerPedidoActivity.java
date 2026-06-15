package com.example.semp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.semp.models.GenericResponse;
import com.example.semp.models.PedidoRequest;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FazerPedidoActivity extends AppCompatActivity {

    private EditText etEmail, etDataReserva, etJustificativa, etNomeUsuario;
    private Spinner spinnerPrioridade;
    private List<Integer> listaIdsParaPedido = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fazer_pedido);

        // Recebe os IDs selecionados do carrinho
        if (getIntent().hasExtra("IDS_SELECIONADOS")) {
            listaIdsParaPedido = getIntent().getIntegerArrayListExtra("IDS_SELECIONADOS");
        }

        etNomeUsuario = findViewById(R.id.etNomeUsuario);
        etEmail = findViewById(R.id.etEmail);
        etDataReserva = findViewById(R.id.etDataReserva);
        etJustificativa = findViewById(R.id.etJustificativa);
        spinnerPrioridade = findViewById(R.id.spinnerPrioridade);
        Button btnConfirmar = findViewById(R.id.btnConfirmarEmprestimo);
        View btnVoltar = findViewById(R.id.btnVoltarCarrinho);

        // Preenche o nome do usuário logado
        if (etNomeUsuario != null) {
            etNomeUsuario.setText(MainActivity.usuarioLogado);
        }

        // Configura o Spinner de Prioridades (Formatado conforme PHP/Banco de Dados)
        if (spinnerPrioridade != null) {
            String[] prioridades = {"Baixo", "Médio", "Alto"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, prioridades);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPrioridade.setAdapter(adapter);
        }

        // Configura o seletor de data (Agenda)
        if (etDataReserva != null) {
            etDataReserva.setOnClickListener(v -> abrirCalendario());
        }

        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> finish());
        }

        btnConfirmar.setOnClickListener(v -> {
            efetivarPedido();
        });

        // Removido carregarCarrinhoParaPedido() pois agora recebemos via Intent
    }

    private void abrirCalendario() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String dataFormatada = year1 + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + String.format("%02d", dayOfMonth);
                    etDataReserva.setText(dataFormatada);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void carregarCarrinhoParaPedido() {
        // Removido o conteúdo original pois a lista vem via Intent
    }

    private void efetivarPedido() {
        if (listaIdsParaPedido == null || listaIdsParaPedido.isEmpty()) {
            Toast.makeText(this, "Nenhum produto selecionado!", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = etEmail.getText().toString().trim();
        String dataReserva = etDataReserva.getText().toString().trim();
        String justificativa = etJustificativa.getText().toString().trim();
        
        // Captura o valor e trata para o formato que a API Cloudflare Workers parece esperar
        String itemSelecionado = spinnerPrioridade.getSelectedItem() != null ? spinnerPrioridade.getSelectedItem().toString() : "baixa";
        String prioridade = itemSelecionado.toLowerCase();

        if (email.isEmpty() || dataReserva.isEmpty()) {
            Toast.makeText(this, "Preencha o e-mail e a data!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Cria o request seguindo EXATAMENTE a lógica do seu PHP
        String dataPostagemComHora = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
        
        // Prioridade exata do PHP: Baixo, Médio, Alto (Primeira letra maiúscula)
        String prioridadeOriginal = spinnerPrioridade.getSelectedItem() != null ? spinnerPrioridade.getSelectedItem().toString() : "Baixo";
        
        // No seu PHP o campo é 'remetente', 'email', 'unidade', 'data_reserva', 'produtos', 'prioridade', 'motivo', 'data_postagem'
        PedidoRequest request = new PedidoRequest(
                MainActivity.usuarioLogado != null && !MainActivity.usuarioLogado.isEmpty() ? MainActivity.usuarioLogado : "Usuario",
                email,
                MainActivity.unidadeAtual != null && !MainActivity.unidadeAtual.isEmpty() ? MainActivity.unidadeAtual : "Unidade Central",
                dataReserva,
                listaIdsParaPedido,
                prioridadeOriginal, 
                justificativa.isEmpty() ? "Solicitação de Empréstimo" : justificativa,
                dataPostagemComHora
        );

        // LOG DE SEGURANÇA: Mostra o JSON completo para conferência manual se necessário
        String jsonPedido = new com.google.gson.Gson().toJson(request);
        android.util.Log.d("DEBUG_PEDIDO", "JSON_COMPLETO: " + jsonPedido);
        android.util.Log.d("DEBUG_PEDIDO", "REMETENTE: " + MainActivity.usuarioLogado);
        android.util.Log.d("DEBUG_PEDIDO", "EMAIL: " + email);
        android.util.Log.d("DEBUG_PEDIDO", "PRIORIDADE: " + prioridadeOriginal);
        android.util.Log.d("DEBUG_PEDIDO", "IDS: " + listaIdsParaPedido.toString());

        RetrofitClient.getApi().fazerPedido(request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().sucesso) {
                    Toast.makeText(FazerPedidoActivity.this, "Pedido efetivado!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String msg = "Erro no servidor.";
                    if (response.body() != null && response.body().mensagem != null) {
                        msg = response.body().mensagem;
                    } else if (response.errorBody() != null) {
                        try {
                            String errorStr = response.errorBody().string();
                            android.util.Log.e("DEBUG_PEDIDO", "ERRO_CORPO: " + errorStr);
                            if (errorStr.contains("check constraint failed")) {
                                msg = "Erro de validação: Verifique se todos os campos estão preenchidos corretamente (E-mail, Data, Justificativa).";
                            } else {
                                msg = errorStr;
                            }
                        } catch (Exception e) {}
                    }
                    Toast.makeText(FazerPedidoActivity.this, "Erro: " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(FazerPedidoActivity.this, "Erro de conexão.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}