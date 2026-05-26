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

class CadastrarUsuarioActivity : AppCompatActivity() {
    private var drawerLayout: DrawerLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar_usuario)

        drawerLayout = findViewById(R.id.drawerLayoutCad)

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

        val etUsu = findViewById<EditText>(R.id.etNovoUsuario)
        val etSenha = findViewById<EditText>(R.id.etNovaSenha)
        val etNivel = findViewById<EditText>(R.id.etNivel)
        val etUni = findViewById<EditText>(R.id.etUni)

        findViewById<Button>(R.id.btnRegistrar)?.setOnClickListener {
            val req = UsuarioRequest(
                etUsu.text.toString(),
                etSenha.text.toString(),
                etNivel.text.toString(),
                etUni.text.toString()
            )
            RetrofitClient.api.cadastrarUsuario(req).enqueue(object: Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, r: Response<GenericResponse>) {
                    Toast.makeText(applicationContext, r.body()?.mensagem, Toast.LENGTH_SHORT).show()
                    if (r.body()?.sucesso == true) {
                        etUsu.text.clear()
                        etSenha.text.clear()
                        etNivel.text.clear()
                        etUni.text.clear()
                    }
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
        btnConfig?.setOnClickListener { startActivity(Intent(this, ConfigEstoqueActivity::class.java)); finish() }
        btnAutorizar?.setOnClickListener { startActivity(Intent(this, AutorizarPedidosActivity::class.java)); finish() }
        btnCadastrar?.setOnClickListener { drawerLayout?.closeDrawer(GravityCompat.START) }
        findViewById<TextView>(R.id.menuItemSair)?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP })
            finish()
        }
    }
}