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

class EstoqueActivity : AppCompatActivity() {

    private var drawerLayout: DrawerLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Try-Catch previne fechamentos forçados caso algum layout XML esteja incompleto
        try {
            setContentView(R.layout.activity_estoque)

            drawerLayout = findViewById(R.id.drawerLayout)
            val btnMenu = findViewById<ImageView>(R.id.btnMenu)
            val recyclerViewEstoque = findViewById<RecyclerView>(R.id.recyclerViewEstoque)

            findViewById<View>(R.id.mainContentLayout)?.let { mainView ->
                ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }
            }

            btnMenu?.setOnClickListener {
                drawerLayout?.let {
                    if (it.isDrawerOpen(GravityCompat.START)) it.closeDrawer(GravityCompat.START)
                    else it.openDrawer(GravityCompat.START)
                }
            }

            configurarNavegacaoMenu()

            recyclerViewEstoque?.layoutManager = LinearLayoutManager(this)
            buscarProdutosAPI(recyclerViewEstoque)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro de interface: verifique o XML do Estoque", Toast.LENGTH_LONG).show()
        }
    }

    private fun buscarProdutosAPI(recyclerView: RecyclerView?) {
        if (recyclerView == null) return

        RetrofitClient.api.getProdutos().enqueue(object : Callback<List<Produto>> {
            override fun onResponse(call: Call<List<Produto>>, response: Response<List<Produto>>) {
                // Checagem crucial: Se a tela foi fechada enquanto a API carregava, ignora a resposta!
                if (isDestroyed || isFinishing) return

                if (response.isSuccessful && response.body() != null) {
                    val produtosDaApi = response.body()!!
                    recyclerView.adapter = EstoqueAdapter(produtosDaApi)
                } else {
                    Toast.makeText(this@EstoqueActivity, "Nenhum produto encontrado", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Produto>>, t: Throwable) {
                if (!isDestroyed && !isFinishing) {
                    Toast.makeText(this@EstoqueActivity, "Erro de conexão", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun configurarNavegacaoMenu() {
        // Uso de Null Safety (?.) caso o item não exista no Menu XML copiado
        findViewById<TextView>(R.id.menuItemEstoque)?.setOnClickListener {
            drawerLayout?.closeDrawer(GravityCompat.START)
        }
        findViewById<TextView>(R.id.menuItemCarrinho)?.setOnClickListener {
            startActivity(Intent(this, CarrinhoActivity::class.java))
            finish()
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

    override fun onBackPressed() {
        if (drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}