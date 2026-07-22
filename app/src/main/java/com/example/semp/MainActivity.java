package com.example.semp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.semp.models.LoginRequest;
import com.example.semp.models.LoginResponse;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.CredentialManagerCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // VARIÁVEIS GLOBAIS DE SESSÃO
    public static String usuarioLogado = "";
    public static String unidadeAtual = "";
    public static String nivelContaAtual = "0";

    private CredentialManager credentialManager;

    private void inicializarSessao() {
        SharedPreferences prefs = getSharedPreferences("SessaoApp", MODE_PRIVATE);
        usuarioLogado = prefs.getString("usuarioLogado", "");
        unidadeAtual = prefs.getString("unidadeAtual", "");
        nivelContaAtual = prefs.getString("nivelContaAtual", "0");
    }

    public void salvarSessao(LoginResponse res) {
        usuarioLogado = res.usuario != null ? res.usuario : "";
        unidadeAtual = res.unidade != null ? res.unidade : "";
        nivelContaAtual = res.nivel_conta != null ? res.nivel_conta : "0";

        SharedPreferences sharedPreferences = getSharedPreferences("SessaoApp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("usuarioLogado", usuarioLogado);
        editor.putString("unidadeAtual", unidadeAtual);
        editor.putString("nivelContaAtual", nivelContaAtual);
        
        // Salva os identificadores para gerar código de produto depois
        editor.putString("id_estado", res.estado_identificador != null ? res.estado_identificador : "X");
        editor.putString("id_regiao", res.regiao_identificador != null ? res.regiao_identificador : "X");
        editor.putInt("id_unidade", res.unidade_identificador != null ? res.unidade_identificador : 0);

        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        inicializarSessao();

        credentialManager = CredentialManager.create(this);

        View mainLayout = findViewById(R.id.main);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                androidx.core.graphics.Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        EditText etUsuario = findViewById(R.id.etUsuario);
        EditText etSenha = findViewById(R.id.etSenha);
        Button btnLogin = findViewById(R.id.btnLogin);


        btnLogin.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();

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

            btnLogin.setEnabled(false);
            btnLogin.setText("Carregando...");

            LoginRequest request = new LoginRequest(usuario, senha);

            RetrofitClient.getApi().fazerLogin(request).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Entrar");

                    LoginResponse body = response.body();

                    if (response.isSuccessful() && body != null && Boolean.TRUE.equals(body.sucesso)) {
                        salvarSessao(body);
                        String nivel = body.nivel_conta != null ? body.nivel_conta : "0";
                        Toast.makeText(MainActivity.this, body.mensagem != null ? body.mensagem : "Login realizado!", Toast.LENGTH_SHORT).show();

                        Intent intent;
                        if ("3".equals(nivel)) {
                            intent = new Intent(MainActivity.this, CadastrarUsuarioActivity.class);
                        } else {
                            intent = new Intent(MainActivity.this, EstoqueActivity.class);
                        }
                        startActivity(intent);
                        finish();
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
