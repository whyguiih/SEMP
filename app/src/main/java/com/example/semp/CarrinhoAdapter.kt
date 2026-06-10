package com.example.semp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CarrinhoAdapter(
    private val itens: List<Produto>,
    private val onEditClick: (Produto) -> Unit,
    private val onDeleteClick: (Produto) -> Unit
) : RecyclerView.Adapter<CarrinhoAdapter.CarrinhoViewHolder>() {

    class CarrinhoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNomeProduto: TextView = view.findViewById(R.id.tvNomeProdutoCarrinho)
        val tvQuantidade: TextView = view.findViewById(R.id.tvQtdCarrinho)
        val btnEditar: ImageView = view.findViewById(R.id.btnEditarCarrinho)
        val btnDeletar: ImageView = view.findViewById(R.id.btnDeletarCarrinho)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarrinhoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carrinho, parent, false)
        return CarrinhoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarrinhoViewHolder, position: Int) {
        val item = itens[position]

        holder.tvNomeProduto.text = item.nome ?: "Sem Nome"

        // Pega a quantidade que já está no carrinho (se sua API retornar no campo "carrinho")
        val qtd = item.carrinho?.toString() ?: item.quant ?: "1"
        holder.tvQuantidade.text = "No carrinho: $qtd"

        // Aciona as funções baseadas nos cliques
        holder.btnEditar.setOnClickListener { onEditClick(item) }
        holder.btnDeletar.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount(): Int = itens.size
}