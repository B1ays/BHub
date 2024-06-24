package ru.blays.hub.core.ui.values

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object CardShape {
    val CardStart = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
    val CardMid = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
    val CardEnd = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
    val CardStandalone = RoundedCornerShape(10.dp)
    val CardStandaloneLarge = RoundedCornerShape(16.dp)
}

object DefaultPadding {
    val CardVerticalPadding = 3.dp
    val CardHorizontalPadding = 12.dp
    val CardDefaultPadding = PaddingValues(horizontal = 12.dp, vertical = 3.dp)
    val CardDefaultPaddingLarge = PaddingValues(horizontal = 16.dp, vertical = 7.dp)
    val CardDefaultPaddingSmall = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
    val CardPaddingSmallVertical = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
}