package com.example.taskflow.repository

import com.example.taskflow.dataBase.daos.CategoriasDao
import com.example.taskflow.dataBase.daos.EstadisticasDao
import com.example.taskflow.dataBase.tablas.CategoriasConContador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class EstadisticasRepository(
    private val estadisticasDao: EstadisticasDao,
    private val categoriasDao: CategoriasDao
) {
    val totalTareasCompletadas: Flow<Int> = estadisticasDao.obtenerTotalTareasCompletadas()
    val totalTareas: Flow<Int> = estadisticasDao.obtenerTotalTareas()
    val categoriasConContador: Flow<List<CategoriasConContador>> = categoriasDao.obtenerCategoriasConContador()

    // Combina el total y las completadas para calcular el porcentaje dinámicamente
    val porcentajeProgreso: Flow<Int> = combine(totalTareas, totalTareasCompletadas) { total, completadas ->
        if (total > 0) (completadas * 100) / total else 0
    }
}