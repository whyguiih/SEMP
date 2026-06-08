package com.example.semp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProdutoDetalheActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produto_detalhe) // Aponta para o XML correto

        // Botão de voltar
        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            finish() // Fecha esta tela e volta para a anterior (Estoque ou Carrinho)
        }

        // Recupera os dados enviados pela tela de Estoque
        val nome = intent.getStringExtra("PRODUTO_NOME") ?: "Produto Desconhecido"
        val desc = intent.getStringExtra("PRODUTO_DESC") ?: "Sem descrição disponível."
        val qtd = intent.getStringExtra("PRODUTO_QTD") ?: "0"

        // Preenche as informações na tela
        findViewById<TextView>(R.id.tvNomeDetalhe).text = nome
        findViewById<TextView>(R.id.tvDescDetalhe).text = desc
        findViewById<TextView>(R.id.tvQtdDetalhe).text = "Quantidade em estoque: $qtd"
    }
}