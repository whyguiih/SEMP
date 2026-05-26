package com.example.semp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
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

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fazer_pedido)

        drawerLayout = findViewById(R.id.drawerLayoutPedido)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        val etNome = findViewById<EditText>(R.id.etNomePedido)
        val etEmail = findViewById<EditText>(R.id.etEmailPedido)
        val etData = findViewById<EditText>(R.id.etDataPedido)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarPedido)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainContentLayout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
        configurarNavegacaoMenu()

        etData.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val dataFormatada = "$selectedYear-${selectedMonth + 1}-$selectedDay" // Formato SQL (YYYY-MM-DD)
                etData.setText(dataFormatada)
            }, year, month, day).show()
        }

        btnConfirmar.setOnClickListener {
            val nome = etNome.text.toString()
            val email = etEmail.text.toString()
            val data = etData.text.toString()
            val unidade = MainActivity.getUnidade()

            if (nome.isEmpty() || email.isEmpty() || data.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            enviarPedidoParaAPI(nome, email, unidade, data, etNome, etEmail, etData)
        }
    }

    private fun enviarPedidoParaAPI(nome: String, email: String, unidade: String, data: String, etNome: EditText, etEmail: EditText, etData: EditText) {
        val request = PedidoRequest(nome, email, unidade, data)

        RetrofitClient.api.fazerPedido(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                val body = response.body()
                if (response.isSuccessful && body != null && body.sucesso) {
                    Toast.makeText(this@FazerPedidoActivity, "Dados inseridos com sucesso!", Toast.LENGTH_SHORT).show()
                    etNome.text.clear()
                    etEmail.text.clear()
                    etData.text.clear()
                } else {
                    Toast.makeText(this@FazerPedidoActivity, body?.mensagem ?: "Erro ao registrar o pedido", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@FazerPedidoActivity, "Erro de conexão: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun configurarNavegacaoMenu() {
        findViewById<TextView>(R.id.menuItemEstoque).setOnClickListener {
            startActivity(Intent(this, EstoqueActivity::class.java))
            finish()
        }
        findViewById<TextView>(R.id.menuItemCarrinho).setOnClickListener {
            startActivity(Intent(this, CarrinhoActivity::class.java))
            finish()
        }
        findViewById<TextView>(R.id.menuItemPedido).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<TextView>(R.id.menuItemSair).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}