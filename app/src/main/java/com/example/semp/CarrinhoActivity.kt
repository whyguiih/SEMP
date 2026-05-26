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

        recyclerViewCarrinho.layoutManager = LinearLayoutManager(this)

        buscarItensCarrinhoAPI()
    }

    private fun buscarItensCarrinhoAPI() {
        RetrofitClient.api.getCarrinho().enqueue(object : Callback<List<Produto>> {
            override fun onResponse(call: Call<List<Produto>>, response: Response<List<Produto>>) {
                if (response.isSuccessful && response.body() != null) {
                    val itensCarrinho = response.body()!!
                    recyclerViewCarrinho.adapter = EstoqueAdapter(itensCarrinho)
                } else {
                    Toast.makeText(this@CarrinhoActivity, "Erro ao carregar carrinho", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Produto>>, t: Throwable) {
                Toast.makeText(this@CarrinhoActivity, "Erro de conexão: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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