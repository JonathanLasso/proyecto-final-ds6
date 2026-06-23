package com.example.taskflow.dataBase.daos

import androidx.room.*
import com.example.taskflow.dataBase.tablas.TareaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TareasDao {

    @Query("SELECT * FROM tareas")
    fun obtenerTodasLasTareas(): Flow<List<TareaEntity>>

    // 1. Consulta para ordenar por fecha
    @Query("SELECT * FROM tareas ORDER BY fechaLimite ASC")
    fun obtenerTareasPorFecha(): Flow<List<TareaEntity>>

    // 2. Consulta para ordenar por Prioridad (Modifica según cómo ordenes tus Strings)
    @Query("SELECT * FROM tareas ORDER BY prioridad ASC")
    fun obtenerTareasPorPrioridad(): Flow<List<TareaEntity>>

    // 3. Consulta para ver solo completadas
    @Query("SELECT * FROM tareas WHERE completada = 1")
    fun obtenerTareasCompletadas(): Flow<List<TareaEntity>>

    // 4. Consulta para ver solo pendientes
    @Query("SELECT * FROM tareas WHERE completada = 0")
    fun obtenerTareasPendientes(): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE id = :id LIMIT 1")
    suspend fun obtenerTareaPorId(id: Int): TareaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTarea(tarea: TareaEntity)

    @Update
    suspend fun actualizarTarea(tarea: TareaEntity)

    @Delete
    suspend fun eliminarTarea(tarea: TareaEntity)
}