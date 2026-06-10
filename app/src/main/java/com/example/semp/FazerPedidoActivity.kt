package com.example.semp

import android.app.DatePickerDialog
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
import java.util.Calendar

class FazerPedidoActivity : AppCompatActivity() {

    private var drawerLayout: DrawerLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_fazer_pedido)

            drawerLayout = findViewById(R.id.drawerLayoutPedido) ?: findViewById(R.id.drawerLayout)
            val btnMenu = findViewById<ImageView>(R.id.btnMenu)
            val etNome = findViewById<EditText>(R.id.etNomePedido)
            val etEmail = findViewById<EditText>(R.id.etEmailPedido)
            val etData = findViewById<EditText>(R.id.etDataPedido)
            val btnConfirmar = findViewById<Button>(R.id.btnConfirmarPedido)

            findViewById<View>(R.id.mainContentLayout)?.let { mainView ->
                ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }
            }

            btnMenu?.setOnClickListener { drawerLayout?.openDrawer(GravityCompat.START) }
            configurarNavegacaoMenu()

            etData?.setOnClickListener {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                    val dataFormatada = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                    etData.setText(dataFormatada)
                }, year, month, day).show()
            }

            btnConfirmar?.setOnClickListener {
                val nome = etNome?.text.toString()
                val email = etEmail?.text.toString()
                val data = etData?.text.toString()
                val unidade = MainActivity.getUnidade()

                if (nome.isEmpty() || email.isEmpty() || data.isEmpty()) {
                    Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                enviarPedidoParaAPI(nome, email, unidade, data, etNome, etEmail, etData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun enviarPedidoParaAPI(nome: String, email: String, unidade: String, data: String, etNome: EditText?, etEmail: EditText?, etData: EditText?) {
        val request = PedidoRequest(nome, email, unidade, data)

        RetrofitClient.api.fazerPedido(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (isDestroyed || isFinishing) return // Bloqueia crash se a tela foi fechada

                val body = response.body()
                if (response.isSuccessful && body != null && body.sucesso) {
                    Toast.makeText(this@FazerPedidoActivity, "Dados inseridos com sucesso!", Toast.LENGTH_SHORT).show()
                    etNome?.text?.clear()
                    etEmail?.text?.clear()
                    etData?.text?.clear()
                } else {
                    Toast.makeText(this@FazerPedidoActivity, body?.mensagem ?: "Erro ao registrar o pedido", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                if (!isDestroyed && !isFinishing) {
                    Toast.makeText(this@FazerPedidoActivity, "Erro de conexão com servidor", Toast.LENGTH_LONG).show()
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

        // Limita o acesso dependendo da conta do usuário
        btnConfigEstoque?.visibility = if (nivel == "1" || nivel == "2") View.VISIBLE else View.GONE
        btnAutorizar?.visibility = if (nivel == "2") View.VISIBLE else View.GONE
        btnConfigAcesso?.visibility = if (nivel == "1") View.VISIBLE else View.GONE

        findViewById<TextView>(R.id.menuItemEstoque)?.setOnClickListener { startActivity(Intent(this, EstoqueActivity::class.java)); finish() }
        findViewById<TextView>(R.id.menuItemCarrinho)?.setOnClickListener { startActivity(Intent(this, CarrinhoActivity::class.java)); finish() }

        btnConfigEstoque?.setOnClickListener { startActivity(Intent(this, ConfigEstoqueActivity::class.java)); finish() }
        btnAutorizar?.setOnClickListener { startActivity(Intent(this, AutorizarPedidosActivity::class.java)); finish() }
        btnConfigAcesso?.setOnClickListener { startActivity(Intent(this, CadastrarUsuarioActivity::class.java)); finish() }

        btnEmprestimo?.setOnClickListener { Toast.makeText(this, "Empréstimo em breve", Toast.LENGTH_SHORT).show() }
        btnVisualizarPedido?.setOnClickListener { Toast.makeText(this, "Visualização de Pedidos em breve", Toast.LENGTH_SHORT).show() }

        findViewById<TextView>(R.id.menuItemSair)?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }); finish()
        }
    }
}