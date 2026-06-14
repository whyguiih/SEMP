package com.example.continuacao;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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
        Button btnSalvar = findViewById(R.id.btnSalvarUsuario);

        btnSalvar.setOnClickListener(v -> {
            String user = etUsuario.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();
            String nivel = etNivel.getText().toString().trim();
            String unidade = etUnidade.getText().toString().trim();

            if (user.isEmpty() || senha.isEmpty() || nivel.isEmpty() || unidade.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            // BLINDAGEM DOS NÍVEIS
            if (!nivel.equals("0") && !nivel.equals("1") && !nivel.equals("2") && !nivel.equals("3")) {
                Toast.makeText(this, "Erro: O nível deve ser APENAS 0, 1, 2 ou 3.", Toast.LENGTH_LONG).show();
                return;
            }

            // AQUI você chamará o RetrofitClient.getApi().cadastrarUsuario(...) futuramente
            Toast.makeText(CadastrarUsuarioActivity.this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
            etUsuario.setText("");
            etSenha.setText("");
            etNivel.setText("");
            etUnidade.setText("");
            // Não dê 'finish()' aqui, para o master poder cadastrar vários seguidos sem o app fechar!
        });
    }
}