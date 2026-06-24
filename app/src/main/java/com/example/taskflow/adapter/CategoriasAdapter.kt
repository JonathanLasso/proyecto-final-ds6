package com.example.taskflow.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskflow.dataBase.tablas.CategoriasConContador
import com.example.taskflow.databinding.ItemCategoriaBinding

class CategoriasAdapter(
    private var lista: List<CategoriasConContador> = emptyList()
) : RecyclerView.Adapter<CategoriasAdapter.CategoriaViewHolder>() {

    class CategoriaViewHolder(val binding: ItemCategoriaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val binding = ItemCategoriaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoriaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val item = lista[position]
        with(holder.binding) {
            tvNombreCategoria.text = item.nombre
            tvContadorTareas.text = "${item.cantidad_tareas} tareas"

            val icono = when (item.nombre) {
                "Personal" -> android.R.drawable.ic_menu_myplaces
                "Trabajo" -> android.R.drawable.ic_menu_gallery
                "Estudios" -> android.R.drawable.ic_menu_agenda
                "Compras" -> android.R.drawable.ic_menu_view
                else -> android.R.drawable.ic_menu_manage
            }
            ivIconoCategoria.setImageResource(icono)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<CategoriasConContador>) {
        this.lista = nuevaLista
        notifyDataSetChanged()
    }
}