package com.example.taskflow.dataBase.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.taskflow.dataBase.tablas.CategoriasEntity
import com.example.taskflow.dataBase.tablas.TareaEntity
import com.example.taskflow.dataBase.daos.CategoriasDao
import com.example.taskflow.dataBase.daos.EstadisticasDao
import com.example.taskflow.dataBase.daos.TareasDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoriasEntity::class,
        TareaEntity::class
    ],
    version = 2,
    exportSchema = false
)

abstract class TareaDb : RoomDatabase() {
    abstract fun categoriasDao(): CategoriasDao
    abstract fun tareasDao(): TareasDao
    abstract fun estadisticasDao(): EstadisticasDao

    companion object {
        @Volatile
        private var INSTANCE: TareaDb? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TareaDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TareaDb::class.java,
                    "tarea_database"
                )
                    .addCallback(TaskFlowDatabaseCallback(scope)) // Callback para precargar datos
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Callback para rellenar de forma automática las categorías del Wireframe 4
    private class TaskFlowDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.categoriasDao()
                    // Crear la lista basada en tus bloques temáticos
                    val categoriasPorDefecto = listOf(
                        CategoriasEntity(nombre = "Personal"),
                        CategoriasEntity(nombre = "Trabajo"),
                        CategoriasEntity(nombre = "Estudios"),
                        CategoriasEntity(nombre = "Compras")
                    )
                    dao.insertarCategoriasPorDefecto(categoriasPorDefecto)
                }
            }
        }
    }
}