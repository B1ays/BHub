package ru.blays.hub.core.domain.data


/**
 * Represents the different ways files can be sorted.
 */
enum class FilesSortType {
    /**
     * Sort files by name.
     */
    NAME,

    /**
     * Sort files by size.
     */
    SIZE,

    /**
     * Sort files by modification date.
     */
    MODIFY,
}