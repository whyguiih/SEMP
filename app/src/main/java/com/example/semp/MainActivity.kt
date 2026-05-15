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
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ativa o layout edge-to-edge
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Ajusta o padding para não ficar sob as barras de status e navegação
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

            realizarLogin(usuario, senha)
        }
    }

    private fun realizarLogin(usuarioInput: String, senhaInput: String) {
        thread {
            // Verifique se o IP do servidor está correto e acessível pelo Android
            val url = "jdbc:mysql://192.168.0.117:3306/db_estoque"
            val dbUsuario = "root"
            val dbPassword = ""

            try {
                Class.forName("com.mysql.cj.jdbc.Driver")
                val conexao = DriverManager.getConnection(url, dbUsuario, dbPassword)

                val sql = "SELECT * FROM tb_usuarios WHERE usuario = ? AND senha = ?"
                val ps = conexao.prepareStatement(sql)
                ps.setString(1, usuarioInput)
                ps.setString(2, senhaInput)

                val rs = ps.executeQuery()

                if (rs.next()) {
                    // Salva os dados na sessão (Companion Object)
                    setUsuario(usuarioInput)
                    val nivelConta = rs.getInt("nivel_conta")
                    setStatus(nivelConta.toString())
                    val unidade = rs.getString("unidade")
                    setUnidade(unidade)

                    // Se o login der certo, vai para a próxima página
                    runOnUiThread {
                        Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, EstoqueActivity::class.java)
                        startActivity(intent)
                        finish() // Fecha a tela de login para não voltar ao apertar o botão "voltar"
                    }
                } else {
                    // Se der errado, mostra a mensagem de erro
                    runOnUiThread {
                        Toast.makeText(this, "Usuário ou senha incorretos!", Toast.LENGTH_SHORT).show()
                    }
                }
                rs.close()
                ps.close()
                conexao.close()
            } catch (e: ClassNotFoundException) {
                runOnUiThread {
                    Toast.makeText(this, "Driver JDBC não encontrado.", Toast.LENGTH_LONG).show()
                }
            } catch (e: SQLException) {
                runOnUiThread {
                    Toast.makeText(this, "Erro de conexão com o banco: ${e.message}", Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            }
        }
    }

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