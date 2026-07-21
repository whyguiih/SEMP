package com.example.semp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.semp.models.GenericResponse;
import com.example.semp.models.UnidadeRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastrarUnidades extends AppCompatActivity {
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_unidade);

        drawerLayout = findViewById(R.id.drawerLayoutCadUsuario);
        ImageView btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) btnMenu.setOnClickListener(v -> {
            if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
        });
        MenuSidebarHelper.configurarNavegacao(this, drawerLayout);

        EditText etNomeUnidade = findViewById(R.id.nomeUnidade);
        AutoCompleteTextView actvEstado = findViewById(R.id.estado);
        AutoCompleteTextView actvRegiao = findViewById(R.id.regiao);
        AutoCompleteTextView actvIdentificacao = findViewById(R.id.identificacao);
        Button btnSalvarUnidade = findViewById(R.id.btnSalvarUnidade);

        // 1. Defina as listas de opções
        String[] listaEstados = {
                "Rio Grande do Sul", "Santa Catarina", "Paraná", "São Paulo", "Rio de Janeiro",
                "Espiríto Santo", "Minas Gerais", "Goiás", "Mato Grosso", "Mato Grosso do Sul",
                "Rio Grande do Norte", "Acre", "Amapá", "Amazonas", "Pará", "Rondônia",
                "Roraima", "Tocantins", "Alagoas", "Bahia", "Ceará", "Maranhão",
                "Paraíba", "Pernambuco", "Piauí", "Sergipe"
        };

        String[] listaRegioesRS = {
                "Metropolitana", "Metropolitana 3", "Serra 1", "Metropolitana 2", "Vale dos Sinos 2",
                "Noroeste 2", "Noroeste 1", "Vale dos Sino 1", "Central", "Vale do Rio Pardo",
                "Serra 3", "Vale do Taquari 2", "Norte 1", "Sul 1", "Vale dos Sinos 3",
                "Norte 2", "Vale do Taquari 1", "Serra 2", "Encosta da Serra", "Sul 2"
        };

        String[] listaRegioesPadrao = {"Metropolitana"};
        String[] opcoesIdentificacao = {"Unidade", "Adendo"};

        // 2. Crie os Adaptadores
        ArrayAdapter<String> adapterEstados = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listaEstados);
        ArrayAdapter<String> adapterIdentificacao = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opcoesIdentificacao);

        // 3. Associe os adaptadores aos campos
        actvEstado.setAdapter(adapterEstados);
        actvIdentificacao.setAdapter(adapterIdentificacao);

        // Inicializa o adapter de região com o padrão
        ArrayAdapter<String> adapterRegioesPadrao = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listaRegioesPadrao);
        actvRegiao.setAdapter(adapterRegioesPadrao);

        // Listener para atualizar as regiões quando o estado mudar
        actvEstado.setOnItemClickListener((parent, view, position, id) -> {
            String estadoSelecionado = (String) parent.getItemAtPosition(position);
            if ("Rio Grande do Sul".equals(estadoSelecionado)) {
                ArrayAdapter<String> adapterRS = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listaRegioesRS);
                actvRegiao.setAdapter(adapterRS);
            } else {
                actvRegiao.setAdapter(adapterRegioesPadrao);
            }
            actvRegiao.setText(""); // Limpa a região anterior
        });

        // Dica: Para abrir a lista assim que o usuário clicar no campo
        actvEstado.setOnClickListener(v -> actvEstado.showDropDown());
        actvRegiao.setOnClickListener(v -> actvRegiao.showDropDown());
        actvIdentificacao.setOnClickListener(v -> actvIdentificacao.showDropDown());

        btnSalvarUnidade.setOnClickListener(v -> {
            String nome = etNomeUnidade.getText().toString().trim();
            String estado = actvEstado.getText().toString().trim();
            String regiao = actvRegiao.getText().toString().trim();
            String identificacao = actvIdentificacao.getText().toString().trim();

            if (nome.isEmpty() || estado.isEmpty() || regiao.isEmpty() || identificacao.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Bloqueia o botão para evitar duplicação
            btnSalvarUnidade.setEnabled(false);
            btnSalvarUnidade.setText("Salvando...");

            UnidadeRequest request = new UnidadeRequest(nome, estado, regiao, identificacao);

            // Chamada REAL para a API
            RetrofitClient.getApi().cadastrarUnidade(request).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                    btnSalvarUnidade.setEnabled(true);
                    btnSalvarUnidade.setText("CADASTRAR UNIDADE");

                    if (response.isSuccessful() && response.body() != null && Boolean.TRUE.equals(response.body().sucesso)) {
                        Toast.makeText(CadastrarUnidades.this, "Unidade cadastrada com sucesso!", Toast.LENGTH_SHORT).show();
                        // Limpa os campos
                        etNomeUnidade.setText("");
                        actvEstado.setText("");
                        actvRegiao.setText("");
                        actvIdentificacao.setText("");
                        etNomeUnidade.requestFocus();
                    } else {
                        String msgErro = (response.body() != null && response.body().mensagem != null) ? response.body().mensagem : "Erro ao cadastrar unidade.";
                        Toast.makeText(CadastrarUnidades.this, msgErro, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse> call, Throwable t) {
                    btnSalvarUnidade.setEnabled(true);
                    btnSalvarUnidade.setText("CADASTRAR UNIDADE");
                    Toast.makeText(CadastrarUnidades.this, "Erro de conexão ao servidor.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
