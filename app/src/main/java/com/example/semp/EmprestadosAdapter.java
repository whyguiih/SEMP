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
    private OnRetornoClickListener listener;

    public interface OnRetornoClickListener {
        void onRetornoClick(PedidosPendentes pedido);
    }

    public EmprestadosAdapter(List<PedidosPendentes> lista, OnRetornoClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emprestado, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PedidosPendentes p = lista.get(position);
        holder.tvProduto.setText(p.nome_produto);
        holder.tvQuant.setText("Qtd: " + p.quant);
        holder.tvStatus.setText(p.aprovacao == 1 ? "Aprovado" : "Pendente");
        
        holder.btnRetorno.setOnClickListener(v -> listener.onRetornoClick(p));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProduto, tvQuant, tvStatus;
        Button btnRetorno;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProduto = itemView.findViewById(R.id.tvItemEmprestadoProduto);
            tvQuant = itemView.findViewById(R.id.tvItemEmprestadoQuant);
            tvStatus = itemView.findViewById(R.id.tvItemEmprestadoStatus);
            btnRetorno = itemView.findViewById(R.id.btnSolicitarRetorno);
        }
    }
}
