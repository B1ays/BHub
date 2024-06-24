package ru.blays.hub.core.ui.utils

internal fun <T> List<T>.containsAll(vararg elements: T): Boolean {
    return elements.fold(true) { acc, element ->
        acc && contains(element)
    }
}