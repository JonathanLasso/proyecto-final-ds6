package com.example.taskflow.repository

import com.example.taskflow.dataBase.daos.CategoriasDao
import com.example.taskflow.dataBase.tablas.CategoriasEntity
import kotlinx.coroutines.flow.Flow

class CategoriaRepository(private val categoriasDao: CategoriasDao) {

    //Obtener lista simple para el menú (susceptible de cambios)
    val todasLasCategorias: Flow<List<CategoriasEntity>> = categoriasDao.obtenerTodasLasCategorias()
}