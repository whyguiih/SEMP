package com.example.semp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// A classe de modelo que representa os campos do seu banco de dados
data class ItemEstoque(
    val nome: String,
    val quantidade: String,
    val detalhe: String
)

// O Adaptador
class EstoqueAdapter(private val itens: List<ItemEstoque>) :
    RecyclerView.Adapter<EstoqueAdapter.EstoqueViewHolder>() {

    class EstoqueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNomeProduto: TextView = view.findViewById(R.id.tvNomeProduto)
        val tvQuantidade: TextView = view.findViewById(R.id.tvQuantidade)
        val tvDetalhe: TextView = view.findViewById(R.id.tvDetalhe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstoqueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_estoque, parent, false)
        return EstoqueViewHolder(view)
    }

    override fun onBindViewHolder(holder: EstoqueViewHolder, position: Int) {
        val item = itens[position]

        // Puxando e injetando as informações!
        holder.tvNomeProduto.text = item.nome
        holder.tvQuantidade.text = item.quantidade // Bloco Azul 1
        holder.tvDetalhe.text = item.detalhe       // Bloco Azul 2
    }

    override fun getItemCount(): Int = itens.size
}