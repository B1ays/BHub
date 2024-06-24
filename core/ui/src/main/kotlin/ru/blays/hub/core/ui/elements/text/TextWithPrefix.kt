package ru.blays.hub.core.ui.elements.text

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextWithPrefix(
    modifier: Modifier = Modifier,
    prefix: String,
    text: String,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onTextClick: (String) -> Unit
) {
    Row {
        Text(
            text = "$prefix: ",
            style = style,
            color = color
        )
        Text(
            text = text,
            style = style,
            color = color,
            modifier = modifier.combinedClickable(
                onClick = {},
                onLongClick = { onTextClick(text) }
            )
        )
    }
}