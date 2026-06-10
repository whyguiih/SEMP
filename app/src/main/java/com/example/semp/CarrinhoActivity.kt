package com.example.semp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
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
        setContentView(R.layout.activity_carrinho)

        // Inicializa os IDs mapeados perfeitamente do novo XML
        drawerLayout = findViewById(R.id.drawerLayoutCarrinho)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        val recyclerViewCarrinho = findViewById<RecyclerView>(R.id.recyclerViewCarrinho)
        val btnFinalizarPedido = findViewById<Button>(R.id.btnFinalizarPedido)

        // LISTENER DE TELA (Evita invasão do topo e base do celular)
        findViewById<View>(R.id.mainContentLayout)?.let { mainView ->
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        btnMenu?.setOnClickListener { drawerLayout?.openDrawer(GravityCompat.START) }

        // Redireciona o botão inferior para a página de Fazer Pedido
        btnFinalizarPedido?.setOnClickListener {
            val intent = Intent(this@CarrinhoActivity, FazerPedidoActivity::class.java)
            startActivity(intent)
        }

        configurarNavegacaoMenu()

        recyclerViewCarrinho?.layoutManager = LinearLayoutManager(this)
        buscarItensCarrinhoAPI(recyclerViewCarrinho)
    }

    private fun buscarItensCarrinhoAPI(recyclerView: RecyclerView?) {
        if (recyclerView == null) return
        val tvCarrinhoVazio = findViewById<TextView>(R.id.tvCarrinhoVazio)
        val btnFinalizarPedido = findViewById<Button>(R.id.btnFinalizarPedido)

        RetrofitClient.api.getCarrinho().enqueue(object : Callback<List<Produto>> {
            override fun onResponse(call: Call<List<Produto>>, response: Response<List<Produto>>) {
                if (isDestroyed || isFinishing) return

                if (response.isSuccessful && response.body() != null) {
                    val itensCarrinho = response.body()!!

                    // CONTROLADOR DE ESTADO VISUAL DO CARRINHO
                    if (itensCarrinho.isEmpty()) {
                        recyclerView.visibility = View.GONE
                        tvCarrinhoVazio?.visibility = View.VISIBLE
                        btnFinalizarPedido?.visibility = View.GONE // Oculta o botão se não houver itens
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        tvCarrinhoVazio?.visibility = View.GONE
                        btnFinalizarPedido?.visibility = View.VISIBLE

                        recyclerView.adapter = EstoqueAdapter(itensCarrinho) { produto ->
                            val intent = Intent(this@CarrinhoActivity, ProdutoDetalheActivity::class.java)
                            intent.putExtra("PRODUTO_ID", produto.id_estoque?.toString() ?: "")
                            intent.putExtra("PRODUTO_NOME", produto.nome)
                            intent.putExtra("PRODUTO_CODIGO", produto.codigo)
                            intent.putExtra("PRODUTO_DESC", produto.descricao)
                            intent.putExtra("PRODUTO_QTD", produto.quant)
                            startActivity(intent)
                        }
                    }
                } else {
                    recyclerView.visibility = View.GONE
                    tvCarrinhoVazio?.visibility = View.VISIBLE
                    btnFinalizarPedido?.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<List<Produto>>, t: Throwable) {
                if (!isDestroyed && !isFinishing) {
                    Toast.makeText(this@CarrinhoActivity, "Sem conexão com o servidor", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun configurarNavegacaoMenu() {
        val nivel = MainActivity.getNivelConta()

        val btnConfigEstoque = findViewById<TextView>(R.id.menuItemConfigEstoque)
        val btnAutorizar = findViewById<TextView>(R.id.menuItemAutorizar)
        val btnConfigAcesso = findViewById<TextView>(R.id.menuItemConfigAcesso)
        val btnEmprestimo = findViewById<TextView>(R.id.menuItemEmprestimo)
        val btnVisualizarPedido = findViewById<TextView>(R.id.menuItemVisualizarPedido)

        btnConfigEstoque?.visibility = if (nivel == "1" || nivel == "2") View.VISIBLE else View.GONE
        btnAutorizar?.visibility = if (nivel == "2") View.VISIBLE else View.GONE
        btnConfigAcesso?.visibility = if (nivel == "1") View.VISIBLE else View.GONE

        findViewById<TextView>(R.id.menuItemEstoque)?.setOnClickListener { startActivity(Intent(this@CarrinhoActivity, EstoqueActivity::class.java)); finish() }
        findViewById<TextView>(R.id.menuItemCarrinho)?.setOnClickListener { drawerLayout?.closeDrawer(GravityCompat.START) }

        btnConfigEstoque?.setOnClickListener { startActivity(Intent(this@CarrinhoActivity, ConfigEstoqueActivity::class.java)); finish() }
        btnAutorizar?.setOnClickListener { startActivity(Intent(this@CarrinhoActivity, AutorizarPedidosActivity::class.java)); finish() }
        btnConfigAcesso?.setOnClickListener { startActivity(Intent(this@CarrinhoActivity, CadastrarUsuarioActivity::class.java)); finish() }

        btnEmprestimo?.setOnClickListener { Toast.makeText(this, "Empréstimo em breve", Toast.LENGTH_SHORT).show() }
        btnVisualizarPedido?.setOnClickListener { Toast.makeText(this, "Visualizar Pedidos em breve", Toast.LENGTH_SHORT).show() }

        findViewById<TextView>(R.id.menuItemSair)?.setOnClickListener {
            val intent = Intent(this@CarrinhoActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}