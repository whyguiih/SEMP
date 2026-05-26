package com.example.semp

import android.content.Intent
import android.os.Bundle
import android.view.View
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

    private var drawerLayout: DrawerLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_carrinho)

            // Procura o id original e o genérico por segurança
            drawerLayout = findViewById(R.id.drawerLayoutCarrinho) ?: findViewById(R.id.drawerLayout)
            val btnMenu = findViewById<ImageView>(R.id.btnMenu)
            val recyclerViewCarrinho = findViewById<RecyclerView>(R.id.recyclerViewCarrinho)

            findViewById<View>(R.id.mainContentLayout)?.let { mainView ->
                ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }
            }

            btnMenu?.setOnClickListener { drawerLayout?.openDrawer(GravityCompat.START) }

            configurarNavegacaoMenu()

            recyclerViewCarrinho?.layoutManager = LinearLayoutManager(this)
            buscarItensCarrinhoAPI(recyclerViewCarrinho)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buscarItensCarrinhoAPI(recyclerView: RecyclerView?) {
        if (recyclerView == null) return

        RetrofitClient.api.getCarrinho().enqueue(object : Callback<List<Produto>> {
            override fun onResponse(call: Call<List<Produto>>, response: Response<List<Produto>>) {
                if (isDestroyed || isFinishing) return // Bloqueia crash se a tela foi fechada

                if (response.isSuccessful && response.body() != null) {
                    val itensCarrinho = response.body()!!
                    recyclerView.adapter = EstoqueAdapter(itensCarrinho)
                } else {
                    Toast.makeText(this@CarrinhoActivity, "Sem produtos no carrinho", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Produto>>, t: Throwable) {
                if (!isDestroyed && !isFinishing) {
                    Toast.makeText(this@CarrinhoActivity, "Sem conexão com o banco de dados", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun configurarNavegacaoMenu() {
        findViewById<TextView>(R.id.menuItemEstoque)?.setOnClickListener {
            startActivity(Intent(this, EstoqueActivity::class.java))
            finish()
        }
        findViewById<TextView>(R.id.menuItemCarrinho)?.setOnClickListener {
            drawerLayout?.closeDrawer(GravityCompat.START)
        }
        findViewById<TextView>(R.id.menuItemPedido)?.setOnClickListener {
            startActivity(Intent(this, FazerPedidoActivity::class.java))
            finish()
        }
        findViewById<TextView>(R.id.menuItemSair)?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}