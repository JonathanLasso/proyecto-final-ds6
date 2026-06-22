package com.example.taskflow

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.taskflow.dataBase.app.TareaApp
import com.example.taskflow.databinding.ActivityCategoriaBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoriaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriaBinding
    private val database by lazy { (application as TareaApp).database }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegresar.setOnClickListener {
            finish()
        }

        obtenerEstadisticasCategorias()
    }

    private fun obtenerEstadisticasCategorias() {
        lifecycleScope.launch {
            database.categoriasDao().obtenerCategoriasConContador().collectLatest { lista ->
                lista.forEach { item ->
                    val textoContador = "${item.cantidad_tareas} tareas"
                    when (item.nombre) {
                        "Personal" -> binding.tvCountPersonal.text = textoContador
                        "Trabajo" -> binding.tvCountTrabajo.text = textoContador
                        "Estudios" -> binding.tvCountEstudios.text = textoContador
                        "Compras" -> binding.tvCountCompras.text = textoContador
                    }
                }
            }
        }
    }
}