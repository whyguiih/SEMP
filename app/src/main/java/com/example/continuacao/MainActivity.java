package com.example.continuacao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.continuacao.models.LoginRequest;
import com.example.continuacao.models.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // VARIÁVEIS GLOBAIS DE SESSÃO ATUALIZADAS
    public static String usuarioLogado = ""; // <-- Adicionamos esta linha!
    public static String unidadeAtual = "";
    public static String nivelContaAtual = "0";

    // Adicionamos o usuário aqui para ser salvo
    public static void salvarSessao(String usuario, String unidade, String nivel) {
        usuarioLogado = usuario != null ? usuario : "";
        unidadeAtual = unidade != null ? unidade : "";
        nivelContaAtual = nivel != null ? nivel : "0";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText etUsuario = findViewById(R.id.etUsuario);
        EditText etSenha = findViewById(R.id.etSenha);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();

            if (usuario.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha usuário e senha", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginRequest request = new LoginRequest(usuario, senha);

            RetrofitClient.getApi().fazerLogin(request).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    LoginResponse body = response.body();

                    if (response.isSuccessful() && body != null && body.sucesso) {

                        // AGORA SALVAMOS O USUÁRIO TAMBÉM!
                        salvarSessao(body.usuario, body.unidade, body.nivel_conta);

                        Toast.makeText(MainActivity.this, body.mensagem, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MainActivity.this, EstoqueActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String msgErro = (body != null && body.mensagem != null) ? body.mensagem : "Erro no login";
                        Toast.makeText(MainActivity.this, msgErro, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}