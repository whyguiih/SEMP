package com.example.semp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.semp.models.PedidosPendentes;
import java.util.List;

public class EmprestadosAdapter extends RecyclerView.Adapter<EmprestadosAdapter.ViewHolder> {

    private List<PedidosPendentes> lista;
    private List<PedidosPendentes> listaOriginal;
    private OnRetornoClickListener listener;

    public interface OnRetornoClickListener {
        void onRetornoClick(PedidosPendentes pedido);
    }

    public EmprestadosAdapter(List<PedidosPendentes> lista, OnRetornoClickListener listener) {
        this.lista = new java.util.ArrayList<>(lista);
        this.listaOriginal = new java.util.ArrayList<>(lista);
        this.listener = listener;
    }

    public void filtrar(String texto) {
        lista.clear();
        if (texto.isEmpty()) {
            lista.addAll(listaOriginal);
        } else {
            String busca = texto.toLowerCase().trim();
            for (PedidosPendentes p : listaOriginal) {
                boolean matchProduto = p.nome_produto != null && p.nome_produto.toLowerCase().contains(busca);
                boolean matchPara = p.nome != null && p.nome.toLowerCase().contains(busca);
                boolean matchUnidade = p.unidade != null && p.unidade.toLowerCase().contains(busca);

                if (matchProduto || matchPara || matchUnidade) {
                    lista.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emprestado, parent, false);
        return new ViewHolder(view);
    }

    // Local: app/src/main/java/com/example/semp/EmprestadosAdapter.java

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PedidosPendentes p = lista.get(position);

        // Produto
        holder.tvProduto.setText(p.nome_produto != null ? p.nome_produto : "Produto N/A");

        // "Emprestado para" (Usando p.nome que é o campo do seu modelo para solicitante)
        holder.tvPara.setText("Emprestado para: " + (p.nome != null ? p.nome : "N/A"));

        // Unidade
        holder.tvUnidade.setText("Unidade: " + (p.unidade != null ? p.unidade : "N/A"));

        // Quantidade (Linkando com o ID tvItemQuant que você criou)
        holder.tvQuant.setText("Quant. Emprestada: " + p.quant);

        // Data
        holder.tvDatas.setText("Reserva: " + (p.data_reserva != null ? p.data_reserva : "N/A"));

        holder.btnRetorno.setOnClickListener(v -> listener.onRetornoClick(p));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProduto, tvPara, tvUnidade, tvQuant, tvDatas;
        Button btnRetorno;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProduto = itemView.findViewById(R.id.tvItemEmprestadoProduto);
            tvPara = itemView.findViewById(R.id.tvItemEmprestadoPara);
            tvUnidade = itemView.findViewById(R.id.tvItemEmprestadoUnidade);
            tvQuant = itemView.findViewById(R.id.tvItemQuant);
            tvDatas = itemView.findViewById(R.id.tvItemEmprestadoDatas);
            btnRetorno = itemView.findViewById(R.id.btnSolicitarRetorno);
        }
    }
}
