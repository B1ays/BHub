package ru.blays.hub

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.android.inject
import ru.blays.hub.core.domain.components.rootComponents.RootComponent
import ru.blays.hub.core.ui.screens.rootContent.RootContent
import ru.blays.hub.core.ui.theme.BHubTheme

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val rootComponentFactory: RootComponent.Factory by inject()
        val rootComponent = retainedComponent(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        ) { rootContext ->
            rootComponentFactory(rootContext)
        }

        setContent {
            val themeState by rootComponent.themeStateFlow.collectAsState()
            BHubTheme(
                themePreferences = themeState
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootContent(component = rootComponent)
                }
            }
        }
    }

    // Function to check permission.
    @Suppress("NOTHING_TO_INLINE")
    private inline fun checkPermission(permission: String): Boolean {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    // Function to request permission.
    private fun checkPermissions(vararg permissions: String): Boolean {
        return permissions.fold(true) { result, permission ->
            result && checkPermission(permission)
        }
    }
}