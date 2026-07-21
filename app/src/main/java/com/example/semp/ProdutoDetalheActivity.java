package com.example.semp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.semp.models.CarrinhoRequest;
import com.example.semp.models.GenericResponse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProdutoDetalheActivity extends AppCompatActivity {

    private int quantidadeSelecionada = 1;
    private int estoqueMaximo = 0;
    private String usuarioSeguro = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produto_detalhe);

        SharedPreferences prefs = getSharedPreferences("SessaoApp", Context.MODE_PRIVATE);
        usuarioSeguro = prefs.getString("usuarioLogado", "");

        View mainView = findViewById(R.id.mainContentLayoutDetalhe);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        ImageView btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        String nome = intent.getStringExtra("PRODUTO_NOME") != null ? intent.getStringExtra("PRODUTO_NOME") : "Produto";
        String codigo = intent.getStringExtra("PRODUTO_CODIGO") != null ? intent.getStringExtra("PRODUTO_CODIGO") : "Sem código";
        String desc = intent.getStringExtra("PRODUTO_DESC") != null ? intent.getStringExtra("PRODUTO_DESC") : "Sem descrição.";
        String descDetalhada = intent.getStringExtra("PRODUTO_DESC_DETALHADA") != null ? intent.getStringExtra("PRODUTO_DESC_DETALHADA") : "Sem detalhes.";
        String cor = intent.getStringExtra("PRODUTO_COR") != null ? intent.getStringExtra("PRODUTO_COR") : "N/A";
        String marca = intent.getStringExtra("PRODUTO_MARCA") != null ? intent.getStringExtra("PRODUTO_MARCA") : "N/A";
        String uniNatal = intent.getStringExtra("PRODUTO_UNI_NATAL") != null ? intent.getStringExtra("PRODUTO_UNI_NATAL") : "N/A";
        String unidadeAtual = intent.getStringExtra("PRODUTO_UNIDADE_ATUAL") != null ? intent.getStringExtra("PRODUTO_UNIDADE_ATUAL") : "N/A";
        String fotoBase64 = intent.getStringExtra("PRODUTO_FOTO") != null ? intent.getStringExtra("PRODUTO_FOTO") : "";
        String qtdCarrinhoString = intent.getStringExtra("PRODUTO_QTD_CARRINHO") != null ? intent.getStringExtra("PRODUTO_QTD_CARRINHO") : "1";
        String altura = intent.getStringExtra("PRODUTO_ALTURA") != null ? intent.getStringExtra("PRODUTO_ALTURA") : "N/A";
        String comprimento = intent.getStringExtra("PRODUTO_COMPRIMENTO") != null ? intent.getStringExtra("PRODUTO_COMPRIMENTO") : "N/A";
        
        // Pega o estoque real calculado pelo Worker
        String qtdEstoqueString = intent.getStringExtra("PRODUTO_QTD_REAL") != null ? intent.getStringExtra("PRODUTO_QTD_REAL") : intent.getStringExtra("PRODUTO_QTD");

        try { estoqueMaximo = Integer.parseInt(qtdEstoqueString); } catch (Exception e) { estoqueMaximo = 0; }
        try { quantidadeSelecionada = Integer.parseInt(qtdCarrinhoString); } catch (Exception e) { quantidadeSelecionada = 1; }

        ((TextView) findViewById(R.id.tvNomeDetalhe)).setText(nome);
        ((TextView) findViewById(R.id.tvCodigoDetalhe)).setText("Código: " + codigo);
        ((TextView) findViewById(R.id.tvDescDetalhe)).setText(desc);
        ((TextView) findViewById(R.id.tvDescDetalhadaDetalhe)).setText(descDetalhada);
        ((TextView) findViewById(R.id.tvCorDetalhe)).setText("Cor: " + cor);
        ((TextView) findViewById(R.id.tvMarcaRefDetalhe)).setText("Marca: " + marca);
        ((TextView) findViewById(R.id.tvUniNatalDetalhe)).setText("Unidade Natal: " + uniNatal);
        ((TextView) findViewById(R.id.tvUnidadeAtualDetalhe)).setText("Unidade Atual: " + unidadeAtual);
        
        // MOSTRA STATUS E UNIDADES JUNTOS
        TextView tvReserva = findViewById(R.id.PeriodoReserva);
        if (estoqueMaximo > 0) {
            tvReserva.setText("STATUS: DISPONÍVEL (" + estoqueMaximo + " unidades agora)");
            tvReserva.setTextColor(android.graphics.Color.parseColor("#27ae60")); // Verde
        } else {
            tvReserva.setText("STATUS: INDISPONÍVEL HOJE");
            tvReserva.setTextColor(android.graphics.Color.parseColor("#e74c3c")); // Vermelho
        }

        // Mostra o estoque total (fixo do cadastro)
        String estoqueTotal = intent.getStringExtra("PRODUTO_QTD") != null ? intent.getStringExtra("PRODUTO_QTD") : "0";
        ((TextView) findViewById(R.id.tvEstoqueDetalhe)).setText("Estoque total da unidade: " + estoqueTotal);

        ((TextView) findViewById(R.id.AlturaDetalhe)).setText("Altura: " + altura + "cm");
        ((TextView) findViewById(R.id.ComprimentoDetalhe)).setText("Comprimento: " + comprimento + "cm");

        ImageView ivFoto = findViewById(R.id.ivProdutoFoto);
        if (ivFoto != null && !fotoBase64.isEmpty()) {
            try {
                String pureBase64 = fotoBase64;
                if (pureBase64.contains(",")) pureBase64 = pureBase64.split(",")[1];
                byte[] decodedString = android.util.Base64.decode(pureBase64, android.util.Base64.DEFAULT);
                android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivFoto.setImageBitmap(decodedByte);
            } catch (Exception e) {
                ivFoto.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        EditText tvQtdSelecionada = findViewById(R.id.tvQuantidadeSelecionada);
        TextView btnDiminuir = findViewById(R.id.btnDiminuirQtd);
        TextView btnAumentar = findViewById(R.id.btnAumentarQtd);
        Button btnAdicionarCarrinho = findViewById(R.id.btnAdicionarCarrinho);

        if (estoqueMaximo == 0) {
            quantidadeSelecionada = 0;
            tvQtdSelecionada.setText("0");
            btnAdicionarCarrinho.setEnabled(false);
            btnAdicionarCarrinho.setText("Fora de Estoque");
        } else {
            tvQtdSelecionada.setText(String.valueOf(quantidadeSelecionada));
        }

        btnDiminuir.setOnClickListener(v -> {
            int atual = lerQuantidadeDoCampo(tvQtdSelecionada);
            if (atual > 1) {
                quantidadeSelecionada = atual - 1;
                tvQtdSelecionada.setText(String.valueOf(quantidadeSelecionada));
            }
        });

        btnAumentar.setOnClickListener(v -> {
            int atual = lerQuantidadeDoCampo(tvQtdSelecionada);
            if (atual < estoqueMaximo) {
                quantidadeSelecionada = atual + 1;
                tvQtdSelecionada.setText(String.valueOf(quantidadeSelecionada));
            } else {
                Toast.makeText(this, "Limite atingido", Toast.LENGTH_SHORT).show();
            }
        });

        btnAdicionarCarrinho.setOnClickListener(v -> {
            if (usuarioSeguro.isEmpty()) {
                Toast.makeText(this, "Faça login!", Toast.LENGTH_LONG).show();
                return;
            }
            CarrinhoRequest request = new CarrinhoRequest(nome, lerQuantidadeDoCampo(tvQtdSelecionada));
            RetrofitClient.getApi().adicionarAoCarrinho(usuarioSeguro, request).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ProdutoDetalheActivity.this, "Adicionado!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                @Override public void onFailure(Call<GenericResponse> call, Throwable t) {}
            });
        });
    }

    private int lerQuantidadeDoCampo(EditText et) {
        try { return Integer.parseInt(et.getText().toString()); } catch (Exception e) { return 1; }
    }
}