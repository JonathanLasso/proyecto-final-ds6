package com.example.taskflow.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskflow.dataBase.tablas.CategoriasConContador // 🌟 Cambiado al POJO con contador
import com.example.taskflow.databinding.ItemCategoriaBinding

class CategoriasAdapter(
    private var listaCategorias: List<CategoriasConContador> = emptyList(), // 🌟 Cambiado aquí
    private val onEliminarClick: (CategoriasConContador) -> Unit // 🌟 Cambiado aquí
) : RecyclerView.Adapter<CategoriasAdapter.CategoriaViewHolder>() {

    class CategoriaViewHolder(val binding: ItemCategoriaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val binding = ItemCategoriaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoriaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoriaConContador = listaCategorias[position]

        // Vinculación de datos usando las propiedades de tu POJO
        holder.binding.tvNombreCategoria.text = categoriaConContador.nombre

        // 🌟 Ahora sí puedes mostrar el conteo real de tareas dinámicamente
        holder.binding.tvContadorTareas.text = "${categoriaConContador.cantidad_tareas} tareas"

        // Evento al presionar la X
        holder.binding.ivEliminarCategoria.setOnClickListener {
            onEliminarClick(categoriaConContador)
        }
    }

    override fun getItemCount(): Int = listaCategorias.size

    // 🌟 Cambiado el tipo de parámetro esperado
    fun actualizarLista(nuevaLista: List<CategoriasConContador>) {
        this.listaCategorias = nuevaLista
        notifyDataSetChanged()
    }
}