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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.semp.models.GenericResponse;
import com.example.semp.models.ProdutoRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastrarProdutoActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
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
        setContentView(R.layout.activity_cadastrar_produto);

        drawerLayout = findViewById(R.id.drawerLayoutCadastroProduto);
        ImageView btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) btnMenu.setOnClickListener(v -> {
            if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
        });
        MenuSidebarHelper.configurarNavegacao(this, drawerLayout);

        Button btnAcessarAlterar = findViewById(R.id.btnAcessarAlterarEstoque);
        if (btnAcessarAlterar != null) {
            btnAcessarAlterar.setOnClickListener(v -> {
                Intent intent = new Intent(CadastrarProdutoActivity.this, ConfigEstoqueActivity.class);
                startActivity(intent);
            });
        }

        EditText etNome = findViewById(R.id.etCadNome);
        EditText etCodigo = findViewById(R.id.etCadCodigo);
        EditText etDescricao = findViewById(R.id.etCadDescricao);
        EditText etQtd = findViewById(R.id.etCadQtd);
        EditText etUniNatal = findViewById(R.id.etCadUniNatal);
        EditText etUniAtual = findViewById(R.id.etCadUniAtual);
        EditText etMarca = findViewById(R.id.etCadMarca);
        EditText etCor = findViewById(R.id.etCadCor);
        EditText etDescDetalhada = findViewById(R.id.etCadDescDetalhada);
        Button btnSalvar = findViewById(R.id.btnSalvarProduto);
        Button btnSelecionarFoto = findViewById(R.id.btnSelecionarFoto);
        ivPreview = findViewById(R.id.ivPreviewFoto);

        btnSelecionarFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSalvar.setOnClickListener(v -> {
            String nome = etNome.getText().toString().trim();
            String codigo = etCodigo.getText().toString().trim();
            String desc = etDescricao.getText().toString().trim();
            String uniNatal = etUniNatal.getText().toString().trim();
            String uniAtual = etUniAtual.getText().toString().trim();
            String marca = etMarca.getText().toString().trim();
            String cor = etCor.getText().toString().trim();
            String descDetalhada = etDescDetalhada.getText().toString().trim();

            int qtd = 0;
            try {
                qtd = Integer.parseInt(etQtd.getText().toString().trim());
            } catch (NumberFormatException e) {
                qtd = 0;
            }

            if (nome.isEmpty() || codigo.isEmpty()) {
                Toast.makeText(this, "Nome e Código são obrigatórios!", Toast.LENGTH_SHORT).show();
                return;
            }

            ProdutoRequest request = new ProdutoRequest(nome, codigo, desc, qtd, uniNatal, marca, cor, descDetalhada, fotoBase64, uniAtual);

            RetrofitClient.getApi().cadastrarProduto(request).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().sucesso) {
                        Toast.makeText(CadastrarProdutoActivity.this, "Produto salvo com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(CadastrarProdutoActivity.this, "Erro ao salvar produto.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse> call, Throwable t) {
                    Toast.makeText(CadastrarProdutoActivity.this, "Erro de Conexão com a API", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void processarImagem(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            // Redimensiona para não estourar o limite da API (Base64 grande pesa)
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
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
}