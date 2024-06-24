package ru.blays.hub.core.ui.elements.infoDialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.blays.hub.core.logic.components.IInfoDialogConfig
import ru.blays.hub.core.logic.components.InfoDialogComponent
import ru.blays.hub.core.ui.elements.spacers.HorizontalSpacer
import ru.blays.hub.core.ui.elements.spacers.VerticalSpacer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <C: IInfoDialogConfig, T: Any> InfoDialogContent(
    modifier: Modifier = Modifier,
    component: InfoDialogComponent<C, T>,
    onDismissRequest: () -> Unit,
    icon: @Composable (RowScope.() -> Unit)? = null,
    confirmButton: @Composable RowScope.() -> Unit,
    dismissButton: @Composable (RowScope.() -> Unit)? = null
) {
    val state = component.state
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if(icon != null) {
                        icon()
                        HorizontalSpacer(width = 10.dp)
                    }
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(6.dp)
                )
                Text(text = state.message)
                VerticalSpacer(height = 6.dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dismissButton?.invoke(this)
                    Spacer(modifier = Modifier.weight(1F))
                    confirmButton()
                }
            }
        }
    }
}