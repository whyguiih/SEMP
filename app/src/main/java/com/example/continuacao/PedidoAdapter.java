package com.example.continuacao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.continuacao.models.PedidosPendentes;
import java.util.List;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {

    private List<PedidosPendentes> listaPedidos;
    private OnAcaoClickListener listener;

    public interface OnAcaoClickListener {
        void onAcaoClick(PedidosPendentes pedido, int novoStatus);
    }

    public PedidoAdapter(List<PedidosPendentes> listaPedidos, OnAcaoClickListener listener) {
        this.listaPedidos = listaPedidos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        PedidosPendentes pedido = listaPedidos.get(position);

        holder.tvPedidoNome.setText("Solicitante: " + pedido.nome);
        holder.tvPedidoUnidade.setText("Unidade: " + pedido.unidade);
        holder.tvPedidoProdutos.setText("Produtos: " + pedido.nome_produto + " (Qtd: " + pedido.quant + ")");
        holder.tvPedidoPrioridade.setText("Prioridade: " + (pedido.prioridade != null ? pedido.prioridade : "Normal"));
        holder.tvPedidoMotivo.setText("Motivo: " + (pedido.motivo != null ? pedido.motivo : "Não informado"));

        holder.btnAutorizar.setOnClickListener(v -> {
            if (listener != null) listener.onAcaoClick(pedido, 1); // 1 = Aprovado
        });

        holder.btnRecusar.setOnClickListener(v -> {
            if (listener != null) listener.onAcaoClick(pedido, 2); // 2 = Recusado
        });
    }

    @Override
    public int getItemCount() {
        return listaPedidos.size();
    }

    public static class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView tvPedidoNome, tvPedidoUnidade, tvPedidoProdutos, tvPedidoPrioridade, tvPedidoMotivo;
        Button btnAutorizar, btnRecusar;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Puxando os IDs corretos do item_pedido.xml
            tvPedidoNome = itemView.findViewById(R.id.tvPedidoNome);
            tvPedidoUnidade = itemView.findViewById(R.id.tvPedidoUnidade);
            tvPedidoProdutos = itemView.findViewById(R.id.tvPedidoProdutos);
            tvPedidoPrioridade = itemView.findViewById(R.id.tvPedidoPrioridade);
            tvPedidoMotivo = itemView.findViewById(R.id.tvPedidoMotivo);
            btnAutorizar = itemView.findViewById(R.id.btnAutorizar);
            btnRecusar = itemView.findViewById(R.id.btnRecusar);
        }
    }
}