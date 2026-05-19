package com.example.semp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EstoqueActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnMenu: ImageView
    private lateinit var recyclerViewEstoque: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estoque)

        // Mapeando componentes
        drawerLayout = findViewById(R.id.drawerLayout)
        btnMenu = findViewById(R.id.btnMenu)
        recyclerViewEstoque = findViewById(R.id.recyclerViewEstoque)

        // Clicou no "S", aparece/some o menu lateral
        btnMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Lógica dos botões de dentro do menu lateral
        findViewById<TextView>(R.id.menuItemEstoque).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<TextView>(R.id.menuItemSair).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // Fecha todas as telas e volta pro Login
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Configurando a Lista do Estoque
        recyclerViewEstoque.layoutManager = LinearLayoutManager(this)

        // Criei esses dados mockados só para você ver os bloquinhos funcionando.
        // Aqui é onde sua ApiService vai injetar os dados puxados do db.sql!
        val listaMock = listOf(
            ItemEstoque("Parafuso Sextavado 10mm", "Qtd: 150", "Prateleira A"),
            ItemEstoque("Porca Borboleta", "Qtd: 300", "Prateleira B"),
            ItemEstoque("Chave Philips Média", "Qtd: 25", "Prateleira C")
        )

        recyclerViewEstoque.adapter = EstoqueAdapter(listaMock)
    }

    // Tratativa para fechar o menu no botão de voltar do celular, em vez de fechar o app
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}