package com.example.semp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
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
    private var listaOriginalProdutos: List<Produto> = listOf() // Armazena original para a busca

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_estoque)

            drawerLayout = findViewById(R.id.drawerLayout)
            val btnMenu = findViewById<ImageView>(R.id.btnMenu)
            val etPesquisa = findViewById<EditText>(R.id.etPesquisa)
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

            // BARRA DE PESQUISA EM TEMPO REAL
            etPesquisa?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val termo = s.toString().lowercase()
                    val filtrados = listaOriginalProdutos.filter {
                        it.nome?.lowercase()?.contains(termo) == true || it.codigo?.lowercase()?.contains(termo) == true
                    }
                    recyclerViewEstoque?.adapter = EstoqueAdapter(filtrados)
                }
            })

        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun buscarProdutosAPI(recyclerView: RecyclerView?) {
        if (recyclerView == null) return
        RetrofitClient.api.getProdutos().enqueue(object : Callback<List<Produto>> {
            override fun onResponse(call: Call<List<Produto>>, response: Response<List<Produto>>) {
                if (isDestroyed || isFinishing) return
                if (response.isSuccessful && response.body() != null) {
                    listaOriginalProdutos = response.body()!!
                    recyclerView.adapter = EstoqueAdapter(listaOriginalProdutos)
                }
            }
            override fun onFailure(call: Call<List<Produto>>, t: Throwable) {
                if (!isDestroyed && !isFinishing) Toast.makeText(this@EstoqueActivity, "Erro", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun configurarNavegacaoMenu() {
        val nivel = MainActivity.getNivelConta()

        val btnConfig = findViewById<TextView>(R.id.menuItemConfigEstoque)
        val btnAutorizar = findViewById<TextView>(R.id.menuItemAutorizar)
        val btnCadastrar = findViewById<TextView>(R.id.menuItemCadastrar)

        // LÓGICA DE DIFERENCIAÇÃO: 0 (Comum), 1 (Admin), 2 (Compras)
        btnConfig?.visibility = if (nivel == "1" || nivel == "2") View.VISIBLE else View.GONE
        btnAutorizar?.visibility = if (nivel == "2") View.VISIBLE else View.GONE
        btnCadastrar?.visibility = if (nivel == "1") View.VISIBLE else View.GONE

        findViewById<TextView>(R.id.menuItemEstoque)?.setOnClickListener { startActivity(Intent(this, EstoqueActivity::class.java)); finish() }
        findViewById<TextView>(R.id.menuItemCarrinho)?.setOnClickListener { startActivity(Intent(this, CarrinhoActivity::class.java)); finish() }
        findViewById<TextView>(R.id.menuItemPedido)?.setOnClickListener { startActivity(Intent(this, FazerPedidoActivity::class.java)); finish() }
        btnConfig?.setOnClickListener { startActivity(Intent(this, ConfigEstoqueActivity::class.java)); finish() }
        btnAutorizar?.setOnClickListener { startActivity(Intent(this, AutorizarPedidosActivity::class.java)); finish() }
        btnCadastrar?.setOnClickListener { startActivity(Intent(this, CadastrarUsuarioActivity::class.java)); finish() }
        findViewById<TextView>(R.id.menuItemSair)?.setOnClickListener { startActivity(Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }); finish() }
    }
}