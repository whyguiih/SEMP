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

        // LÓGICA DO SELETOR DE QUANTIDADE (INSPIRADA NO SCRIPT PHP/JS)
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
            // Aqui você deve futuramente chamar a sua API para gravar isso no banco
            Toast.makeText(this, "$quantidadeSelecionada x $nome adicionado(s) ao carrinho!", Toast.LENGTH_SHORT).show()
            finish() // Opcional: voltar para tela anterior após adicionar
        }
    }
}