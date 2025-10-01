package ru.blays.hub.core.domain.data

/**
 * App update channel type
 */
enum class UpdateChannelType {
    /**
     * Stable releases
     */
    STABLE,

    /**
     * Beta releases
     */
    BETA,

    /**
     * Nightly builds
     */
    NIGHTLY;
}