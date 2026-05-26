package com.example.semp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConfigEstoqueActivity : AppCompatActivity() {

    private var drawerLayout: DrawerLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_estoque)

        drawerLayout = findViewById(R.id.drawerLayoutConfig)

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

        val etId = findViewById<EditText>(R.id.etIdAtualizar)
        val etColuna = findViewById<EditText>(R.id.etColuna)
        val etValor = findViewById<EditText>(R.id.etNovoValor)
        val etCodigoDel = findViewById<EditText>(R.id.etCodigoDeletar)

        findViewById<Button>(R.id.btnAtualizar)?.setOnClickListener {
            val id = etId.text.toString().toIntOrNull()
            if (id != null) {
                val req = UpdateProdutoRequest(id, etColuna.text.toString(), etValor.text.toString())
                RetrofitClient.api.atualizarProduto(req).enqueue(object: Callback<GenericResponse> {
                    override fun onResponse(call: Call<GenericResponse>, r: Response<GenericResponse>) {
                        Toast.makeText(applicationContext, r.body()?.mensagem, Toast.LENGTH_SHORT).show()
                    }
                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                        Toast.makeText(applicationContext, "Erro de conexão", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "ID inválido", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnDeletar)?.setOnClickListener {
            val req = DeleteProdutoRequest(etCodigoDel.text.toString())
            RetrofitClient.api.deletarProduto(req).enqueue(object: Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, r: Response<GenericResponse>) {
                    Toast.makeText(applicationContext, r.body()?.mensagem, Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "Erro de conexão", Toast.LENGTH_SHORT).show()
                }
            })
        }
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
        btnConfig?.setOnClickListener { drawerLayout?.closeDrawer(GravityCompat.START) }
        btnAutorizar?.setOnClickListener { startActivity(Intent(this, AutorizarPedidosActivity::class.java)); finish() }
        btnCadastrar?.setOnClickListener { startActivity(Intent(this, CadastrarUsuarioActivity::class.java)); finish() }
        findViewById<TextView>(R.id.menuItemSair)?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP })
            finish()
        }
    }
}