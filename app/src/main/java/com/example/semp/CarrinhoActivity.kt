package com.example.semp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CarrinhoActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnMenu: ImageView
    private lateinit var recyclerViewCarrinho: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrinho)

        drawerLayout = findViewById(R.id.drawerLayoutCarrinho)
        btnMenu = findViewById(R.id.btnMenu)
        recyclerViewCarrinho = findViewById(R.id.recyclerViewCarrinho)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainContentLayout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        configurarNavegacaoMenu()

        // Configuração da Lista do Carrinho
        recyclerViewCarrinho.layoutManager = LinearLayoutManager(this)

        // TODO: Futuramente, chame a API com Retrofit aqui, como você fez no Login!
        // WHERE carrinho = '1'
        buscarItensCarrinhoAPI()
    }

    private fun buscarItensCarrinhoAPI() {
        // Simulação dos dados que viriam da API baseados na query do carrinho.kt
        val listaMockCarrinho = listOf(
            ItemEstoque("Parafuso Sextavado 10mm", "Qtd: 50", "No Carrinho", android.R.drawable.ic_menu_manage)
        )
        // Reutilizamos o mesmo Adapter do Estoque por enquanto
        recyclerViewCarrinho.adapter = EstoqueAdapter(listaMockCarrinho)
    }

    private fun configurarNavegacaoMenu() {
        findViewById<TextView>(R.id.menuItemEstoque).setOnClickListener {
            startActivity(Intent(this, EstoqueActivity::class.java))
            finish()
        }
        findViewById<TextView>(R.id.menuItemCarrinho).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<TextView>(R.id.menuItemPedido).setOnClickListener {
            startActivity(Intent(this, FazerPedidoActivity::class.java))
            finish()
        }
        findViewById<TextView>(R.id.menuItemSair).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}