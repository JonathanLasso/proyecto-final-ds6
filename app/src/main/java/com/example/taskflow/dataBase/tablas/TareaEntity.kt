package com.example.taskflow.dataBase.tablas

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tareas",
    foreignKeys = [
        ForeignKey(
            entity = CategoriasEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoria_id"],
            onDelete = ForeignKey.CASCADE
        )
    ])
data class TareaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val prioridad: String,
    val completada: Boolean = false,
    val fechaLimite: Long,
    val categoria_id: Int,
    val progreso: Int = 0
)