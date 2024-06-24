package ru.blays.hub.core.ui.elements.text

import android.annotation.SuppressLint
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.core.util.PatternsCompat

@Composable
fun HyperlinkText(
    modifier: Modifier = Modifier,
    fullText: String,
    textStyle: TextStyle = TextStyle.Default,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    linkTextColor: Color = MaterialTheme.colorScheme.primary,
    linkTextFontWeight: FontWeight = FontWeight.SemiBold,
    linkTextDecoration: TextDecoration = TextDecoration.Underline,
    fontSize: TextUnit = TextUnit.Unspecified,
    onLinkClick: (link: String) -> Unit,
    onClick: () -> Unit = {}
) {
    val linkStyle = SpanStyle(
        color = linkTextColor,
        fontSize = fontSize,
        fontWeight = linkTextFontWeight,
        textDecoration = linkTextDecoration
    )
    val normalTextStyle = SpanStyle(
        fontSize = fontSize
    )

    val links = rememberSaveable(fullText) { findLinksInText(fullText) }

    val annotatedString = remember(fullText) {
        buildAnnotatedString {
            append(fullText)

            for(link in links){
                val startIndex = fullText.indexOf(link).coerceAtLeast(0)
                val endIndex = startIndex + link.length
                addStyle(
                    style = linkStyle,
                    start = startIndex,
                    end = endIndex
                )
                addStringAnnotation(
                    tag = "URL",
                    annotation = link,
                    start = startIndex,
                    end = endIndex
                )
            }
            addStyle(
                style = normalTextStyle,
                start = 0,
                end = fullText.length
            )
        }
    }

    ClickableText(
        modifier = modifier,
        text = annotatedString,
        style = textStyle,
        overflow = overflow,
        maxLines = maxLines,
        onClick = {
            annotatedString.getStringAnnotations("URL", it, it)
                .firstOrNull()
                ?.let { stringAnnotation ->
                    onLinkClick(stringAnnotation.item)
                }
                ?: onClick()
        }
    )
}

@SuppressLint("RestrictedApi")
fun findLinksInText(text: String): List<String> {
    val matcher = PatternsCompat.AUTOLINK_WEB_URL.matcher(text)
    val result = mutableListOf<String>()
    while (matcher.find()) {
        result.add(matcher.group())
    }
    return result
}