package com.example.taskflow

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.taskflow.adapter.CategoriasAdapter
import com.example.taskflow.dataBase.app.TareaApp
import com.example.taskflow.dataBase.tablas.CategoriasEntity
import com.example.taskflow.databinding.ActivityCategoriaBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoriaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriaBinding
    private val database by lazy { (application as TareaApp).database }
    private lateinit var categoriaAdapter: CategoriasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecyclerView()

        binding.btnVolver.setOnClickListener {
            finish()
        }

        binding.btnAgregarCategoria.setOnClickListener {
            guardarNuevaCategoria()
        }

        obtenerEstadisticasCategorias()
    }

    private fun configurarRecyclerView() {
        categoriaAdapter = CategoriasAdapter()
        binding.rvCategorias.apply {
            // Asigna un layout de cuadrícula con 2 columnas de forma dinámica
            layoutManager = GridLayoutManager(this@CategoriaActivity, 2)
            adapter = categoriaAdapter
        }
    }

    private fun obtenerEstadisticasCategorias() {
        lifecycleScope.launch {
            database.categoriasDao().obtenerCategoriasConContador().collectLatest { lista ->
                // El adaptador recibe la lista de Room y se redibuja automáticamente
                categoriaAdapter.actualizarLista(lista)
            }
        }
    }

    private fun guardarNuevaCategoria() {
        val nombreCat = binding.etNuevaCategoria.text.toString().trim()

        if (nombreCat.isNotEmpty()) {
            lifecycleScope.launch {
                val nuevaCategoria = CategoriasEntity(nombre = nombreCat)
                database.categoriasDao().insertarCategoria(nuevaCategoria) // Asegúrate de tener este método @Insert en tu DAO

                // Limpiar el campo y avisar al usuario
                binding.etNuevaCategoria.text.clear()
                Toast.makeText(this@CategoriaActivity, "Categoría '$nombreCat' agregada", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Por favor escribe un nombre", Toast.LENGTH_SHORT).show()
        }
    }
}