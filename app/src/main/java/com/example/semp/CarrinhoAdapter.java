package com.example.semp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.LruCache;
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

    // Cache de memória para imagens Base64
    private LruCache<String, Bitmap> memoryCache;

    public interface OnCarrinhoActionListener {
        void onEditClick(Produto produto);
        void onDeleteClick(Produto produto);
        void onSelectionChanged(java.util.List<Integer> idsSelecionados);
    }

    public CarrinhoAdapter(List<Produto> itens, OnCarrinhoActionListener listener) {
        this.itens = itens;
        this.listener = listener;

        // Configuração do cache (1/8 da memória máxima)
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public static class CarrinhoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomeProduto, tvQuantidade;
        ImageView btnEditar, btnDeletar, ivProdutoCarrinho;
        android.widget.CheckBox cbSelecionar;

        public CarrinhoViewHolder(View view) {
            super(view);
            tvNomeProduto = view.findViewById(R.id.tvNomeProdutoCarrinho);
            tvQuantidade = view.findViewById(R.id.tvQtdCarrinho);
            btnEditar = view.findViewById(R.id.btnEditarCarrinho);
            btnDeletar = view.findViewById(R.id.btnDeletarCarrinho);
            cbSelecionar = view.findViewById(R.id.cbSelecionarProduto);
            ivProdutoCarrinho = view.findViewById(R.id.ivProdutoCarrinho);
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

        // Otimização de imagem com cache
        if (item.foto != null && !item.foto.isEmpty()) {
            holder.ivProdutoCarrinho.setVisibility(View.VISIBLE);
            String cacheKey = String.valueOf(item.id_estoque) + "_carrinho";
            Bitmap bitmapCached = memoryCache.get(cacheKey);

            if (bitmapCached != null) {
                holder.ivProdutoCarrinho.setImageBitmap(bitmapCached);
            } else {
                try {
                    String base64Data = item.foto;
                    if (base64Data.contains(",")) base64Data = base64Data.split(",")[1];
                    byte[] decodedString = Base64.decode(base64Data, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    memoryCache.put(cacheKey, decodedByte);
                    holder.ivProdutoCarrinho.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    holder.ivProdutoCarrinho.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
        } else {
            holder.ivProdutoCarrinho.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        int qtdFinal = item.quantidade > 0 ? item.quantidade : (item.carrinho > 0 ? item.carrinho : 1);
        holder.tvQuantidade.setText("Quant.: " + qtdFinal);

        holder.cbSelecionar.setOnCheckedChangeListener(null);
        holder.cbSelecionar.setChecked(itensSelecionados.contains(item.id_estoque));
        holder.cbSelecionar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) itensSelecionados.add(item.id_estoque);
            else itensSelecionados.remove(item.id_estoque);

            if (listener != null) listener.onSelectionChanged(new java.util.ArrayList<>(itensSelecionados));
        });

        holder.btnEditar.setOnClickListener(v -> { if (listener != null) listener.onEditClick(item); });
        holder.btnDeletar.setOnClickListener(v -> { if (listener != null) listener.onDeleteClick(item); });
    }

    @Override
    public int getItemCount() {
        return itens != null ? itens.size() : 0;
    }
}