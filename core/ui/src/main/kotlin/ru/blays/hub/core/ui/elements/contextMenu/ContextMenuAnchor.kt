package ru.blays.hub.core.ui.elements.contextMenu

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import kotlin.math.roundToInt

class ContextMenuAnchor(
   var state: ContextMenuState
): Modifier.Node(), GlobalPositionAwareModifierNode {
    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        state.setSize(coordinates.size)
        state.setPosition(coordinates.positionInWindow())
    }
}

data class ContextMenuAnchorElement(
    val state: ContextMenuState
): ModifierNodeElement<ContextMenuAnchor>() {
    override fun create(): ContextMenuAnchor = ContextMenuAnchor(state)

    override fun update(node: ContextMenuAnchor) {
        node.state = state
    }
}

fun Modifier.contextMenuAnchor(state: ContextMenuState): Modifier {
    return this then ContextMenuAnchorElement(state)
}

class ContextMenuPositionProvider(
    private val positionInWindow: IntOffset,
    private val size: IntSize,
    private val offsetFromEdge: Int
): PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        var x: Int = positionInWindow.x + size.width/2 - popupContentSize.width/2
        var y: Int = (positionInWindow.y + size.height/2 - popupContentSize.height/2)
        if((y + popupContentSize.height) > windowSize.height) {
            y = (windowSize.height - popupContentSize.height - offsetFromEdge)
        }
        if(y < offsetFromEdge) {
            y = offsetFromEdge
        }
        if((x + popupContentSize.width) > windowSize.width) {
            x = windowSize.width - popupContentSize.width - offsetFromEdge
        }
        if(x < offsetFromEdge) {
            x = offsetFromEdge
        }
        return IntOffset(x, y)
    }
}

fun Offset.toIntOffset(): IntOffset = try {
    IntOffset(x.roundToInt(), y.roundToInt())
} catch (e: Exception) {
    IntOffset.Zero
}