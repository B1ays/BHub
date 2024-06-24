package ru.blays.hub.core.ui.elements.buttons

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.blays.hub.core.ui.elements.spacers.HorizontalSpacer
import ru.blays.hub.core.ui.utils.primaryColorAtAlpha

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    @StringRes text: Int,
    @DrawableRes icon: Int,
    @StringRes contentDescription: Int?,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryColorAtAlpha(0.25F)
        )
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription?.let { stringResource(it) },
            modifier = Modifier.size(22.dp),
        )
        HorizontalSpacer(4.dp)
        Text(
            text = stringResource(text),
            style = MaterialTheme.typography.labelLarge
        )
    }
}