package com.example.taskflow.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData // IMPORTANTE: necesitas esta importación
import com.example.taskflow.dataBase.tablas.CategoriasEntity
import com.example.taskflow.repository.CategoriaRepository

class CategoriaViewModel(private val repository: CategoriaRepository) : ViewModel() {

    // 1. Quita los paréntesis ()
    // 2. Usa .asLiveData() para convertir el Flow a LiveData
    val todasLasCategorias: LiveData<List<CategoriasEntity>> = repository.todasLasCategorias.asLiveData()
}