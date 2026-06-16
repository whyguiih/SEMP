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

        // SharedPreferences para prevenir erro caso minimize o app
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
        String idProduto = intent.getStringExtra("PRODUTO_ID") != null ? intent.getStringExtra("PRODUTO_ID") : "";
        String nome = intent.getStringExtra("PRODUTO_NOME") != null ? intent.getStringExtra("PRODUTO_NOME") : "Produto";
        String codigo = intent.getStringExtra("PRODUTO_CODIGO") != null ? intent.getStringExtra("PRODUTO_CODIGO") : "Sem código";
        String desc = intent.getStringExtra("PRODUTO_DESC") != null ? intent.getStringExtra("PRODUTO_DESC") : "Sem descrição disponível.";
        String descDetalhada = intent.getStringExtra("PRODUTO_DESC_DETALHADA") != null ? intent.getStringExtra("PRODUTO_DESC_DETALHADA") : "Nenhuma descrição detalhada informada.";
        String cor = intent.getStringExtra("PRODUTO_COR") != null ? intent.getStringExtra("PRODUTO_COR") : "Não especificada";
        String marca = intent.getStringExtra("PRODUTO_MARCA") != null ? intent.getStringExtra("PRODUTO_MARCA") : "Não especificada";
        String uniNatal = intent.getStringExtra("PRODUTO_UNI_NATAL") != null ? intent.getStringExtra("PRODUTO_UNI_NATAL") : "Não especificada";
        String unidadeAtual = intent.getStringExtra("PRODUTO_UNIDADE_ATUAL") != null ? intent.getStringExtra("PRODUTO_UNIDADE_ATUAL") : "Não especificada";
        String fotoBase64 = intent.getStringExtra("PRODUTO_FOTO") != null ? intent.getStringExtra("PRODUTO_FOTO") : "";
        String qtdEstoqueString = intent.getStringExtra("PRODUTO_QTD") != null ? intent.getStringExtra("PRODUTO_QTD") : "0";
        String qtdCarrinhoString = intent.getStringExtra("PRODUTO_QTD_CARRINHO") != null ? intent.getStringExtra("PRODUTO_QTD_CARRINHO") : "1";

        try { estoqueMaximo = Integer.parseInt(qtdEstoqueString); } catch (NumberFormatException e) { estoqueMaximo = 0; }
        try { quantidadeSelecionada = Integer.parseInt(qtdCarrinhoString); } catch (NumberFormatException e) { quantidadeSelecionada = 1; }

        ((TextView) findViewById(R.id.tvNomeDetalhe)).setText(nome);
        ((TextView) findViewById(R.id.tvCodigoDetalhe)).setText("Código: " + codigo);
        ((TextView) findViewById(R.id.tvDescDetalhe)).setText(desc);
        ((TextView) findViewById(R.id.tvDescDetalhadaDetalhe)).setText(descDetalhada);
        ((TextView) findViewById(R.id.tvCorDetalhe)).setText("Cor: " + cor);
        ((TextView) findViewById(R.id.tvMarcaRefDetalhe)).setText("Marca/Ref: " + marca);
        ((TextView) findViewById(R.id.tvUniNatalDetalhe)).setText("Unidade Natal: " + uniNatal);
        ((TextView) findViewById(R.id.tvUnidadeAtualDetalhe)).setText("Unidade Atual: " + unidadeAtual);
        ((TextView) findViewById(R.id.tvEstoqueDetalhe)).setText("Estoque disponível: " + estoqueMaximo);

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
            if (intent.hasExtra("PRODUTO_QTD_CARRINHO")) {
                btnAdicionarCarrinho.setText("Atualizar Carrinho");
            }
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
            } else if (estoqueMaximo > 0) {
                Toast.makeText(this, "Você atingiu o limite do estoque!", Toast.LENGTH_SHORT).show();
            }
        });

        btnAdicionarCarrinho.setOnClickListener(v -> {
            if (usuarioSeguro == null || usuarioSeguro.isEmpty()) {
                Toast.makeText(ProdutoDetalheActivity.this, "Erro: Faça o login novamente!", Toast.LENGTH_LONG).show();
                return;
            }

            int qtdDigitada = lerQuantidadeDoCampo(tvQtdSelecionada);
            quantidadeSelecionada = Math.min(qtdDigitada, estoqueMaximo);

            btnAdicionarCarrinho.setEnabled(false);
            btnAdicionarCarrinho.setText("Aguarde...");

            CarrinhoRequest request = new CarrinhoRequest(nome, quantidadeSelecionada);
            boolean ehEdicao = getIntent().hasExtra("PRODUTO_QTD_CARRINHO");

            if (ehEdicao) {
                RetrofitClient.getApi().removerDoCarrinho(usuarioSeguro, request).enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                        adicionarNovoAoCarrinho(usuarioSeguro, request, btnAdicionarCarrinho);
                    }
                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {
                        adicionarNovoAoCarrinho(usuarioSeguro, request, btnAdicionarCarrinho);
                    }
                });
            } else {
                adicionarNovoAoCarrinho(usuarioSeguro, request, btnAdicionarCarrinho);
            }
        });
    }

    private void adicionarNovoAoCarrinho(String usuarioId, CarrinhoRequest request, Button btn) {
        RetrofitClient.getApi().adicionarAoCarrinho(usuarioId, request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().sucesso) {
                    Toast.makeText(ProdutoDetalheActivity.this, "Carrinho atualizado!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btn.setEnabled(true);
                    btn.setText("Tentar Novamente");
                    Toast.makeText(ProdutoDetalheActivity.this, "Erro ao atualizar carrinho", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                btn.setEnabled(true);
                btn.setText("Tentar Novamente");
                Toast.makeText(ProdutoDetalheActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int lerQuantidadeDoCampo(EditText et) {
        try {
            return Integer.parseInt(et.getText().toString());
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}