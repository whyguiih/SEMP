package com.example.continuacao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TelaPedidoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Precisará de um res/layout/activity_tela_pedido.xml simples (Texto de Sucesso e Botão)
        setContentView(R.layout.activity_tela_pedido); 

        TextView tvMensagem = findViewById(R.id.tvMensagemSucesso);
        tvMensagem.setText("Pedido realizado com sucesso!\nAcompanhe em 'Visualizar Pedidos'.");

        Button btnVoltarInicio = findViewById(R.id.btnVoltarInicio);
        btnVoltarInicio.setOnClickListener(v -> {
            Intent intent = new Intent(TelaPedidoActivity.this, EstoqueActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
