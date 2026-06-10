package com.example.semp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
// IMPORTAÇÕES OBRIGATÓRIAS DO RETROFIT
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProdutoDetalheActivity : AppCompatActivity() {

    private var quantidadeSelecionada = 1
    private var estoqueMaximo = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produto_detalhe)

        // IMPLEMENTAÇÃO DO SCREEN VIEW (INSETS) PARA NÃO INVADIR O TOPO/BASE
        val mainView = findViewById<View>(R.id.mainContentLayoutDetalhe)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        btnVoltar.setOnClickListener { finish() }

        // Recebendo os dados enviados pela Intent
        val idProduto = intent.getStringExtra("PRODUTO_ID") ?: ""
        val nome = intent.getStringExtra("PRODUTO_NOME") ?: "Produto"
        val codigo = intent.getStringExtra("PRODUTO_CODIGO") ?: "Sem código"
        val desc = intent.getStringExtra("PRODUTO_DESC") ?: "Sem descrição disponível."
        val descDetalhada = intent.getStringExtra("PRODUTO_DESC_DETALHADA") ?: "Nenhuma descrição detalhada informada."
        val cor = intent.getStringExtra("PRODUTO_COR") ?: "Não especificada"
        val qtdString = intent.getStringExtra("PRODUTO_QTD") ?: "0"

        estoqueMaximo = qtdString.toIntOrNull() ?: 0

        // Vinculando e preenchendo as Views com os dados do Banco
        findViewById<TextView>(R.id.tvNomeDetalhe).text = nome
        findViewById<TextView>(R.id.tvCodigoDetalhe).text = "Código: $codigo"
        findViewById<TextView>(R.id.tvDescDetalhe).text = desc
        findViewById<TextView>(R.id.tvDescDetalhadaDetalhe).text = descDetalhada
        findViewById<TextView>(R.id.tvCorDetalhe).text = "Cor: $cor"
        findViewById<TextView>(R.id.tvEstoqueDetalhe).text = "Estoque disponível: $estoqueMaximo"

        // LÓGICA DO SELETOR DE QUANTIDADE
        val tvQtdSelecionada = findViewById<TextView>(R.id.tvQuantidadeSelecionada)
        val btnDiminuir = findViewById<TextView>(R.id.btnDiminuirQtd)
        val btnAumentar = findViewById<TextView>(R.id.btnAumentarQtd)
        val btnAdicionarCarrinho = findViewById<Button>(R.id.btnAdicionarCarrinho)

        // Se o estoque for 0, o botão de adicionar deverá começar em 0 e estar desativado
        if (estoqueMaximo == 0) {
            quantidadeSelecionada = 0
            tvQtdSelecionada.text = "0"
            btnAdicionarCarrinho.isEnabled = false
            btnAdicionarCarrinho.text = "Fora de Estoque"
        }

        btnDiminuir.setOnClickListener {
            if (quantidadeSelecionada > 1) {
                quantidadeSelecionada--
                tvQtdSelecionada.text = quantidadeSelecionada.toString()
            }
        }

        btnAumentar.setOnClickListener {
            if (quantidadeSelecionada < estoqueMaximo) {
                quantidadeSelecionada++
                tvQtdSelecionada.text = quantidadeSelecionada.toString()
            } else if (estoqueMaximo > 0) {
                Toast.makeText(this, "Você atingiu o limite do estoque!", Toast.LENGTH_SHORT).show()
            }
        }

        btnAdicionarCarrinho.setOnClickListener {
            if (idProduto.isEmpty()) {
                Toast.makeText(this, "Erro: Identificador do produto inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = CarrinhoRequest(id_produto = idProduto, quantidade = quantidadeSelecionada)

            // CHAMADA AO RETROFIT CORRIGIDA E SINCRONIZADA
            RetrofitClient.api.adicionarAoCarrinho(request).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    val body = response.body()

                    if (response.isSuccessful && body != null) {
                        if (body.sucesso) {
                            Toast.makeText(this@ProdutoDetalheActivity, "$quantidadeSelecionada x $nome inserido no carrinho!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@ProdutoDetalheActivity, body.mensagem ?: "Falha ao adicionar", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@ProdutoDetalheActivity, "Erro no servidor ao salvar item", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Toast.makeText(this@ProdutoDetalheActivity, "Sem conexão com a API: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}