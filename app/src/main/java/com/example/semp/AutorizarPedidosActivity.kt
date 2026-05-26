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

class AutorizarPedidosActivity : AppCompatActivity() {

    private var drawerLayout: DrawerLayout? = null
    private var recyclerViewPedidos: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autorizar_pedidos)

        drawerLayout = findViewById(R.id.drawerLayoutAutorizar)
        recyclerViewPedidos = findViewById(R.id.recyclerViewPedidos)

        findViewById<View>(R.id.mainContentLayout)?.let { mainView ->
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        findViewById<ImageView>(R.id.btnMenu)?.setOnClickListener {
            drawerLayout?.openDrawer(GravityCompat.START)
        }

        configurarNavegacaoMenu()

        recyclerViewPedidos?.layoutManager = LinearLayoutManager(this)
        buscarPedidosPendentes()
    }

    private fun buscarPedidosPendentes() {
        RetrofitClient.api.getPedidosPendentes().enqueue(object : Callback<List<PedidoPendente>> {
            override fun onResponse(call: Call<List<PedidoPendente>>, response: Response<List<PedidoPendente>>) {
                if (isDestroyed || isFinishing) return

                val lista = response.body()
                if (response.isSuccessful && lista != null) {
                    if(lista.isEmpty()) {
                        Toast.makeText(this@AutorizarPedidosActivity, "Não há pedidos pendentes.", Toast.LENGTH_SHORT).show()
                    }

                    recyclerViewPedidos?.adapter = PedidoAdapter(lista) { idEmprestimo, novoStatus ->
                        processarAutorizacao(idEmprestimo, novoStatus)
                    }
                }
            }

            override fun onFailure(call: Call<List<PedidoPendente>>, t: Throwable) {
                if (!isDestroyed && !isFinishing) {
                    Toast.makeText(this@AutorizarPedidosActivity, "Erro ao carregar pedidos", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun processarAutorizacao(idEmprestimo: Int, novoStatus: Int) {
        val request = AutorizarRequest(idEmprestimo, novoStatus)
        RetrofitClient.api.autorizarPedido(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                val acao = if (novoStatus == 1) "aprovado" else "recusado"
                Toast.makeText(this@AutorizarPedidosActivity, "Pedido $acao com sucesso!", Toast.LENGTH_SHORT).show()
                buscarPedidosPendentes() // Recarrega a lista para sumir o pedido
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@AutorizarPedidosActivity, "Erro ao processar", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun configurarNavegacaoMenu() {
        val nivel = MainActivity.getNivelConta()

        val btnConfig = findViewById<TextView>(R.id.menuItemConfigEstoque)
        val btnAutorizar = findViewById<TextView>(R.id.menuItemAutorizar)
        val btnCadastrar = findViewById<TextView>(R.id.menuItemCadastrar)

        btnConfig?.visibility = if (nivel == "1" || nivel == "2") View.VISIBLE else View.GONE
        btnAutorizar?.visibility = if (nivel == "2") View.VISIBLE else View.GONE
        btnCadastrar?.visibility = if (nivel == "1") View.VISIBLE else View.GONE

        findViewById<TextView>(R.id.menuItemEstoque)?.setOnClickListener { startActivity(Intent(this, EstoqueActivity::class.java)); finish() }
        findViewById<TextView>(R.id.menuItemCarrinho)?.setOnClickListener { startActivity(Intent(this, CarrinhoActivity::class.java)); finish() }
        findViewById<TextView>(R.id.menuItemPedido)?.setOnClickListener { startActivity(Intent(this, FazerPedidoActivity::class.java)); finish() }
        btnConfig?.setOnClickListener { startActivity(Intent(this, ConfigEstoqueActivity::class.java)); finish() }
        btnAutorizar?.setOnClickListener { drawerLayout?.closeDrawer(GravityCompat.START) }
        btnCadastrar?.setOnClickListener { startActivity(Intent(this, CadastrarUsuarioActivity::class.java)); finish() }
        findViewById<TextView>(R.id.menuItemSair)?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP })
            finish()
        }
    }
}