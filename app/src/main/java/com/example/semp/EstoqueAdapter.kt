package com.example.semp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adicionado o campo 'imagemRes' para vincular imagens ao modelo de dados
data class ItemEstoque(
    val nome: String,
    val quantidade: String,
    val detalhe: String,
    val imagemRes: Int
)

class EstoqueAdapter(private val itens: List<ItemEstoque>) :
    RecyclerView.Adapter<EstoqueAdapter.EstoqueViewHolder>() {

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

        holder.tvNomeProduto.text = item.nome
        holder.tvQuantidade.text = item.quantidade
        holder.tvDetalhe.text = item.detalhe

        // Define a imagem do produto vinda da sua fonte de dados
        holder.ivProduto.setImageResource(item.imagemRes)
    }

    override fun getItemCount(): Int = itens.size
}