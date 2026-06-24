package com.example.taskflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskflow.dataBase.db.TareaDb
import com.example.taskflow.dataBase.tablas.CategoriasConContador
import com.example.taskflow.repository.EstadisticasRepository

class EstadisticasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EstadisticasRepository

    val totalCompletadas: LiveData<Int>
    val progresoPorcentaje: LiveData<Int>
    val listaCategorias: LiveData<List<CategoriasConContador>>

    init {
        val db = TareaDb.getDatabase(application, viewModelScope)
        repository = EstadisticasRepository(db.estadisticasDao(), db.categoriasDao())

        totalCompletadas = repository.totalTareasCompletadas.asLiveData()
        progresoPorcentaje = repository.porcentajeProgreso.asLiveData()
        listaCategorias = repository.categoriasConContador.asLiveData()
    }
}