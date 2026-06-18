package com.example.taskflow.dataBase.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import com.example.taskflow.dataBase.db.TareaDb

class TareaApp : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database: TareaDb by lazy {
        TareaDb.getDatabase(this, applicationScope)
    }
}