package com.example.semp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.semp.models.PedidosPendentes;
import java.util.List;

public class MeusPedidosAdapter extends RecyclerView.Adapter<MeusPedidosAdapter.ViewHolder> {
    private List<PedidosPendentes> listaPedidos;

    public MeusPedidosAdapter(List<PedidosPendentes> listaPedidos) {
        this.listaPedidos = listaPedidos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meu_pedido, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PedidosPendentes pedido = listaPedidos.get(position);
        holder.tvPedidoId.setText("Pedido #" + pedido.id_emprestimo);
        holder.tvPedidoProdutos.setText("Produto: " + pedido.nome_produto + " (Qtd: " + pedido.quant + ")");
        
        String status;
        switch (pedido.aprovacao) {
            case 1:
                status = "Status: Aprovado";
                holder.tvPedidoStatus.setBackgroundColor(0xFFE8F5E9); // Green background
                holder.tvPedidoStatus.setTextColor(0xFF4CAF50); // Green text
                break;
            case 2:
                status = "Status: Recusado";
                holder.tvPedidoStatus.setBackgroundColor(0xFFFFEBEE); // Red background
                holder.tvPedidoStatus.setTextColor(0xFFF44336); // Red text
                break;
            default:
                status = "Status: Pendente";
                holder.tvPedidoStatus.setBackgroundColor(0xFFFFF3E0); // Orange background
                holder.tvPedidoStatus.setTextColor(0xFFFF9800); // Orange text
                break;
        }
        holder.tvPedidoStatus.setText(status);
    }

    @Override
    public int getItemCount() {
        return listaPedidos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPedidoId, tvPedidoProdutos, tvPedidoStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPedidoId = itemView.findViewById(R.id.tvPedidoId);
            tvPedidoProdutos = itemView.findViewById(R.id.tvPedidoProdutos);
            tvPedidoStatus = itemView.findViewById(R.id.tvPedidoStatus);
        }
    }
}
