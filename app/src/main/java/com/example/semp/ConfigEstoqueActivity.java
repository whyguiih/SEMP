package com.example.semp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.semp.models.DeleteProdutoRequest;
import com.example.semp.models.GenericResponse;
import com.example.semp.models.UpdateProdutoRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.example.semp.models.Produto;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ConfigEstoqueActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private List<Produto> listaProdutos = new ArrayList<>();
    private Produto produtoSelecionado = null;
    private String fotoBase64 = "";
    private ImageView ivPreview;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    processarImagem(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_estoque);

        drawerLayout = findViewById(R.id.drawerLayoutConfig);

        View mainView = findViewById(R.id.mainContentLayout);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        ImageView btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
            });
        }

        Button btnVoltarCadastro = findViewById(R.id.btnVoltarCadastro);
        if (btnVoltarCadastro != null) {
            btnVoltarCadastro.setOnClickListener(v -> {
                startActivity(new Intent(this, CadastrarProdutoActivity.class));
                finish();
            });
        }

        configurarNavegacaoMenu();
        carregarProdutosAPI();

        // --- CAMPOS DE ENTRADA ---
        AutoCompleteTextView etNome = findViewById(R.id.etNomeAtualizar);
        EditText etId = findViewById(R.id.etIdAtualizar);
        EditText etCodigo = findViewById(R.id.etCodigoAtualizar);
        EditText etDescricao = findViewById(R.id.etDescricaoAtualizar);
        EditText etQuantidade = findViewById(R.id.etQuantidadeAtualizar);
        EditText etUnidade = findViewById(R.id.etUnidadeAtualizar);
        EditText etUniAtual = findViewById(R.id.etUniAtualAtualizar);
        EditText etCor = findViewById(R.id.etCorAtualizar);
        EditText etMarca = findViewById(R.id.etMarcaAtualizar);
        EditText etDescDetalhada = findViewById(R.id.etDescDetalhadaAtualizar);
        
        Button btnSelecionarFoto = findViewById(R.id.btnSelecionarFotoAlt);
        ivPreview = findViewById(R.id.ivPreviewFotoAlt);

        AutoCompleteTextView etNomeDel = findViewById(R.id.etNomeDeletar);

        // Lógica para puxar informações ao selecionar na lista
        if (etNome != null) {
            etNome.setOnItemClickListener((parent, view, position, id) -> {
                String nomeSelecionado = (String) parent.getItemAtPosition(position);
                buscarEPreencherProduto(nomeSelecionado, etId, etCodigo, etDescricao, etQuantidade, etUnidade, etUniAtual, etCor, etMarca, etDescDetalhada);
            });
        }

        btnSelecionarFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        Button btnAtualizar = findViewById(R.id.btnAtualizar);
        if (btnAtualizar != null) {
            btnAtualizar.setOnClickListener(v -> {
                if (produtoSelecionado == null) {
                    Toast.makeText(this, "Selecione um produto da lista primeiro", Toast.LENGTH_SHORT).show();
                    return;
                }

                String novaQtdStr = etQuantidade.getText().toString().trim();
                Integer novaQtd = null;
                try {
                    if (!novaQtdStr.isEmpty()) novaQtd = Integer.parseInt(novaQtdStr);
                } catch (Exception e) {}

                // CRITICAL: Consolidating into ONE request to avoid API flood and "Processing" stuck state
                UpdateProdutoRequest req = new UpdateProdutoRequest(
                        produtoSelecionado.id_estoque,
                        etNome.getText().toString().trim(),
                        etCodigo.getText().toString().trim(),
                        etDescricao.getText().toString().trim(),
                        novaQtd,
                        etUnidade.getText().toString().trim(),
                        etUniAtual.getText().toString().trim(),
                        etCor.getText().toString().trim(),
                        etMarca.getText().toString().trim(),
                        etDescDetalhada.getText().toString().trim(),
                        fotoBase64.isEmpty() ? null : fotoBase64
                );

                Toast.makeText(this, "Enviando alterações...", Toast.LENGTH_SHORT).show();

                RetrofitClient.getApi().atualizarProduto(req).enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().sucesso) {
                            Toast.makeText(ConfigEstoqueActivity.this, "Produto atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                            fotoBase64 = ""; // Clear for next use
                            carregarProdutosAPI();
                        } else {
                            String erro = response.body() != null ? response.body().mensagem : "Erro no servidor";
                            Toast.makeText(ConfigEstoqueActivity.this, "Falha: " + erro, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {
                        Toast.makeText(ConfigEstoqueActivity.this, "Erro de conexão!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        Button btnDeletar = findViewById(R.id.btnDeletar);
        if (btnDeletar != null) {
            btnDeletar.setOnClickListener(v -> {
                String nomeBusca = etNomeDel != null ? etNomeDel.getText().toString().trim() : "";
                String codigoParaDeletar = "";
                for (Produto p : listaProdutos) {
                    if (p.nome != null && p.nome.equalsIgnoreCase(nomeBusca)) {
                        codigoParaDeletar = p.codigo;
                        break;
                    }
                }

                if (codigoParaDeletar.isEmpty()) {
                    Toast.makeText(this, "Produto não encontrado para deletar", Toast.LENGTH_SHORT).show();
                    return;
                }

                DeleteProdutoRequest req = new DeleteProdutoRequest(codigoParaDeletar);
                RetrofitClient.getApi().deletarProduto(req).enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().sucesso) {
                            Toast.makeText(ConfigEstoqueActivity.this, "Produto deletado!", Toast.LENGTH_SHORT).show();
                            carregarProdutosAPI();
                            etNomeDel.setText("");
                        } else {
                            Toast.makeText(ConfigEstoqueActivity.this, "Erro ao deletar", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {
                        Toast.makeText(ConfigEstoqueActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

    private void processarImagem(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            // REDIMENSIONAMENTO AGRESSIVO: Para garantir que o Cloudflare aceite (limite de 1MB por request no Free tier)
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 250, 250, true);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            
            fotoBase64 = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
            
            if (ivPreview != null) {
                ivPreview.setImageBitmap(scaledBitmap);
                ivPreview.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarProdutosAPI() {
        RetrofitClient.getApi().getProdutos().enqueue(new Callback<List<Produto>>() {
            @Override
            public void onResponse(Call<List<Produto>> call, Response<List<Produto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaProdutos = response.body();
                    configurarAutoComplete();
                }
            }
            @Override
            public void onFailure(Call<List<Produto>> call, Throwable t) {}
        });
    }

    private void configurarAutoComplete() {
        List<String> nomes = new ArrayList<>();
        for (Produto p : listaProdutos) {
            if (p.nome != null) nomes.add(p.nome);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_dropdown, nomes);
        AutoCompleteTextView etNome = findViewById(R.id.etNomeAtualizar);
        AutoCompleteTextView etNomeDel = findViewById(R.id.etNomeDeletar);
        if (etNome != null) etNome.setAdapter(adapter);
        if (etNomeDel != null) etNomeDel.setAdapter(adapter);
    }

    private void buscarEPreencherProduto(String nome, EditText etId, EditText etCod, EditText etDesc, EditText etQtd, EditText etUni, EditText etUniAtu, EditText etCor, EditText etMarca, EditText etDescDet) {
        for (Produto p : listaProdutos) {
            if (p.nome != null && p.nome.equalsIgnoreCase(nome)) {
                produtoSelecionado = p;
                if (etId != null) etId.setText(String.valueOf(p.id_estoque));
                if (etCod != null) etCod.setText(p.codigo);
                if (etDesc != null) etDesc.setText(p.descricao);
                if (etQtd != null) etQtd.setText(String.valueOf(p.quant));
                if (etUni != null) etUni.setText(p.uni_natal);
                if (etUniAtu != null) etUniAtu.setText(p.uni_intermediarias);
                if (etCor != null) etCor.setText(p.cor);
                if (etMarca != null) etMarca.setText(p.marca_ref);
                if (etDescDet != null) etDescDet.setText(p.descricao_detalhada);

                // Mostrar prévia da foto atual se existir
                if (p.foto != null && !p.foto.isEmpty()) {
                    try {
                        String pureBase64 = p.foto;
                        if (pureBase64.contains(",")) pureBase64 = pureBase64.split(",")[1];
                        byte[] decodedString = android.util.Base64.decode(pureBase64, android.util.Base64.DEFAULT);
                        android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        if (ivPreview != null) {
                            ivPreview.setImageBitmap(decodedByte);
                            ivPreview.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {}
                } else if (ivPreview != null) {
                    ivPreview.setVisibility(View.GONE);
                }

                return;
            }
        }
        produtoSelecionado = null;
    }

    private void configurarNavegacaoMenu() {
        MenuSidebarHelper.configurarNavegacao(this, drawerLayout);
    }
}