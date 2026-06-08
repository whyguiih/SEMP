package com.example.semp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EstoqueAdapter(
    private val itens: List<Produto>,
    private val onItemClick: ((Produto) -> Unit)? = null // Callback de clique adicionado
) : RecyclerView.Adapter<EstoqueAdapter.EstoqueViewHolder>() {

    class EstoqueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNomeProduto: TextView = view.findViewById(R.id.tvNomeProduto)
        val tvQuantidade: TextView = view.findViewById(R.id.tvQuantidade)
        val tvDetalhe: TextView = view.findViewById(R.id.tvDetalhe)
        val ivProduto: ImageView = view.findViewById(R.id.ivProduto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstoqueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_estoque, parent, false)
        return EstoqueViewHolder(view)
    }

    override fun onBindViewHolder(holder: EstoqueViewHolder, position: Int) {
        val item = itens[position]

        holder.tvNomeProduto.text = item.nome ?: "Sem Nome"
        holder.tvQuantidade.text = "Qtd: ${item.quant ?: "0"}"
        holder.tvDetalhe.text = item.descricao ?: "Sem detalhes disponíveis"
        holder.ivProduto.setImageResource(android.R.drawable.ic_menu_gallery)

        // Aciona o clique e passa o produto adiante
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    override fun getItemCount(): Int = itens.size
}