package com.example.semp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PedidoAdapter(
    private val pedidos: List<PedidoPendente>,
    private val onAcao: (Int, Int) -> Unit // Onde Int, Int será (Id_do_Emprestimo, Novo_Status)
) : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    class PedidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNome: TextView = view.findViewById(R.id.tvNomeEmprestimo)
        val tvUnidade: TextView = view.findViewById(R.id.tvUnidadeEmprestimo)
        val tvData: TextView = view.findViewById(R.id.tvDataEmprestimo)
        val btnAprovar: Button = view.findViewById(R.id.btnAprovar)
        val btnRecusar: Button = view.findViewById(R.id.btnRecusar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        holder.tvNome.text = "Solicitante: ${pedido.nome}"
        holder.tvUnidade.text = "Unidade: ${pedido.unidade_natal}"
        holder.tvData.text = "Data da Reserva: ${pedido.data_reserva}"

        holder.btnAprovar.setOnClickListener { onAcao(pedido.id_emprestimo, 1) } // 1 = Aprovar
        holder.btnRecusar.setOnClickListener { onAcao(pedido.id_emprestimo, 2) } // 2 = Deletar
    }

    override fun getItemCount() = pedidos.size
}