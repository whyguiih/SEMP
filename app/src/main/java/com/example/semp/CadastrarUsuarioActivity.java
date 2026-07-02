package com.example.semp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.semp.models.GenericResponse;
import com.example.semp.models.UsuarioRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastrarUsuarioActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_usuario);

        drawerLayout = findViewById(R.id.drawerLayoutCadUsuario);
        ImageView btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) btnMenu.setOnClickListener(v -> {
            if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
        });
        MenuSidebarHelper.configurarNavegacao(this, drawerLayout);

        EditText etUsuario = findViewById(R.id.etNovoUsuario);
        EditText etSenha = findViewById(R.id.etNovaSenha);
        EditText etNivel = findViewById(R.id.etNovoNivel);
        EditText etUnidade = findViewById(R.id.etNovaUnidade);
        EditText etFoto = findViewById(R.id.etNovaFoto); // NOVA LINHA
        Button btnSalvar = findViewById(R.id.btnSalvarUsuario);

        btnSalvar.setOnClickListener(v -> {
            String user = etUsuario.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();
            String nivelStr = etNivel.getText().toString().trim();
            String unidade = etUnidade.getText().toString().trim();
            String fotoUrl = etFoto.getText().toString().trim(); // NOVA LINHA

            if (user.isEmpty() || senha.isEmpty() || nivelStr.isEmpty() || unidade.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show();
                return;
            }

            // BLINDAGEM DOS NÍVEIS
            if (!nivelStr.equals("0") && !nivelStr.equals("1") && !nivelStr.equals("2") && !nivelStr.equals("3")) {
                Toast.makeText(this, "Erro: O nível deve ser APENAS 0, 1, 2 ou 3.", Toast.LENGTH_LONG).show();
                return;
            }

            // Bloqueia o botão para evitar duplicação de cadastro
            btnSalvar.setEnabled(false);
            btnSalvar.setText("Salvando...");

            int nivel = Integer.parseInt(nivelStr);
            // PASSA A VARIÁVEL fotoUrl NO FINAL DO NOVO CONSTRUTOR:
            UsuarioRequest request = new UsuarioRequest(user, senha, nivel, unidade, fotoUrl);

            // Chamada REAL para a API
            RetrofitClient.getApi().cadastrarUsuario(request).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                    btnSalvar.setEnabled(true);
                    btnSalvar.setText("Salvar Usuário");

                    if (response.isSuccessful() && response.body() != null && Boolean.TRUE.equals(response.body().sucesso)) {
                        Toast.makeText(CadastrarUsuarioActivity.this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                        // Limpa todos os campos
                        etUsuario.setText("");
                        etSenha.setText("");
                        etNivel.setText("");
                        etUnidade.setText("");
                        etFoto.setText(""); // NOVA LINHA
                        etUsuario.requestFocus();
                    } else {
                        String msgErro = (response.body() != null && response.body().mensagem != null) ? response.body().mensagem : "Erro ao cadastrar usuário.";
                        Toast.makeText(CadastrarUsuarioActivity.this, msgErro, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse> call, Throwable t) {
                    btnSalvar.setEnabled(true);
                    btnSalvar.setText("Salvar Usuário");
                    Toast.makeText(CadastrarUsuarioActivity.this, "Erro de conexão ao servidor.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}