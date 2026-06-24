package com.example.taskflow

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.taskflow.databinding.ActivityEstadisticasBinding
import com.example.taskflow.viewmodel.EstadisticasViewModel

class EstadisticasActivity : AppCompatActivity() {

    private val viewModel: EstadisticasViewModel by viewModels()

    // Declaramos la variable del binding
    private lateinit var binding: ActivityEstadisticasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializamos el binding e inflamos la vista
        binding = ActivityEstadisticasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Observar Porcentaje de Progreso General
        viewModel.progresoPorcentaje.observe(this) { progreso ->
            binding.tvProgresoPorcentaje.text = "$progreso%"
            binding.circularProgressBar.progress = progreso
        }

        // 2. Observar Total de Tareas Completadas
        viewModel.totalCompletadas.observe(this) { completadas ->
            binding.tvTareasCompletadasTotal.text = "TAREAS COMPLETADAS: $completadas"
        }

        // 3. Observar Distribución por Categorías
        viewModel.listaCategorias.observe(this) { categorias ->
            for (cat in categorias) {
                val textoTarea = if (cat.cantidad_tareas == 1) "1 Tarea" else "${cat.cantidad_tareas} Tareas"
                val pesoBarra = cat.cantidad_tareas.toFloat()

                when (cat.nombre) {
                    "Personal" -> {
                        binding.tvCantPersonal.text = textoTarea
                        actualizarPesoBarra(binding.barPersonal, pesoBarra)
                    }
                    "Trabajo" -> {
                        binding.tvCantTrabajo.text = textoTarea
                        actualizarPesoBarra(binding.barTrabajo, pesoBarra)
                    }
                    "Estudios" -> {
                        binding.tvCantEstudios.text = textoTarea
                        actualizarPesoBarra(binding.barEstudios, pesoBarra)
                    }
                    "Compras" -> {
                        binding.tvCantCompras.text = textoTarea
                        actualizarPesoBarra(binding.barCompras, pesoBarra)
                    }
                }
            }
        }

        // Configuración del botón Volver
        binding.btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun actualizarPesoBarra(viewBarra: View, peso: Float) {
        val params = viewBarra.layoutParams as LinearLayout.LayoutParams
        if (peso > 0) {
            viewBarra.visibility = View.VISIBLE
            params.weight = peso
        } else {
            viewBarra.visibility = View.INVISIBLE
            params.weight = 0.01f
        }
        viewBarra.layoutParams = params
    }
}