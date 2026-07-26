package com.example.semp;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
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
            if (drawerLayout != null) {
                try {
                    drawerLayout.openDrawer(androidx.core.view.GravityCompat.START);
                } catch (Exception e) {
                    drawerLayout.openDrawer(android.view.Gravity.LEFT);
                }
            }
        });
        MenuSidebarHelper.configurarNavegacao(this, drawerLayout);

        Button btnAcessarAlterar = findViewById(R.id.btnAcessarAlterarEstoque);
        if (btnAcessarAlterar != null) {
            btnAcessarAlterar.setOnClickListener(v -> startActivity(new Intent(CadastrarProdutoActivity.this, ConfigEstoqueActivity.class)));
        }

        EditText etNome = findViewById(R.id.etCadNome);
        EditText etCodigo = findViewById(R.id.etCadCodigo);
        EditText etCodigoFisico = findViewById(R.id.codigoFisico);
        EditText etDescricao = findViewById(R.id.etCadDescricao);
        EditText etQtd = findViewById(R.id.etCadQtd);
        EditText etUniNatal = findViewById(R.id.etCadUniNatal);
        EditText etUniAtual = findViewById(R.id.etCadUniAtual);
        EditText etMarca = findViewById(R.id.etCadMarca);
        EditText etCor = findViewById(R.id.etCadCor);
        EditText etDescDetalhada = findViewById(R.id.etCadDescDetalhada);
        btnSalvar = findViewById(R.id.btnSalvarProduto);
        Button btnSelecionarFoto = findViewById(R.id.btnSelecionarFoto);
        Button btnGerarCodigo = findViewById(R.id.btnGerarCodigo);
        ivPreview = findViewById(R.id.ivPreviewFoto);

        // PUXA UNIDADE AUTOMATICAMENTE
        SharedPreferences sessao = getSharedPreferences("SessaoApp", MODE_PRIVATE);
        String unidadeLogada = sessao.getString("unidadeAtual", "");
        if (etUniNatal != null) {
            etUniNatal.setText(unidadeLogada);
            etUniNatal.setEnabled(false); // Bloqueia para não cadastrar em unidade errada
        }
        if (etUniAtual != null) {
            etUniAtual.setText(unidadeLogada);
            etUniAtual.setEnabled(false);
        }

        btnGerarCodigo.setOnClickListener(v -> {
            String idEstado = sessao.getString("id_estado", "X");
            String idRegiao = sessao.getString("id_regiao", "X");
            int idUnidade = sessao.getInt("id_unidade", 0);
            
            String novoCodigo = SempUtils.gerarCodigoProdutoModerno(idEstado, idRegiao, idUnidade);
            etCodigo.setText(novoCodigo);
            Toast.makeText(this, "Código Gerado!", Toast.LENGTH_SHORT).show();
        });

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

            btnSalvar.setEnabled(false);
            btnSalvar.setText("Salvando...");

            ProdutoRequest request = new ProdutoRequest(nome, codigo, etCodigoFisico.getText().toString().trim(), 
                    etDescricao.getText().toString().trim(),
                    qtd, etUniNatal.getText().toString().trim(), etMarca.getText().toString().trim(),
                    etCor.getText().toString().trim(), etDescDetalhada.getText().toString().trim(),
                    fotoBase64, etUniAtual.getText().toString().trim());

            RetrofitClient.getApi().cadastrarProduto(request).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                    btnSalvar.setEnabled(true);
                    btnSalvar.setText("SALVAR NOVO PRODUTO");

                    if (response.isSuccessful() && response.body() != null && Boolean.TRUE.equals(response.body().sucesso)) {
                        Toast.makeText(CadastrarProdutoActivity.this, "Produto salvo com sucesso!", Toast.LENGTH_SHORT).show();
                        // LIMPA OS CAMPOS EM VEZ DE FECHAR A TELA
                        limparFormulario(etNome, etCodigo, etCodigoFisico, etDescricao, etQtd, etDescDetalhada, etMarca, etCor);
                    } else {
                        String erroApi = "Erro ao salvar";
                        if (response.body() != null && response.body().mensagem != null) {
                            erroApi = response.body().mensagem;
                        } else if (response.errorBody() != null) {
                            try {
                                erroApi = "Erro " + response.code() + ": " + response.errorBody().string();
                            } catch (Exception e) {
                                erroApi = "Erro HTTP " + response.code();
                            }
                        }
                        mostrarAlertaGrande(findViewById(android.R.id.content), erroApi, "#e74c3c");
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse> call, Throwable t) {
                    btnSalvar.setEnabled(true);
                    btnSalvar.setText("SALVAR NOVO PRODUTO");
                    mostrarAlertaGrande(findViewById(android.R.id.content), "Falha na conexão: " + t.getMessage(), "#c0392b");
                }
            });
        });
    }

    private void limparFormulario(EditText... editTexts) {
        for (EditText et : editTexts) {
            if (et != null) et.setText("");
        }
        fotoBase64 = "";
        if (ivPreview != null) ivPreview.setVisibility(View.GONE);
    }

    private void mostrarAlertaGrande(View view, String mensagem, String corHexa) {
        try {
            com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar.make(view, mensagem, 20000); // 20 segundos
            snackbar.setAction("FECHAR", v -> snackbar.dismiss());
            snackbar.setActionTextColor(android.graphics.Color.BLACK);
            
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(android.graphics.Color.parseColor(corHexa));

            android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) snackbarView.getLayoutParams();
            params.gravity = android.view.Gravity.TOP;
            params.topMargin = 150;
            snackbarView.setLayoutParams(params);

            TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            if (textView != null) {
                textView.setTextColor(android.graphics.Color.BLACK);
                textView.setTextSize(18);
                textView.setPadding(20, 20, 20, 20);
                textView.setMaxLines(10);
            }
            
            snackbar.show();
        } catch (Exception e) {
            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
        }
    }

    private void processarImagem(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float ratio = (float) width / height;
            int newWidth = 400;
            int newHeight = (int) (newWidth / ratio);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
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