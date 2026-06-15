package com.example.semp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.semp.models.LoginRequest;
import com.example.semp.models.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // VARIÁVEIS GLOBAIS DE SESSÃO
    // Nota: Mantive para não quebrar suas outras telas, mas adicionei SharedPreferences abaixo como boa prática.
    public static String usuarioLogado = "";
    public static String unidadeAtual = "";
    public static String nivelContaAtual = "0";
    public void salvarSessao(String usuario, String unidade, String nivel) {
        usuarioLogado = usuario != null ? usuario : "";
        unidadeAtual = unidade != null ? unidade : "";
        nivelContaAtual = nivel != null ? nivel : "0";

        // Boa prática: Salvar no SharedPreferences para não perder o login ao fechar o app
        SharedPreferences sharedPreferences = getSharedPreferences("SessaoApp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("usuarioLogado", usuarioLogado);
        editor.putString("unidadeAtual", unidadeAtual);
        editor.putString("nivelContaAtual", nivelContaAtual);
        editor.apply();
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

            // Validação visual de campos vazios
            if (usuario.isEmpty()) {
                etUsuario.setError("Preencha o usuário");
                etUsuario.requestFocus();
                return;
            }

            if (senha.isEmpty()) {
                etSenha.setError("Preencha a senha");
                etSenha.requestFocus();
                return;
            }

            // Desativa o botão para evitar múltiplos cliques durante o carregamento
            btnLogin.setEnabled(false);
            btnLogin.setText("Carregando...");

            LoginRequest request = new LoginRequest(usuario, senha);

            RetrofitClient.getApi().fazerLogin(request).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    // Reativa o botão
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Entrar");

                    LoginResponse body = response.body();

                    if (response.isSuccessful() && body != null && Boolean.TRUE.equals(body.sucesso)) {

                        salvarSessao(body.usuario, body.unidade, body.nivel_conta);

                        Toast.makeText(MainActivity.this, body.mensagem != null ? body.mensagem : "Login realizado!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MainActivity.this, EstoqueActivity.class);
                        startActivity(intent);
                        finish(); // Fecha a tela de login
                    } else {
                        String msgErro = (body != null && body.mensagem != null) ? body.mensagem : "Usuário ou senha incorretos";
                        Toast.makeText(MainActivity.this, msgErro, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Entrar");
                    Toast.makeText(MainActivity.this, "Erro de conexão. Tente novamente.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}