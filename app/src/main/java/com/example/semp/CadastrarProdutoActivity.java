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
    private Button btnSalvar;

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
            btnAcessarAlterar.setOnClickListener(v -> startActivity(new Intent(CadastrarProdutoActivity.this, ConfigEstoqueActivity.class)));
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
        btnSalvar = findViewById(R.id.btnSalvarProduto);
        Button btnSelecionarFoto = findViewById(R.id.btnSelecionarFoto);
        ivPreview = findViewById(R.id.ivPreviewFoto);

        btnSelecionarFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSalvar.setOnClickListener(v -> {
            String nome = etNome.getText().toString().trim();
            String codigo = etCodigo.getText().toString().trim();

            if (nome.isEmpty() || codigo.isEmpty()) {
                Toast.makeText(this, "Nome e Código são obrigatórios!", Toast.LENGTH_SHORT).show();
                return;
            }

            int qtd = 0;
            try { qtd = Integer.parseInt(etQtd.getText().toString().trim()); } catch (Exception ignored) {}

            btnSalvar.setEnabled(false); // Evitar duplo clique
            btnSalvar.setText("Salvando...");

            ProdutoRequest request = new ProdutoRequest(nome, codigo, etDescricao.getText().toString().trim(),
                    qtd, etUniNatal.getText().toString().trim(), etMarca.getText().toString().trim(),
                    etCor.getText().toString().trim(), etDescDetalhada.getText().toString().trim(),
                    fotoBase64, etUniAtual.getText().toString().trim());

            RetrofitClient.getApi().cadastrarProduto(request).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                    btnSalvar.setEnabled(true);
                    btnSalvar.setText("Salvar Produto");

                    if (response.isSuccessful() && response.body() != null && Boolean.TRUE.equals(response.body().sucesso)) {
                        Toast.makeText(CadastrarProdutoActivity.this, "Produto salvo com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(CadastrarProdutoActivity.this, "Erro ao salvar produto.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse> call, Throwable t) {
                    btnSalvar.setEnabled(true);
                    btnSalvar.setText("Salvar Produto");
                    Toast.makeText(CadastrarProdutoActivity.this, "Erro de Conexão", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void processarImagem(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // OTIMIZAÇÃO: Manter o Aspect Ratio (Proporção real da imagem)
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float ratio = (float) width / height;
            int newWidth = 400; // Largura máxima desejada
            int newHeight = (int) (newWidth / ratio);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream); // Comprime qualidade 70% para a API
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