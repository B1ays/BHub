package ru.blays.hub.core.packageManager.api

import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named

fun Koin.getPackageManager(type: PackageManagerType): PackageManager = get(named(type))
fun Koin.injectPackageManager(type: PackageManagerType): Lazy<PackageManager> = inject(named(type))

fun KoinComponent.getPackageManager(type: PackageManagerType): PackageManager = get(named(type))
fun KoinComponent.injectPackageManager(type: PackageManagerType): Lazy<PackageManager> = inject(named(type))