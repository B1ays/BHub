package ru.blays.hub.core.ui.elements.contextMenu

import android.os.Build
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.gigamole.composeshadowsplus.rsblur.rsBlurShadow
import com.gigamole.composeshadowsplus.softlayer.softLayerShadow

class ContextMenuState {
    private var _visible: Boolean by mutableStateOf(false)
    private var _position: Offset by mutableStateOf(Offset.Unspecified)
    private var _size: IntSize by mutableStateOf(IntSize.Zero)

    val visible: Boolean get() = _visible
    val position: Offset get() = _position
    val size: IntSize get() = _size

    fun setSize(size: IntSize) {
        _size = size
    }

    fun setPosition(position: Offset) {
        _position = position
    }

    fun show() {
        _visible = true
    }

    fun hide() {
        _visible = false
    }
}

@Composable
fun rememberContextMenuState(): ContextMenuState {
    return remember { ContextMenuState() }
}

@Composable
fun ContextMenu(
    modifier: Modifier = Modifier,
    state: ContextMenuState,
    scrollState: ScrollState = rememberScrollState(),
    ambientColor: Color = DefaultShadowColor,
    spotColor: Color = ambientColor,
    shadowRadius: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val expandedState = remember { MutableTransitionState(false) }
    expandedState.targetState = state.visible

    if(expandedState.currentState || expandedState.targetState) {
        val density = LocalDensity.current
        val popupPositionProvider = remember(state.position, state.size) {
            ContextMenuPositionProvider(
                positionInWindow = state.position.toIntOffset(),
                size = state.size,
                offsetFromEdge = with(density) { 12.dp.roundToPx() }
            )
        }

        Popup(
            popupPositionProvider = popupPositionProvider,
            onDismissRequest = state::hide
        ) {
            DropdownMenuContent(
                modifier = modifier,
                expandedState = expandedState,
                transformOriginState = remember { mutableStateOf(TransformOrigin.Center) },
                scrollState = scrollState,
                shadowColor = ambientColor,
                shadowRadius = shadowRadius,
                content = content
            )
        }
    }
}

@Suppress("ModifierParameter", "TransitionPropertiesLabel")
@Composable
fun DropdownMenuContent(
    modifier: Modifier = Modifier,
    expandedState: MutableTransitionState<Boolean>,
    transformOriginState: MutableState<TransformOrigin>,
    scrollState: ScrollState,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    shadowColor: Color,
    shadowRadius: Dp,
    content: @Composable() (ColumnScope.() -> Unit)
) {
    // Menu open/close animation.
    val transition = rememberTransition(expandedState, "DropDownMenu")

    val scale by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(
                    durationMillis = InTransitionDuration,
                    easing = LinearOutSlowInEasing
                )
            } else {
                // Expanded to dismissed.
                tween(
                    durationMillis = 1,
                    delayMillis = OutTransitionDuration - 1
                )
            }
        }
    ) { expanded ->
        if (expanded) 1f else 0.8f
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(durationMillis = 30)
            } else {
                // Expanded to dismissed.
                tween(durationMillis = OutTransitionDuration)
            }
        }
    ) { expanded ->
        if (expanded) 1f else 0f
    }

    Surface(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
            transformOrigin = transformOriginState.value
        }
        .run {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                softLayerShadow(
                    radius = shadowRadius,
                    color = shadowColor,
                    shape = MaterialTheme.shapes.medium,
                    offset = DpOffset(
                        shadowRadius/2,
                        shadowRadius/2
                    )
                )
            } else {
                rsBlurShadow(
                    radius = shadowRadius,
                    color = shadowColor,
                    shape = MaterialTheme.shapes.medium,
                    offset = DpOffset(
                        shadowRadius/2,
                        shadowRadius/2
                    )
                )
            }
        },
        shape = MaterialTheme.shapes.medium,
        color = containerColor
    ) {
        Column(
            modifier = modifier
                .padding(vertical = DropdownMenuVerticalPadding)
                .width(IntrinsicSize.Max)
                .verticalScroll(scrollState), content = content
        )
    }
}

// Size defaults.
internal val MenuVerticalMargin = 48.dp
private val DropdownMenuItemHorizontalPadding = 12.dp
internal val DropdownMenuVerticalPadding = 8.dp
private val DropdownMenuItemDefaultMinWidth = 112.dp
private val DropdownMenuItemDefaultMaxWidth = 280.dp

// Menu open/close animation.
internal const val InTransitionDuration = 120
internal const val OutTransitionDuration = 75