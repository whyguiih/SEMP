package com.example.semp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.semp.models.GenericResponse;
import com.example.semp.models.RastreioRequest;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RastreioActivity extends AppCompatActivity {

    private String unidadeSegura = "";
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rastreio);

        drawerLayout = findViewById(R.id.drawerLayout);
        ImageView btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
            });
        }
        MenuSidebarHelper.configurarNavegacao(this, drawerLayout);

        SharedPreferences prefs = getSharedPreferences("SessaoApp", Context.MODE_PRIVATE);
        unidadeSegura = prefs.getString("unidadeAtual", "");

        EditText etCodigo = findViewById(R.id.etRastreioCodigo);
        EditText etOriginal = findViewById(R.id.etRastreioOriginal);
        EditText etDestino = findViewById(R.id.etRastreioDestino);
        EditText etDataSaida = findViewById(R.id.etRastreioDataSaida);
        EditText etDataEntrada = findViewById(R.id.etRastreioDataEntrada);
        Button btnConfirmar = findViewById(R.id.btnConfirmarRastreio);

        etOriginal.setText(unidadeSegura);

        etDataSaida.setOnClickListener(v -> abrirCalendario(etDataSaida));
        etDataEntrada.setOnClickListener(v -> abrirCalendario(etDataEntrada));

        btnConfirmar.setOnClickListener(v -> {
            String codigo = etCodigo.getText().toString().trim();
            String original = etOriginal.getText().toString().trim();
            String destino = etDestino.getText().toString().trim();
            String dataSaida = etDataSaida.getText().toString().trim();
            String dataEntrada = etDataEntrada.getText().toString().trim();

            if (codigo.isEmpty() || destino.isEmpty() || dataEntrada.isEmpty()) {
                Toast.makeText(this, "Preencha Código, Destino e Data de Entrada!", Toast.LENGTH_SHORT).show();
                return;
            }

            RastreioRequest req = new RastreioRequest(codigo, original, destino, dataSaida, dataEntrada);

            RetrofitClient.getApi().registrarRastreio(req).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().sucesso) {
                        Toast.makeText(RastreioActivity.this, "Rastreio salvo com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RastreioActivity.this, "Erro ao salvar rastreio.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse> call, Throwable t) {
                    Toast.makeText(RastreioActivity.this, "Falha na conexão.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void abrirCalendario(EditText editText) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    String dataFormatada = year + "-" + String.format(Locale.getDefault(), "%02d", (month + 1)) + "-" + String.format(Locale.getDefault(), "%02d", day);
                    editText.setText(dataFormatada);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }
}
