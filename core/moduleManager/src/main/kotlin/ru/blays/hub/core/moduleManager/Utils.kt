package ru.blays.hub.core.moduleManager

internal fun String.escapeAll(): String {
    return if(isEmpty()) this
    else map(::escapeChar).reduce(String::plus)
}

private inline fun escapeChar(char: Char) = "\\$char"

