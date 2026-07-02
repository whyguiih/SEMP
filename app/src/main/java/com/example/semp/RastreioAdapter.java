package com.example.semp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.semp.models.Rastreio;
import java.util.List;

public class RastreioAdapter extends RecyclerView.Adapter<RastreioAdapter.ViewHolder> {

    private List<Rastreio> lista;

    public RastreioAdapter(List<Rastreio> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rastreio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rastreio r = lista.get(position);
        holder.tvCodigo.setText("Pedido: " + r.codigo);
        holder.tvRota.setText(r.unidade_original + " -> " + r.unidade_destino);
        holder.tvDatas.setText("Saída: " + r.data_saida + " | Chegada: " + r.data_entrada);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCodigo, tvRota, tvDatas;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCodigo = itemView.findViewById(R.id.tvRastreioCodigo);
            tvRota = itemView.findViewById(R.id.tvRastreioRota);
            tvDatas = itemView.findViewById(R.id.tvRastreioDatas);
        }
    }
}
