package ru.blays.hub.core.domain.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import ru.blays.hub.core.domain.R
import java.io.File

fun Context.openInBrowser(url: String) {
    kotlin.runCatching {
        Intent.createChooser(
            intent {
                action = Intent.ACTION_VIEW
                data = url.toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            getString(R.string.action_openInBrowser)
        ).let(::startActivity)
    }.onFailure {
        Toast.makeText(
            this,
            getString(R.string.error_openInBrowser),
            Toast.LENGTH_SHORT
        ).show()
    }
}

fun Context.copyToClipboard(text: String) {
    getSystemService(ClipboardManager::class.java)?.setPrimaryClip(
        ClipData.newPlainText("text", text)
    )?.also {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(
                this,
                getString(R.string.textCopied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

fun Context.share(text: String) {
    val intent = intent {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val chooser = Intent.createChooser(intent, "Share text").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(chooser)
}

fun Context.share(uri: Uri) {
    val intent = intent {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = contentResolver.getType(uri)
    }
    val chooser = Intent.createChooser(intent, "Share file").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(chooser)
}


fun Context.vibrate(length: Long) {
    getService<Vibrator>().vibrate(
        VibrationEffect.createOneShot(
            length,
            VibrationEffect.DEFAULT_AMPLITUDE
        )
    )
}

@Throws(NullPointerException::class)
inline fun <reified SERVICE: Any> Context.getService(): SERVICE {
    return getSystemService()!!
}

inline val Context.currentLanguage: String
    get() = resources.configuration.locales[0].language

inline val Context.fileProviderAuthority: String
    get() = "${packageName}.provider"

fun Context.getUriForFile(file: File): Uri {
    return FileProvider.getUriForFile(this, fileProviderAuthority, file)
}

inline val Context.packageUri: Uri
    get() = "package:$packageName".toUri()