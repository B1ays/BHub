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
import com.arkivanov.decompose.retainedComponent
import ru.blays.hub.core.logic.components.rootComponents.RootComponent
import ru.blays.hub.core.ui.screens.rootContent.RootContent
import ru.blays.hub.core.ui.theme.BHubTheme

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val rootComponent = retainedComponent { rootContext ->
            RootComponent(rootContext)
        }

        setContent {
            val themeState by rootComponent.themeStateFlow.collectAsState()
            BHubTheme(
                themeSettings = themeState
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