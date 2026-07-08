package com.khaata.app

import android.app.Application
import com.khaata.app.data.AppDatabase
import com.khaata.app.data.CatalogSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class KhaataApp : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    lateinit var db: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.get(this)
        appScope.launch { CatalogSeeder.seedIfEmpty(db) }
    }
}
