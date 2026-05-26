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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EstoqueActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnMenu: ImageView
    private lateinit var recyclerViewEstoque: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estoque)

        drawerLayout = findViewById(R.id.drawerLayout)
        btnMenu = findViewById(R.id.btnMenu)
        recyclerViewEstoque = findViewById(R.id.recyclerViewEstoque)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainContentLayout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        configurarNavegacaoMenu()

        recyclerViewEstoque.layoutManager = LinearLayoutManager(this)

        // Busca os dados da API
        buscarProdutosAPI()
    }

    private fun buscarProdutosAPI() {
        RetrofitClient.api.getProdutos().enqueue(object : Callback<List<Produto>> {
            override fun onResponse(call: Call<List<Produto>>, response: Response<List<Produto>>) {
                if (response.isSuccessful && response.body() != null) {
                    val produtosDaApi = response.body()!!
                    recyclerViewEstoque.adapter = EstoqueAdapter(produtosDaApi)
                } else {
                    Toast.makeText(this@EstoqueActivity, "Erro ao buscar produtos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Produto>>, t: Throwable) {
                Toast.makeText(this@EstoqueActivity, "Erro de conexão: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun configurarNavegacaoMenu() {
        findViewById<TextView>(R.id.menuItemEstoque).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<TextView>(R.id.menuItemCarrinho).setOnClickListener {
            startActivity(Intent(this, CarrinhoActivity::class.java))
            finish()
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

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}