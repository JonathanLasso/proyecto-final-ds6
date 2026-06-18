package com.example.taskflow.dataBase.daos

import androidx.room.*
import kotlinx.coroutines.flow.Flow

interface EstadisticasDao {

    @Query("SELECT COUNT(*) FROM tareas WHERE completada = 1")
    fun obtenerTotalTareasCompletadas(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tareas")
    fun obtenerTotalTareas(): Flow<Int>
}