package ru.blays.hub.core.ui.elements.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.blays.hub.core.ui.elements.enhancedSurface.CustomSurface

@Composable
fun CustomIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    containerColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8F),
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shadowElevation: Dp = 0.dp,
    shadowColor: Color = DefaultShadowColor,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(6.dp),
    content: @Composable RowScope.() -> Unit
) {
    CustomSurface(
        onClick = onClick,
        modifier = modifier.semantics { role = Role.Button },
        enabled = enabled,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        shadowElevation = shadowElevation,
        shadowColor = shadowColor
    ) {
        Row(
            Modifier.padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun BackgroundedIcon(
    modifier: Modifier = Modifier,
    icon: Painter,
    shape: Shape = ButtonDefaults.shape,
    size: Dp = 50.dp,
    iconScale: Float = 1F,
    containerColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8F),
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    contentPadding: PaddingValues = PaddingValues(6.dp),
) {
    Box(
        modifier = modifier
            .background(
                color = containerColor,
                shape = shape
            )
    ) {
        Icon(
            modifier = Modifier
                .size(size)
                .padding(contentPadding)
                .scale(iconScale),
            painter = icon,
            contentDescription = null,
            tint = contentColor
        )
    }
}