package com.example.taskflow.dataBase.daos

import androidx.room.*
import com.example.taskflow.dataBase.tablas.TareaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TareasDao {

    //Pantalla de inicio
    @Query("SELECT * FROM tareas")
    fun obtenerTodasLasTareas(): Flow<List<TareaEntity>>
    //Consulta para obtener por id
    @Query("SELECT * FROM tareas WHERE id = :id LIMIT 1")
    suspend fun obtenerTareaPorId(id: Int): TareaEntity?

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