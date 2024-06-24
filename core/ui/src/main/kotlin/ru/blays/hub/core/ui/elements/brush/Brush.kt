package ru.blays.hub.core.ui.elements.brush

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush

fun largeRadialGradientBrush(vararg colorStops: Pair<Float, Color>) = object : ShaderBrush() {
    override fun createShader(size: Size): Shader {
        return RadialGradientShader(
            colors = colorStops.map(Pair<Float, Color>::second),
            center = size.center,
            radius = maxOf(size.height, size.width) / 2f,
            colorStops = colorStops.map(Pair<Float, Color>::first)
        )
    }
}