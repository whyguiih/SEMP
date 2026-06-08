package com.example.semp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProdutoDetalheActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Você pode criar um layout 'activity_produto_detalhe.xml' simples para esta tela
        // Como exemplo rápido, usaremos o setContentView genérico,
        // mas você deve criar o XML para esta tela ficar bonita.
        setContentView(R.layout.activity_carrinho) // Trocando provisoriamente apenas para compilar, crie o layout correto depois

        val nome = intent.getStringExtra("PRODUTO_NOME") ?: "Produto Desconhecido"
        val desc = intent.getStringExtra("PRODUTO_DESC") ?: "Sem descrição"

        // Aqui você vincularia com os TextViews do seu activity_produto_detalhe.xml
        // Exemplo:
        // findViewById<TextView>(R.id.tvNomeDetalhe).text = nome
        // findViewById<TextView>(R.id.tvDescDetalhe).text = desc
    }
}