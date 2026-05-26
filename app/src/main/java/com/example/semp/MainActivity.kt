package com.example.semp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    // Companion Object permite acessar a unidade e o nível de qualquer tela do app
    companion object {
        private var unidadeAtual = ""
        private var nivelContaAtual = "0" // Padrão: Usuário Comum

        fun getUnidade() = unidadeAtual
        fun getNivelConta() = nivelContaAtual

        fun salvarSessao(unidade: String, nivel: String) {
            unidadeAtual = unidade
            nivelContaAtual = nivel
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etSenha = findViewById<EditText>(R.id.etSenha)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val usuario = etUsuario.text.toString()
            val senha = etSenha.text.toString()

            if (usuario.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha usuário e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(usuario, senha)

            RetrofitClient.api.fazerLogin(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    val body = response.body()

                    if (response.isSuccessful && body != null && body.sucesso) {
                        // Salva os dados na sessão global
                        salvarSessao(body.unidade ?: "", body.nivel_conta ?: "0")

                        Toast.makeText(this@MainActivity, body.mensagem, Toast.LENGTH_SHORT).show()

                        // Direciona para a tela principal (Estoque)
                        val intent = Intent(this@MainActivity, EstoqueActivity::class.java)
                        startActivity(intent)
                        finish() // Finaliza o login para não voltar nele
                    } else {
                        Toast.makeText(this@MainActivity, body?.mensagem ?: "Erro no login", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Erro de conexão: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}