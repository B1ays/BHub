package ru.blays.hub.core.ui.elements.lazyListItems

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.blays.hub.core.ui.elements.spacers.VerticalSpacer

@Suppress("NonSkippableComposable")
inline fun LazyListScope.groupWithTitle(
    title: String,
    content: LazyListScope.() -> Unit
) {
    item {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Start
        )
    }
    content()
    item { VerticalSpacer(height = 12.dp) }
}

@Suppress("NonSkippableComposable")
inline fun LazyListScope.groupWithTitle(
    crossinline title: @Composable () -> Unit,
    content: LazyListScope.() -> Unit
) {
    item { title() }
    content()
    item { VerticalSpacer(height = 8.dp) }
}

inline fun <T> LazyListScope.items(
    items: Set<T>,
    noinline key: ((item: T) -> Any)? = null,
    noinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit
) = items(
    count = items.size,
    key = if (key != null) { index: Int -> key(items.elementAt(index)) } else null,
    contentType = { index: Int -> contentType(items.elementAt(index)) }
) {
    itemContent(items.elementAt(it))
}

