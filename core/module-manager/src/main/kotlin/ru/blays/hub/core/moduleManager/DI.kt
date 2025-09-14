package ru.blays.hub.core.moduleManager

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.bind
import org.koin.dsl.lazyModule

@OptIn(KoinExperimentalAPI::class)
val moduleManagerModule = lazyModule {
    single { params ->
        val adapter: LoggerAdapter = params.getOrNull() ?: DefaultLoggerAdapter()
        ModuleManagerImpl2(
            loggerAdapter = adapter,
            context = get()
        )
    } bind ModuleManager::class
}