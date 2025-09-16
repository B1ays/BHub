package ru.blays.hub

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.blays.hub.core.domain.domainModule

val appModule = module {
    includes(domainModule)
    factory(named("debug")) { BuildConfig.DEBUG }
}