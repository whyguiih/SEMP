package com.example.semp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.semp.models.Produto;
import java.util.List;

public class CarrinhoAdapter extends RecyclerView.Adapter<CarrinhoAdapter.CarrinhoViewHolder> {

    private List<Produto> itens;
    private OnCarrinhoActionListener listener;
    private java.util.Set<Integer> itensSelecionados = new java.util.HashSet<>();

    public interface OnCarrinhoActionListener {
        void onEditClick(Produto produto);
        void onDeleteClick(Produto produto);
        void onSelectionChanged(java.util.List<Integer> idsSelecionados);
    }

    public CarrinhoAdapter(List<Produto> itens, OnCarrinhoActionListener listener) {
        this.itens = itens;
        this.listener = listener;
    }

    public static class CarrinhoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomeProduto, tvQuantidade;
        ImageView btnEditar, btnDeletar;
        android.widget.CheckBox cbSelecionar;

        public CarrinhoViewHolder(View view) {
            super(view);
            tvNomeProduto = view.findViewById(R.id.tvNomeProdutoCarrinho);
            tvQuantidade = view.findViewById(R.id.tvQtdCarrinho);
            btnEditar = view.findViewById(R.id.btnEditarCarrinho);
            btnDeletar = view.findViewById(R.id.btnDeletarCarrinho);
            cbSelecionar = view.findViewById(R.id.cbSelecionarProduto);
        }
    }

    @NonNull
    @Override
    public CarrinhoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carrinho, parent, false);
        return new CarrinhoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarrinhoViewHolder holder, int position) {
        Produto item = itens.get(position);

        holder.tvNomeProduto.setText(item.nome != null ? item.nome : "Sem Nome");

        // Carregar imagem se houver
        if (item.foto != null && !item.foto.isEmpty()) {
            try {
                String base64Data = item.foto;
                if (base64Data.contains(",")) {
                    base64Data = base64Data.split(",")[1];
                }
                byte[] decodedString = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
                android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.itemView.findViewById(R.id.ivProdutoCarrinho).setVisibility(View.VISIBLE);
                ((ImageView) holder.itemView.findViewById(R.id.ivProdutoCarrinho)).setImageBitmap(decodedByte);
            } catch (Exception e) {
                ((ImageView) holder.itemView.findViewById(R.id.ivProdutoCarrinho)).setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            ((ImageView) holder.itemView.findViewById(R.id.ivProdutoCarrinho)).setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // MOSTRAR QUANTIDADE: Prioridade total para o campo 'quantidade' que vem mapeado da API
        int qtdFinal = item.quantidade;
        if (qtdFinal <= 0) qtdFinal = item.carrinho;
        if (qtdFinal <= 0) qtdFinal = 1;
        
        holder.tvQuantidade.setText("No carrinho: " + qtdFinal);

        // Configurar Checkbox de seleção
        holder.cbSelecionar.setOnCheckedChangeListener(null);
        holder.cbSelecionar.setChecked(itensSelecionados.contains(item.id_estoque));
        holder.cbSelecionar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                itensSelecionados.add(item.id_estoque);
            } else {
                itensSelecionados.remove(item.id_estoque);
            }
            if (listener != null) {
                listener.onSelectionChanged(new java.util.ArrayList<>(itensSelecionados));
            }
        });

        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(item);
        });
        
        holder.btnDeletar.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }
}