package com.khaata.app

import android.app.Application
import com.khaata.app.agent.AgentTools
import com.khaata.app.agent.KhaataAgent
import com.khaata.app.agent.LiveApiManager
import com.khaata.app.agent.OfflineModelManager
import com.khaata.app.agent.OmniFlashManager
import com.khaata.app.data.AppDatabase
import com.khaata.app.data.CatalogRepository
import com.khaata.app.data.CatalogSeeder
import com.khaata.app.util.ConnectivityObserver
import com.khaata.app.util.TtsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class KhaataApp : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val db by lazy { AppDatabase.get(this) }
    val catalog by lazy { CatalogRepository(db) }
    val tools by lazy { AgentTools(db, catalog) }
    val connectivity by lazy { ConnectivityObserver(this) }
    val liveApi by lazy { LiveApiManager() }
    val offlineModel by lazy { OfflineModelManager(this) }
    val omniFlash by lazy { OmniFlashManager() }
    val tts by lazy { TtsManager(this) }
    val agent by lazy { KhaataAgent(tools, liveApi, offlineModel, connectivity) }

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            CatalogSeeder.seedIfEmpty(db)      // 50-item kirana catalog on first run
            offlineModel.warmUp()              // pre-warm Gemma if weights are on device
        }
    }
}
