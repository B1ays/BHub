@file:OptIn(ExperimentalContracts::class)

package ru.blays.utils.browser

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Набор утилит для открытия ссылок в браузере. По умолчанию использует `Custom Tabs`
 */
object BrowserHelper {
    /**
     * Открытие ссылки в браузере.
     * @param url ссылка
     */
    fun open(context: Context, url: String) {
        open(context, url.toUri())
    }

    /**
     * Открытие ссылки в браузере.
     * @param uri ссылка в виде [Uri]
     */
    fun open(context: Context, uri: Uri) {
        val customTabIntent = CustomTabsIntent.Builder().build()
        context.launchUrl(customTabIntent, uri)
    }

    /**
     * Открытие ссылки в браузере с дополнительными параметрами оформления
     * @param url ссылка
     * @param decorConfig параметры оформления
     */
    fun openWithDecor(
        context: Context,
        url: String,
        decorConfig: DecorConfig
    ) = openWithDecor(
        context = context,
        uri = url.toUri(),
        decorConfig = decorConfig
    )

    /**
     * Открытие ссылки в браузере с дополнительными параметрами оформления
     * @param uri ссылка в виде [Uri]
     * @param decorConfig параметры оформления
     */
    fun openWithDecor(
        context: Context,
        uri: Uri,
        decorConfig: DecorConfig
    ) {
        val customTabIntent = customTabIntent {
            val darkTheme = decorConfig.darkTheme
            val toolbarColor = decorConfig.toolbarColor
            val hideToolbar = decorConfig.hideToolbar
            val closeButtonIcon = decorConfig.closeButtonIcon

            val colorSchemeParams = colorSchemeParams {
                if(toolbarColor != null) {
                    setToolbarColor(toolbarColor)
                    setNavigationBarColor(toolbarColor)
                }
            }
            when(darkTheme) {
                true -> {
                    setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_DARK, colorSchemeParams)
                }
                false -> {
                    setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_LIGHT, colorSchemeParams)
                }
                else -> {
                    setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_SYSTEM, colorSchemeParams)
                }
            }

            setUrlBarHidingEnabled(hideToolbar)

            if(closeButtonIcon != null) {
                val drawable = context.getDrawable(closeButtonIcon)
                val bitmap = drawable?.toBitmap()
                bitmap?.let(::setCloseButtonIcon)
            }
        }

        context.launchUrl(customTabIntent, uri)
    }

    private fun Context.launchUrl(intent: CustomTabsIntent, uri: Uri) {
        startActivity(
            intent.intent.apply {
                data = uri
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    /**
     * Параметры оформлени для `Custom Tabs`
     */
    data class DecorConfig(
        val darkTheme: Boolean ? = null,
        @ColorInt val toolbarColor: Int? = null,
        val hideToolbar: Boolean = false,
        @DrawableRes val closeButtonIcon: Int? = null
    )
}

/**
 * Создание `Custom Tabs` интента
 * @param builder билдер интента
 */
private inline fun customTabIntent(
    builder: CustomTabsIntent.Builder.() -> Unit
): CustomTabsIntent {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }
    return CustomTabsIntent.Builder().apply(builder).build()
}

/**
 * Создание параметров цвета для `Custom Tabs`
 * @param builder билдер параметров
 */
private inline fun colorSchemeParams(
    builder: CustomTabColorSchemeParams.Builder.() -> Unit
): CustomTabColorSchemeParams {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }
    return CustomTabColorSchemeParams.Builder().apply(builder).build()
}