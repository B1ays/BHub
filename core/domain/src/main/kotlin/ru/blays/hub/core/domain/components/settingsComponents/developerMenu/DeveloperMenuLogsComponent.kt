package ru.blays.hub.core.domain.components.settingsComponents.developerMenu

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.hub.core.logger.Logger
import ru.blays.hub.core.domain.utils.copyToClipboard
import ru.blays.hub.core.domain.utils.getUriForFile
import ru.blays.hub.core.domain.utils.readableSize
import ru.blays.hub.core.domain.utils.share

class DeveloperMenuLogsComponent(
    componentContext: ComponentContext,
    private val onOutput: (Output) -> Unit
): ComponentContext by componentContext, KoinComponent {
    private val context: Context by inject()

    private val logFile = Logger.file

    private val _state = MutableStateFlow(
        State(
            logs = Logger.logs,
            fileSize = logFile.readableSize
        )
    )

    val state: StateFlow<State>
        get() = _state.asStateFlow()

    fun sendIntent(intent: Intent) {
        when(intent) {
            Intent.Refresh -> refresh()
            Intent.ShareLogs -> shareLogs()
            Intent.ShareLogsFile -> shareLogsFile()
            Intent.CopyLogs -> copyLogs()
        }
    }

    fun onOutput(output: Output) {
        onOutput.invoke(output)
    }

    private fun refresh() {
        _state.update {
            it.copy(
                logs = Logger.logs,
                fileSize = logFile.readableSize
            )
        }
    }

    private fun shareLogs() {
        val log = state.value.logs.joinToString("\n")
        context.share(log)
    }

    private fun shareLogsFile() {
        val uri = context.getUriForFile(logFile)
        context.share(uri)
    }

    private fun copyLogs() {
        val log = state.value.logs.joinToString("\n")
        context.copyToClipboard(log)
    }

    sealed class Intent {
        data object Refresh: Intent()
        data object CopyLogs: Intent()
        data object ShareLogs: Intent()
        data object ShareLogsFile: Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
    }

    data class State(
        val logs: List<String>,
        val fileSize: String
    )
}