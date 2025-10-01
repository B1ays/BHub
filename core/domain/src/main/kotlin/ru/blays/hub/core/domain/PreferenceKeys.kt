package ru.blays.hub.core.domain

import ru.blays.hub.core.domain.data.FilesSortType
import ru.blays.hub.core.domain.data.ThemePreferenceModel
import ru.blays.hub.core.domain.data.UpdateChannelType
import ru.blays.hub.core.downloader.DownloadMode
import ru.blays.hub.core.packageManager.api.PackageManagerType
import ru.blays.preferences.accessor.preferenceAccessor

internal val AppThemeAccessor = preferenceAccessor(
    key = "app_theme",
    defaultValue = ThemePreferenceModel(
        themeType = ThemePreferenceModel.ThemeType.SYSTEM,
        colorAccentType = ThemePreferenceModel.AccentType.Dynamic(1),
        amoledTheme = false
    )
)

internal val PackageManagerAccessor = preferenceAccessor(
    key = "package_manager",
    defaultValue = PackageManagerType.NonRoot
)

internal val DownloadModeAccessor = preferenceAccessor<DownloadMode>(
    key = "download_mode",
    defaultValue = DownloadMode.SingleTry
)

internal val RootModeAccessor = preferenceAccessor(
    key = "root_mode",
    defaultValue = false
)

val SortTypeAccessor = preferenceAccessor(
    key = "files_sort_type",
    defaultValue = FilesSortType.MODIFY
)

val SortReverseOrderAccessor = preferenceAccessor(
    key = "files_sort_reverse_order",
    defaultValue = true,
)

val CheckSelfUpdatesAccessor = preferenceAccessor(
key = "check_self_updates",
defaultValue = true,
)

val SelfUpdatesChannelAccessor = preferenceAccessor(
    key = "self_updates_channel",
    defaultValue = UpdateChannelType.STABLE,
)

val CheckAppsUpdatesAccessor = preferenceAccessor(
    key = "check_apps_updates",
    defaultValue = true,
)

val CheckUpdatesIntervalAccessor = preferenceAccessor(
    key = "check_apps_updates_interval",
    defaultValue = 24,
)