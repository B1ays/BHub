package ru.blays.hub.core.ui.elements.placeholder

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.blays.hub.core.ui.utils.primaryColorAtAlpha

@Composable
fun FullscreenPlaceholder(
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int,
    @StringRes contentDescriptionId: Int? = null,
    message: String
) {
    Box(modifier = Modifier.fillMaxSize().then(modifier)) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = contentDescriptionId?.let { id ->
                    stringResource(id = id)
                },
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(130.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primaryColorAtAlpha(0.7F)
            )
        }
    }
}

@Composable
fun FullscreenPlaceholder(
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int,
    @StringRes contentDescriptionId: Int? = null,
    @StringRes messageId: Int
) = FullscreenPlaceholder(
    modifier = modifier,
    iconId = iconId,
    contentDescriptionId = contentDescriptionId,
    message = stringResource(id = messageId)
)