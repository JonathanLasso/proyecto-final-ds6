package com.example.taskflow.dataBase.daos

import androidx.room.*
import com.example.taskflow.dataBase.tablas.TareaEntity
import kotlinx.coroutines.flow.Flow

interface TareasDao {

    //Pantalla de inicio
    @Query("SELECT * FROM tareas")
    fun obtenerTodasLasTareas(): Flow<List<TareaEntity>>

    //Pantalla de insertar
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTarea(tarea: TareaEntity)
    //Pantalla de actualizar
    @Update
    suspend fun actualizarTarea(tarea: TareaEntity)
    //Boton de eliminar
    @Delete
    suspend fun eliminarTarea(tarea: TareaEntity)
}