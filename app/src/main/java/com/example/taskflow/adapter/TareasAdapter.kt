package com.example.taskflow.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskflow.dataBase.tablas.TareaEntity
import com.example.taskflow.databinding.ItemTareaBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TareasAdapter(
    private var listaTareas: List<TareaEntity> = emptyList(),
    private val onTareaClick: (TareaEntity) -> Unit,       // Listener para click normal
    private val onEliminarClick: (TareaEntity) -> Unit     // Listener para la X
) : RecyclerView.Adapter<TareasAdapter.TareaViewHolder>() {

    class TareaViewHolder(val binding: ItemTareaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val binding = ItemTareaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TareaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = listaTareas[position]
        holder.binding.tvItemTitulo.text = tarea.titulo
        holder.binding.tvItemDescripcion.text = tarea.descripcion
        holder.binding.tvItemPrioridad.text = tarea.prioridad

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaLegible = formatoFecha.format(Date(tarea.fechaLimite))
        holder.binding.tvItemFecha.text = "Vence: $fechaLegible"

        // CORREGIDO: Se usa holder.binding.root para asignar el click a toda la tarjeta
        holder.binding.root.setOnClickListener {
            onTareaClick(tarea)
        }

        // Evento al presionar la X (Eliminar con confirmación)
        holder.binding.ivEliminarTarea.setOnClickListener {
            onEliminarClick(tarea)
        }
    }

    override fun getItemCount(): Int = listaTareas.size

    fun actualizarLista(nuevaLista: List<TareaEntity>) {
        this.listaTareas = nuevaLista
        notifyDataSetChanged()
    }
}