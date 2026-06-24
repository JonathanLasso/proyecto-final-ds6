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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoriasEntity::class,
        TareaEntity::class
    ],
    version = 3,
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
                    .fallbackToDestructiveMigration()
                    .addCallback(TaskFlowDatabaseCallback(scope, context)) // 🌟 Pasamos el context
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class TaskFlowDatabaseCallback(
        private val scope: CoroutineScope,
        private val context: Context
    ) : Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)

            scope.launch(Dispatchers.IO) {
                val database = getDatabase(context, scope)
                val dao = database.categoriasDao()

                // 🌟 VALIDACIÓN: Solo insertamos si la tabla de categorías está completamente vacía
                val categoriasExistentes = dao.obtenerTodasLasCategorias().first()
                if (categoriasExistentes.isEmpty()) {
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