package com.example.taskflow

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.taskflow.adapter.CategoriasAdapter
import com.example.taskflow.dataBase.app.TareaApp
import com.example.taskflow.dataBase.tablas.CategoriasConContador // 🌟 Importante
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
        categoriaAdapter = CategoriasAdapter(
            listaCategorias = emptyList(),
            onEliminarClick = { categoriaConContador ->
                // 🌟 Pasamos el objeto del tipo correcto
                mostrarDialogoConfirmacion(categoriaConContador)
            }
        )

        binding.rvCategorias.apply {
            layoutManager = GridLayoutManager(this@CategoriaActivity, 2)
            adapter = categoriaAdapter
        }
    }

    // 🌟 Modificado para recibir CategoriasConContador
    private fun mostrarDialogoConfirmacion(categoria: CategoriasConContador) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Categoría")
            .setMessage("¿Quieres eliminar la categoría '${categoria.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarCategoriaDeBaseDatos(categoria)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 🌟 Modificado para recibir CategoriasConContador y transformarlo para Room
    private fun eliminarCategoriaDeBaseDatos(categoriaConContador: CategoriasConContador) {
        lifecycleScope.launch {
            // Creamos la entidad que Room entiende usando los datos que ya tenemos
            val categoriaAEliminar = CategoriasEntity(
                id = categoriaConContador.id,
                nombre = categoriaConContador.nombre
            )

            database.categoriasDao().eliminarCategoria(categoriaAEliminar)
            Toast.makeText(this@CategoriaActivity, "Categoría '${categoriaConContador.nombre}' eliminada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerEstadisticasCategorias() {
        lifecycleScope.launch {
            database.categoriasDao().obtenerCategoriasConContador().collectLatest { lista ->
                // Ahora los tipos coinciden perfectamente (List<CategoriasConContador>)
                categoriaAdapter.actualizarLista(lista)
            }
        }
    }

    private fun guardarNuevaCategoria() {
        val nombreCat = binding.etNuevaCategoria.text.toString().trim()

        if (nombreCat.isNotEmpty()) {
            lifecycleScope.launch {
                val nuevaCategoria = CategoriasEntity(nombre = nombreCat)
                database.categoriasDao().insertarCategoria(nuevaCategoria)

                binding.etNuevaCategoria.text.clear()
                Toast.makeText(this@CategoriaActivity, "Categoría '$nombreCat' agregada", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Por favor escribe un nombre", Toast.LENGTH_SHORT).show()
        }
    }
}