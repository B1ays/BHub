package ru.blays.hub.core.preferences

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.lazyModule
import java.io.File

@OptIn(KoinExperimentalAPI::class)
val preferencesModule = lazyModule {
    single {
        val dataStore = DataStoreFactory.create(
            serializer = SettingsSerializer,
            produceFile = {
                File(get<Context>().filesDir, "Settings.pb")
            }
        )
        SettingsRepository(dataStore)
    }
}