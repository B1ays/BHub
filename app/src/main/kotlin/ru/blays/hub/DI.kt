package ru.blays.hub

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.blays.hub.core.domain.domainModule
import ru.blays.preferences.api.PreferencesHolder
import ru.blays.preferences.default.PreferenceHolderProvider

val appModule = module {
    includes(domainModule)
    factory(named("debug")) { BuildConfig.DEBUG }
    single<PreferencesHolder> { PreferenceHolderProvider.getInstance() }
}