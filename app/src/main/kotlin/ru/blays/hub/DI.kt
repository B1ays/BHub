package ru.blays.hub

import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    factory(named("debug")) { false /*BuildConfig.DEBUG*/ }
}