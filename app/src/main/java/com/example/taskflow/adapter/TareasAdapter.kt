package com.example.taskflow.adapter

import android.graphics.Color
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

        // Vinculación de datos existentes
        holder.binding.tvItemTitulo.text = tarea.titulo
        holder.binding.tvItemDescripcion.text = tarea.descripcion
        holder.binding.tvItemPrioridad.text = tarea.prioridad

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaLegible = formatoFecha.format(Date(tarea.fechaLimite))
        holder.binding.tvItemFecha.text = "Vence: $fechaLegible"

        // 1. ASIGNACIÓN DE LA CATEGORÍA (Mapeo basado en tus IDs de Room)
        holder.binding.tvItemCategoria.text = when(tarea.categoria_id) {
            1 -> "Categoría: Personal"
            2 -> "Categoría: Trabajo"
            3 -> "Categoría: Estudios"
            4 -> "Categoría: Compras"
            else -> "Categoría: General"
        }

        // 2. ASIGNACIÓN DEL ESTADO Y COLOR DINÁMICO
        if (tarea.completada || tarea.progreso == 100) {
            holder.binding.tvItemEstado.text = "100%"
            holder.binding.tvItemEstado.setTextColor(Color.parseColor("#4CAF50"))
            holder.binding.vEstadoIndicador.setBackgroundColor(Color.parseColor("#4CAF50")) // Barra Verde
        } else {
            holder.binding.tvItemEstado.text = "${tarea.progreso}%"
            holder.binding.tvItemEstado.setTextColor(Color.parseColor("#F44336"))
            holder.binding.vEstadoIndicador.setBackgroundColor(Color.parseColor("#F44336")) // Barra Roja
        }

        // Evento al presionar toda la tarjeta
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