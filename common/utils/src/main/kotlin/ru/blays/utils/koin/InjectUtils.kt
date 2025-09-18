@file:Suppress("NAME_SHADOWING")

package ru.blays.utils.koin

import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.ParametersHolder
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.Qualifier

/**
 * Получение объекта из Koin с передачей дополнительных параметров.
 * @param parameters зависимости объекта
 * @param qualifier квалификатор объекта
 */
inline fun <reified T: Any> Koin.get(
    vararg parameters: Any,
    qualifier: Qualifier? = null,
): T {
    val parameters = parameters
    return get<T>(qualifier, parameters::toParameters)
}

/**
 * Получение объекта из Koin с передачей дополнительных параметров.
 * @param parameters зависимости объекта
 * @param qualifier квалификатор объекта
 */
inline fun <reified T: Any> KoinComponent.get(
    vararg parameters: Any,
    qualifier: Qualifier? = null,
): T {
    val parameters = parameters
    return get<T>(qualifier, parameters::toParameters)
}

/**
 * Ленивое получение объекта из Koin с передачей дополнительных параметров.
 * @param parameters зависимости объекта
 * @param qualifier квалификатор объекта
 */
inline fun <reified T: Any> Koin.inject(
    vararg parameters: Any,
    qualifier: Qualifier? = null,
): Lazy<T> {
    val parameters = parameters
    return inject<T>(qualifier, parameters = parameters::toParameters)
}

/**
 * Ленивое получение объекта из Koin с передачей дополнительных параметров.
 * @param parameters зависимости объекта
 * @param qualifier квалификатор объекта
 */
inline fun <reified T: Any> KoinComponent.inject(
    vararg parameters: Any,
    qualifier: Qualifier? = null,
): Lazy<T> {
    val parameters = parameters
    return inject<T>(qualifier, parameters = parameters::toParameters)
}

/**
 * Создание [ParametersHolder] с значениями из массива.
 * @return [ParametersHolder]
 */
@PublishedApi
internal fun Array<*>.toParameters(): ParametersHolder = parametersOf(*this)