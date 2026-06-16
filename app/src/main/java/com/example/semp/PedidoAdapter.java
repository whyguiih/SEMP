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

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {

    private List<PedidosPendentes> listaPedidos;
    private int tipoLista; // 0 = Pendente, 1 = Confirmado
    private OnAcaoClickListener listener;

    public interface OnAcaoClickListener {
        void onAcaoClick(PedidosPendentes pedido, int acao);
    }

    // CONSTRUTOR ATUALIZADO: Agora ele exige receber o "tipoLista"
    public PedidoAdapter(List<PedidosPendentes> listaPedidos, int tipoLista, OnAcaoClickListener listener) {
        this.listaPedidos = listaPedidos;
        this.tipoLista = tipoLista;
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

        // ========================================================
        // LÓGICA DE SEPARAÇÃO VISUAL DOS BOTÕES
        // ========================================================
        if (tipoLista == 0) {
            // LISTA DE PENDENTES
            holder.btnAutorizar.setVisibility(View.VISIBLE);
            holder.btnRecusar.setVisibility(View.VISIBLE);

            holder.btnAutorizar.setText("Liberar");
            holder.btnRecusar.setText("Recusar");

            holder.btnAutorizar.setBackgroundColor(android.graphics.Color.parseColor("#1a4b9f")); // Azul
            holder.btnRecusar.setBackgroundColor(android.graphics.Color.parseColor("#ef5e31")); // Laranja

            holder.btnAutorizar.setOnClickListener(v -> listener.onAcaoClick(pedido, 1));
            holder.btnRecusar.setOnClickListener(v -> listener.onAcaoClick(pedido, 2));

        } else if (tipoLista == 1) {
            // LISTA DE CONFIRMADOS
            holder.btnRecusar.setVisibility(View.GONE); // Esconde o botão de recusar

            holder.btnAutorizar.setVisibility(View.VISIBLE);
            holder.btnAutorizar.setText("Remover da Tela");
            holder.btnAutorizar.setBackgroundColor(android.graphics.Color.parseColor("#555555")); // Fica Cinza

            // Retorna a ação "99" (Código que criamos para ocultar o item localmente)
            holder.btnAutorizar.setOnClickListener(v -> listener.onAcaoClick(pedido, 99));
        }
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