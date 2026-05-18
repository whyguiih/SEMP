package com.example.semp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.concurrent.thread

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

            realizarLogin(usuario, senha)
        }
    }

    private fun realizarLogin(usuarioInput: String, senhaInput: String) {
        thread {
            // ATENÇÃO AQUI: Se você estiver usando o EMULADOR do Android Studio,
            // mude o IP 192.168.0.117 para 10.0.2.2 (que é o IP padrão que o emulador usa para acessar o PC).
            // Se estiver testando no SEU CELULAR FÍSICO via Wi-Fi, mantenha o 192.168.0.117.

            // Adicionados parâmetros de SSL, fuso horário e recuperação de chave pública (essenciais)
            val url = "jdbc:mysql://192.168.0.131:3306/db_estoque?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
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
                    setUsuario(usuarioInput)
                    val nivelConta = rs.getInt("nivel_conta")
                    setStatus(nivelConta.toString())
                    val unidade = rs.getString("unidade")
                    setUnidade(unidade ?: "")

                    runOnUiThread {
                        Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, EstoqueActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Usuário ou senha incorretos!", Toast.LENGTH_SHORT).show()
                    }
                }

                rs.close()
                ps.close()
                conexao.close()

            } catch (e: ClassNotFoundException) {
                Log.e("BANCO_ERRO", "Driver não encontrado", e)
                runOnUiThread {
                    Toast.makeText(this, "Driver JDBC não encontrado.", Toast.LENGTH_LONG).show()
                }
            } catch (e: SQLException) {
                Log.e("BANCO_ERRO", "Erro de SQL/Conexão", e)
                runOnUiThread {
                    Toast.makeText(this, "Erro de conexão: Verifique o PC/Rede. Detalhe: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("BANCO_ERRO", "Erro geral", e)
                runOnUiThread {
                    Toast.makeText(this, "Erro inesperado: ${e.message}", Toast.LENGTH_LONG).show()
                }
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