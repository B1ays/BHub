package ru.blays.hub.core.data

import androidx.room.Room
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.bind
import org.koin.dsl.lazyModule
import ru.blays.hub.core.data.repositories.CatalogsRepository
import ru.blays.hub.core.data.repositories.CatalogsRepositoryImpl

@OptIn(KoinExperimentalAPI::class)
val dataModule = lazyModule {
    single<AppDatabase> {
        Room.databaseBuilder(
            context = get(),
            klass = AppDatabase::class.java,
            name = DATABASE_NAME
        )
        .createFromAsset("databases/$DATABASE_NAME")
        .build()
    }
    single {
        CatalogsRepositoryImpl(get<AppDatabase>())
    } bind CatalogsRepository::class
}

private const val DATABASE_NAME = "app.db"