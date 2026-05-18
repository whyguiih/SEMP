package com.example.semp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etSenha = findViewById<EditText>(R.id.etSenha)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)

        btnEntrar.setOnClickListener {
            val usuario = etUsuario.text.toString()
            val senha = etSenha.text.toString()

            if (usuario.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Chama a nova função conectada na API
            realizarLogin(usuario, senha)
        }
    }

    private fun realizarLogin(usuarioInput: String, senhaInput: String) {
        val request = LoginRequest(usuarioInput, senhaInput)

        // O Retrofit gerencia a chamada de rede de forma assíncrona automaticamente
        RetrofitClient.api.fazerLogin(request).enqueue(object : Callback<LoginResponse> {

            // Se conseguiu comunicar com a API
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val loginResponse = response.body()

                if (response.isSuccessful && loginResponse != null) {
                    if (loginResponse.sucesso) {
                        // Salva os dados na sessão (Companion Object)
                        loginResponse.usuario?.let { setUsuario(it) }
                        loginResponse.nivel_conta?.let { setStatus(it.toString()) }
                        loginResponse.unidade?.let { setUnidade(it) }

                        Toast.makeText(this@MainActivity, loginResponse.mensagem, Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@MainActivity, EstoqueActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Usuário ou senha incorretos
                        Toast.makeText(this@MainActivity, loginResponse.mensagem, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Erro na resposta do servidor.", Toast.LENGTH_SHORT).show()
                }
            }

            // Se falhou por falta de internet, IP errado, ou API fora do ar
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Erro de conexão: Verifique o servidor", Toast.LENGTH_LONG).show()
                t.printStackTrace()
            }
        })
    }

    // Seu bloco companion foi mantido intacto!
    companion object {
        private var nomeUsuario: String? = null
        private var statusSecondario: String? = null
        private var unidadePertencente: String? = null
        private var usu_pedido: String? = null

        fun setStatus(status: String) { statusSecondario = status }
        fun setUsuario(usuario: String) { nomeUsuario = usuario }
        fun getUsuario(): String = nomeUsuario ?: "Usuário não logado"
        fun getStatus(): String = statusSecondario ?: "Usuário não logado"
        fun setUnidade(unidade: String) { unidadePertencente = unidade }
        fun getUnidade(): String = unidadePertencente ?: "Usuário não logado"
        fun setUsu(usu: String) { usu_pedido = usu }
        fun getUsus(): String = usu_pedido ?: "Usuário não logado"
    }
}