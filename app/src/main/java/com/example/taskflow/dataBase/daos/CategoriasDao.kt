package com.example.taskflow.dataBase.daos

import androidx.room.*
import com.example.taskflow.dataBase.tablas.CategoriasConContador
import com.example.taskflow.dataBase.tablas.CategoriasEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriasDao {
    //Pantalla para ver las categorias
    @Query("""
        SELECT c.id, c.nombre, COUNT(t.id) AS cantidad_tareas
        FROM categorias c
        LEFT JOIN tareas t ON c.id = t.categoria_id
        GROUP BY c.id
    """)
    fun obtenerCategoriasConContador(): Flow<List<CategoriasConContador>>

    // Para insertar una nueva categoria
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarCategoria(categoria: CategoriasEntity)

    //Para consultar todas las categorias
    @Query("SELECT * FROM categorias")
    suspend fun obtenerTodasLasCategorias(): Flow<List<CategoriasEntity>>

    //Para insertar categorias por defecto
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarCategoriasPorDefecto(categorias: List<CategoriasEntity>)
}