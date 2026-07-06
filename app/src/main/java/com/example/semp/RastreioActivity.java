package com.example.semp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.semp.models.GenericResponse;
import com.example.semp.models.PedidosPendentes;
import com.example.semp.models.RastreioRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RastreioActivity extends AppCompatActivity {

    private String unidadeSegura = "";
    private DrawerLayout drawerLayout;
    private List<PedidosPendentes> listaPedidosGlobal = new ArrayList<>();
    private PedidosPendentes pedidoSelecionado = null;
    private PedidoDropdownAdapter adapter;

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

        AutoCompleteTextView etCodigo = findViewById(R.id.etRastreioCodigo);
        EditText etOriginal = findViewById(R.id.etRastreioOriginal);
        EditText etDestino = findViewById(R.id.etRastreioDestino);
        EditText etDataSaida = findViewById(R.id.etRastreioDataSaida);
        EditText etDataEntrada = findViewById(R.id.etRastreioDataEntrada);
        Button btnConfirmar = findViewById(R.id.btnConfirmarRastreio);

        etOriginal.setText(unidadeSegura);
        etOriginal.setEnabled(false);

        etDataSaida.setOnClickListener(v -> abrirCalendario(etDataSaida));
        etDataEntrada.setOnClickListener(v -> abrirCalendario(etDataEntrada));

        buscarPedidosParaFiltro(etCodigo, etDestino);

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

            // 1. Validar Unidade Destino
            if (pedidoSelecionado != null) {
                String destinoOficial = pedidoSelecionado.unidade != null ? pedidoSelecionado.unidade : "";
                if (!destinoOficial.equalsIgnoreCase(destino)) {
                    Toast.makeText(this, "ERRO: O destino para este pedido deve ser " + destinoOficial, Toast.LENGTH_LONG).show();
                    etDestino.setText(destinoOficial);
                    return;
                }
                // Validar data
                validarDatas(dataEntrada, pedidoSelecionado.data_reserva);
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

    private void buscarPedidosParaFiltro(AutoCompleteTextView autoView, EditText etDestino) {
        RetrofitClient.getApi().getPedidosPendentes().enqueue(new Callback<List<PedidosPendentes>>() {
            @Override
            public void onResponse(Call<List<PedidosPendentes>> call, Response<List<PedidosPendentes>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaPedidosGlobal = response.body();
                    adapter = new PedidoDropdownAdapter(RastreioActivity.this, listaPedidosGlobal);
                    autoView.setAdapter(adapter);

                    autoView.setOnItemClickListener((parent, view, position, id) -> {
                        pedidoSelecionado = (PedidosPendentes) parent.getItemAtPosition(position);
                        if (pedidoSelecionado != null) {
                            String displayCode = pedidoSelecionado.codigo_pedido != null ? pedidoSelecionado.codigo_pedido : String.valueOf(pedidoSelecionado.id_emprestimo);
                            autoView.setText(displayCode);
                            autoView.setSelection(displayCode.length());
                            etDestino.setText(pedidoSelecionado.unidade);
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call<List<PedidosPendentes>> call, Throwable t) {}
        });
    }

    private void validarDatas(String dataChegadaStr, String dataReservaStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dataChegada = sdf.parse(dataChegadaStr);
            Date dataReserva = sdf.parse(dataReservaStr);
            if (dataChegada != null && dataReserva != null && dataChegada.after(dataReserva)) {
                Toast.makeText(this, "AVISO: Previsão de chegada após a reserva (" + dataReservaStr + ")!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {}
    }

    private void abrirCalendario(EditText editText) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, day) -> {
            String dataFormatada = String.format(Locale.getDefault(), "%d-%02d-%02d", year, (month + 1), day);
            editText.setText(dataFormatada);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private class PedidoDropdownAdapter extends ArrayAdapter<PedidosPendentes> {
        private List<PedidosPendentes> fullList;
        private List<PedidosPendentes> filteredList;

        public PedidoDropdownAdapter(Context context, List<PedidosPendentes> pedidos) {
            super(context, 0, pedidos);
            this.fullList = new ArrayList<>(pedidos);
            this.filteredList = new ArrayList<>(pedidos);
        }

        @Override
        public int getCount() { return filteredList.size(); }

        @Override
        public PedidosPendentes getItem(int position) { return filteredList.get(position); }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView;
            if (v == null) v = LayoutInflater.from(getContext()).inflate(R.layout.item_pedido_dropdown, parent, false);
            TextView tvCodigo = v.findViewById(R.id.tvDropdownCodigo);
            TextView tvDetalhes = v.findViewById(R.id.tvDropdownDetalhes);
            PedidosPendentes p = getItem(position);
            if (p != null) {
                String cod = (p.codigo_pedido != null && !p.codigo_pedido.isEmpty()) ? p.codigo_pedido : "#" + p.id_emprestimo;
                tvCodigo.setText(cod);
                tvDetalhes.setText("Para: " + p.unidade + " | Item: " + p.nome_produto);
            }
            return v;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<PedidosPendentes> suggestions = new ArrayList<>();
                    if (constraint == null || constraint.length() == 0) {
                        suggestions.addAll(fullList);
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        for (PedidosPendentes item : fullList) {
                            if ((item.codigo_pedido != null && item.codigo_pedido.toLowerCase().contains(filterPattern)) ||
                                (String.valueOf(item.id_emprestimo).contains(filterPattern)) ||
                                (item.nome_produto != null && item.nome_produto.toLowerCase().contains(filterPattern))) {
                                suggestions.add(item);
                            }
                        }
                    }
                    results.values = suggestions;
                    results.count = suggestions.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredList.clear();
                    if (results != null && results.count > 0) {
                        filteredList.addAll((List) results.values);
                    }
                    notifyDataSetChanged();
                }

                @Override
                public CharSequence convertResultToString(Object resultValue) {
                    PedidosPendentes p = (PedidosPendentes) resultValue;
                    return p.codigo_pedido != null ? p.codigo_pedido : String.valueOf(p.id_emprestimo);
                }
            };
        }
    }
}
