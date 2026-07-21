package com.example.semp;

import android.content.Context;
import android.content.Intent;
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

public class EstoqueAdapter extends RecyclerView.Adapter<EstoqueAdapter.ProdutoViewHolder> {

    private List<Produto> listaProdutos;
    private Context context;
    // Cache de memória para evitar travamentos ao rolar a lista (Base64 é pesado)
    private LruCache<String, Bitmap> memoryCache;

    public EstoqueAdapter(List<Produto> listaProdutos, Context context) {
        this.listaProdutos = listaProdutos;
        this.context = context;

        // Configuração do tamanho do cache (1/8 da memória máxima disponível)
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @NonNull
    @Override
    public ProdutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_estoque, parent, false);
        return new ProdutoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoViewHolder holder, int position) {
        Produto produto = listaProdutos.get(position);

        holder.tvNome.setText(produto.nome != null ? produto.nome : "Sem nome");
        holder.tvCodigo.setText("Cód: " + (produto.codigo != null ? produto.codigo : "N/A"));
        holder.tvQuantidade.setText("Em Estoque: " + produto.quant);

        // Otimização: Carregamento de imagem usando Cache
        if (produto.foto != null && !produto.foto.isEmpty()) {
            Bitmap bitmapCached = memoryCache.get(String.valueOf(produto.id_estoque));
            if (bitmapCached != null) {
                holder.ivProduto.setImageBitmap(bitmapCached); // Usa do cache, muito rápido!
            } else {
                try {
                    String base64Data = produto.foto;
                    if (base64Data.contains(",")) {
                        base64Data = base64Data.split(",")[1];
                    }
                    byte[] decodedString = Base64.decode(base64Data, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    // Salva no cache para as próximas rolagens
                    memoryCache.put(String.valueOf(produto.id_estoque), decodedByte);
                    holder.ivProduto.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    holder.ivProduto.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
        } else {
            holder.ivProduto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProdutoDetalheActivity.class);
            intent.putExtra("PRODUTO_ID", String.valueOf(produto.id_estoque));
            intent.putExtra("PRODUTO_NOME", produto.nome != null ? produto.nome : "Sem nome");
            intent.putExtra("PRODUTO_CODIGO", produto.codigo != null ? produto.codigo : "");
            intent.putExtra("PRODUTO_DESC", produto.descricao != null ? produto.descricao : "");
            intent.putExtra("PRODUTO_QTD", String.valueOf(produto.quant));
            
            // NOVIDADE: Passa o estoque real calculado pelo servidor
            intent.putExtra("PRODUTO_QTD_REAL", String.valueOf(produto.estoque_real));

            intent.putExtra("PRODUTO_COR", produto.cor != null ? produto.cor : "");
            intent.putExtra("PRODUTO_MARCA", produto.marca_ref != null ? produto.marca_ref : "");
            intent.putExtra("PRODUTO_UNI_NATAL", produto.uni_natal != null ? produto.uni_natal : "");
            // Onde você passa o ID, NOME, CODIGO, etc., adicione esta linha:
            intent.putExtra("PRODUTO_UNIDADE_ATUAL", produto.unidade_atual != null ? produto.unidade_atual : "Não informada");
            intent.putExtra("PRODUTO_DESC_DETALHADA", produto.descricao_detalhada != null ? produto.descricao_detalhada : "");
            intent.putExtra("PRODUTO_FOTO", produto.foto != null ? produto.foto : "");
            
            // ADICIONE ESTAS TRÊS LINHAS:
            intent.putExtra("PRODUTO_ALTURA", String.valueOf(produto.altura));
            intent.putExtra("PRODUTO_COMPRIMENTO", String.valueOf(produto.comprimento));
            intent.putExtra("PRODUTO_PERIODO_RESERVA", produto.periodo_reserva != null ? produto.periodo_reserva : "");

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaProdutos != null ? listaProdutos.size() : 0;
    }

    public static class ProdutoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvCodigo, tvQuantidade;
        ImageView ivProduto;

        public ProdutoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNomeProduto);
            tvCodigo = itemView.findViewById(R.id.tvCodigoProduto);
            tvQuantidade = itemView.findViewById(R.id.tvQuantidade);
            ivProduto = itemView.findViewById(R.id.ivProduto);
        }
    }
}