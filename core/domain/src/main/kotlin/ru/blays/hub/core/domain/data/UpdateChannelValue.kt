package ru.blays.hub.core.domain.data

import ru.blays.hub.core.domain.UPDATES_SOURCE_URL_BETA
import ru.blays.hub.core.domain.UPDATES_SOURCE_URL_NIGHTLY
import ru.blays.hub.core.domain.UPDATES_SOURCE_URL_STABLE
import ru.blays.hub.core.preferences.proto.UpdateChannel

fun getUpdateChannelUrl(channel: UpdateChannel): String = when(channel) {
    UpdateChannel.STABLE -> UPDATES_SOURCE_URL_STABLE
    UpdateChannel.BETA -> UPDATES_SOURCE_URL_BETA
    UpdateChannel.NIGHTLY -> UPDATES_SOURCE_URL_NIGHTLY
    UpdateChannel.UNRECOGNIZED -> throw IllegalStateException("Unrecognized update channel")
}