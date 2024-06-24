package ru.blays.hub.core.ui.utils

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import com.gigamole.composeshadowsplus.common.ShadowsPlusDefaults
import com.gigamole.composeshadowsplus.rsblur.RSBlurShadowDefaults
import com.gigamole.composeshadowsplus.rsblur.rsBlurShadow
import com.gigamole.composeshadowsplus.softlayer.softLayerShadow

@SuppressLint("SuspiciousModifierThen")
@Composable
fun Modifier.shadowPlus(
    radius: Dp = ShadowsPlusDefaults.ShadowRadius,
    color: Color = ShadowsPlusDefaults.ShadowColor,
    shape: Shape = ShadowsPlusDefaults.ShadowShape,
    spread: Dp = ShadowsPlusDefaults.ShadowSpread,
    offset: DpOffset = ShadowsPlusDefaults.ShadowOffset,
    isAlignRadius: Boolean = RSBlurShadowDefaults.RSBlurShadowIsAlignRadius,
    isAlphaContentClip: Boolean = ShadowsPlusDefaults.IsAlphaContentClip
) = this then if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    softLayerShadow(
        radius = radius,
        color = color,
        shape = shape,
        spread = spread,
        offset = offset,
        isAlphaContentClip = isAlphaContentClip
    )
} else {
    rsBlurShadow(
        radius = radius,
        color = color,
        shape = shape,
        spread = spread,
        offset = offset,
        isAlignRadius = isAlignRadius,
        isAlphaContentClip = isAlphaContentClip
    )
}