@file:Suppress("NOTHING_TO_INLINE")

package ru.blays.utils.android

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri

/**
 * Запрос в базу данных через [ContentResolver]
 * @param uri адрес поиска
 * @param projection запрашиваемые столбцы таблицы
 * @param sortOrder порядок сортировки результатов
 * @param selectionBuilder билдер поискового запроса
 * @return [Cursor] или `null`, если при запросе возникла ошибка
 */
fun ContentResolver.query(
    uri: Uri,
    vararg projection: String,
    sortOrder: SqlSortOrder? = null,
    selectionBuilder: MutableMap<String, String>.() -> Unit = {}
): Cursor? {
    val selectionsMap = buildMap(selectionBuilder)

    val projection = projection
    val sortOrder = sortOrder?.toFormattedString()
    val selection = selectionsMap.keys
        .fold("") { acc, key ->
            if(acc.isEmpty()) {
                "$key = ?"
            } else {
                "$acc AND $key = ?"
            }
        }
        .takeIf(String::isNotEmpty)
    val selectionArgs = selectionsMap.values
        .toTypedArray()
        .takeIf(Array<String>::isNotEmpty)

    return query(
        /* uri = */ uri,
        /* projection = */ projection,
        /* selection = */ selection,
        /* selectionArgs = */ selectionArgs,
        /* sortOrder = */ sortOrder
    )
}

/**
 * Представление [Cursor] как [Sequence].
 * Итерируется, пока [Cursor.moveToNext]
 */
inline fun Cursor.asSequence(): Sequence<Cursor> = generateSequence {
    if(moveToNext()) this else null
}


/**
 * Порядок сортировки результатов запроса
 */
sealed class SqlSortOrder {
    /**
     * По возрастанию значения поля [field]
     */
    data class ASC(override val field: String): SqlSortOrder()

    /**
     * По убыванию значения поля [field]
     */
    data class DESC(override val field: String): SqlSortOrder()

    /**
     * Поле по которому будет сортироваться результат
     */
    abstract val field: String

    /**
     * Лимит выдачи результатов. Максимум результатов
     */
    internal var limit: Int? = null

    /**
     * Установление лимита выдачи результатов
     */
    infix fun withLimit(limit: Int) = apply { this.limit = limit }
}

/**
 * Преобразование в строку запроса
 */
private fun SqlSortOrder.toFormattedString() = buildString {
    append(field)
    append(' ')
    when(this@toFormattedString) {
        is SqlSortOrder.ASC -> append("ASC")
        is SqlSortOrder.DESC -> append("DESC")
    }
    limit?.let { limit ->
        append(' ')
        append("LIMIT")
        append(' ')
        append(limit)
    }
}