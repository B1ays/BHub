package ru.blays.hub.core.ui.elements.imageSlider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.PersistentList
import ru.blays.hub.core.ui.elements.indicators.JumpDotIndicator
import kotlin.math.absoluteValue

@Suppress("NonSkippableComposable")
@Composable
fun ImageSlider(
    modifier: Modifier = Modifier,
    images: PersistentList<*>
) {
    val pagerState = rememberPagerState {
        images.size
    }
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)
    val shape = MaterialTheme.shapes.large
    SlidingCarousel(
        modifier = modifier,
        pagerState = pagerState,
        itemContent = { index ->
            SubcomposeAsyncImage(
                model = images[index],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1/1.5F)
                            .shimmer(shimmer)
                            .background(MaterialTheme.colorScheme.onSurface.copy(0.3F))
                    )
                },
                modifier = Modifier
                    .graphicsLayer {
                        this.shape = shape
                        this.clip = true

                        val pageOffset = pagerState.getOffsetDistanceInPages(index).absoluteValue

                        lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        ).also { scale ->
                            this.scaleX = scale
                            this.scaleY = scale
                        }
                        this.alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
            )
        }
    )
}

@Composable
private fun SlidingCarousel(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    itemContent: @Composable PagerScope.(index: Int) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(maxWidth * 0.6F),
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(
                horizontal = maxWidth * 0.2F,
                vertical = 6.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            pageContent = itemContent
        )

        // you can remove the surface in case you don't want
        // the transparent background
        Surface(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .align(Alignment.BottomCenter),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.5f)
        ) {
            JumpDotIndicator(
                pagerState = pagerState,
                modifier = Modifier
            )
        }
    }
}