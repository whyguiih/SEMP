package com.example.continuacao;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.continuacao.models.Produto;
import java.util.List;

public class EstoqueAdapter extends RecyclerView.Adapter<EstoqueAdapter.ProdutoViewHolder> {

    private List<Produto> listaProdutos;
    private Context context;

    public EstoqueAdapter(List<Produto> listaProdutos, Context context) {
        this.listaProdutos = listaProdutos;
        this.context = context;
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

        holder.tvNome.setText(produto.nome);
        holder.tvCodigo.setText("Cód: " + produto.codigo);
        holder.tvQuantidade.setText("Em Estoque: " + produto.quant);

        // Carregar imagem se houver
        if (produto.foto != null && !produto.foto.isEmpty()) {
            try {
                String base64Data = produto.foto;
                if (base64Data.contains(",")) {
                    base64Data = base64Data.split(",")[1];
                }
                byte[] decodedString = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
                android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivProduto.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.ivProduto.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.ivProduto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // AGORA TODO MUNDO VAI PARA A TELA DE DETALHES DO PRODUTO
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProdutoDetalheActivity.class);
            intent.putExtra("PRODUTO_ID", String.valueOf(produto.id_estoque));
            intent.putExtra("PRODUTO_NOME", produto.nome != null ? produto.nome : "Sem nome");
            intent.putExtra("PRODUTO_CODIGO", produto.codigo != null ? produto.codigo : "");
            intent.putExtra("PRODUTO_DESC", produto.descricao != null ? produto.descricao : "");
            intent.putExtra("PRODUTO_QTD", String.valueOf(produto.quant));
            intent.putExtra("PRODUTO_COR", produto.cor != null ? produto.cor : "");
            intent.putExtra("PRODUTO_MARCA", produto.marca_ref != null ? produto.marca_ref : "");
            intent.putExtra("PRODUTO_UNI_NATAL", produto.uni_natal != null ? produto.uni_natal : "");
            intent.putExtra("PRODUTO_DESC_DETALHADA", produto.descricao_detalhada != null ? produto.descricao_detalhada : "");
            intent.putExtra("PRODUTO_FOTO", produto.foto != null ? produto.foto : "");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaProdutos.size();
    }

    public static class ProdutoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvCodigo, tvQuantidade;
        ImageView ivProduto;

        public ProdutoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Linka com os novos IDs que criamos no arquivo item_estoque.xml
            tvNome = itemView.findViewById(R.id.tvNomeProduto);
            tvCodigo = itemView.findViewById(R.id.tvCodigoProduto);
            tvQuantidade = itemView.findViewById(R.id.tvQuantidade);
            ivProduto = itemView.findViewById(R.id.ivProduto);
        }
    }
}