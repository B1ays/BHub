package ru.blays.hub.core.ui.screens.aboutContent

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.blays.hub.core.domain.components.AboutComponent
import ru.blays.hub.core.domain.utils.openInBrowser
import ru.blays.hub.core.ui.BuildConfig
import ru.blays.hub.core.ui.R
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingTitle
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingToolbar
import ru.blays.hub.core.ui.elements.lazyListItems.groupWithTitle
import ru.blays.hub.core.ui.elements.spacers.HorizontalSpacer
import ru.blays.hub.core.ui.elements.spacers.VerticalSpacer
import ru.blays.hub.core.ui.values.CardShape
import ru.blays.hub.core.ui.values.DefaultPadding

@Composable
fun AboutContent(component: AboutComponent) {
    Scaffold(
        topBar = {
            CollapsingToolbar(
                collapsingTitle = CollapsingTitle.large(stringResource(R.string.toolbar_title_about)),
                collapsedElevation = 0.dp,
            )
        }
    ) { padding ->
        val appGroupTitle = stringResource(R.string.about_group_app)
        val developerGroupTitle = stringResource(R.string.about_group_developer)

        LazyColumn(
            contentPadding = padding,
            modifier = Modifier,
        ) {
            item { Info() }
            groupWithTitle(appGroupTitle) {
                item { AppGroup() }
            }
            groupWithTitle(developerGroupTitle) {
                item { DeveloperGroup() }
            }
        }
    }
}

@Composable
private fun Info() {
    Column(
        modifier = Modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_app_round),
            contentDescription = null,
            modifier = Modifier.size(86.dp),
        )
        VerticalSpacer(10.dp)
        Text(
            text = stringResource(R.string.app_name_full),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        VerticalSpacer(2.dp)
        Text(
            text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.W300
        )
    }
}


@Composable
fun AppGroup() {
    val context = LocalContext.current
    ItemCard(
        title = stringResource(R.string.about_title_tg_group),
        iconPainter = painterResource(R.drawable.ic_telegram),
        shape = CardShape.CardStart,
        modifier = Modifier.padding(DefaultPadding.CardPaddingSmallVertical),
    ) {
        context.openInBrowser(context.getString(R.string.link_tg_group))
    }
    ItemCard(
        title = stringResource(R.string.about_title_source_code),
        iconPainter = painterResource(R.drawable.ic_github),
        shape = CardShape.CardEnd,
        modifier = Modifier.padding(DefaultPadding.CardPaddingSmallVertical),
    ) {
        context.openInBrowser(context.getString(R.string.link_github_repo))
    }
}

@Composable
fun DeveloperGroup() {
    val context = LocalContext.current
    InfoCard(
        text = stringResource(R.string.about_info_developer),
        iconPainter = painterResource(R.drawable.ic_user),
        shape = CardShape.CardStart,
        modifier = Modifier.padding(DefaultPadding.CardPaddingSmallVertical),
    )
    ItemCard(
        title = stringResource(R.string.about_title_profile_tg),
        iconPainter = painterResource(R.drawable.ic_telegram),
        shape = CardShape.CardMid,
        modifier = Modifier.padding(DefaultPadding.CardPaddingSmallVertical),
    ) {
        context.openInBrowser(context.getString(R.string.link_developer_tg))
    }
    ItemCard(
        title = stringResource(R.string.about_title_profile_4pda),
        iconPainter = painterResource(R.drawable.ic_4pda),
        shape = CardShape.CardMid,
        modifier = Modifier.padding(DefaultPadding.CardPaddingSmallVertical),
    ) {
        context.openInBrowser(context.getString(R.string.link_developer_4pda))
    }
    ItemCard(
        title = stringResource(R.string.about_title_profile_github),
        iconPainter = painterResource(R.drawable.ic_github),
        shape = CardShape.CardMid,
        modifier = Modifier.padding(DefaultPadding.CardPaddingSmallVertical),
    ) {
        context.openInBrowser(context.getString(R.string.link_developer_github))
    }
    ItemCard(
        title = stringResource(R.string.about_title_support),
        iconPainter = painterResource(R.drawable.ic_usd_circle),
        shape = CardShape.CardEnd,
        modifier = Modifier.padding(DefaultPadding.CardPaddingSmallVertical),
    ) {
        context.openInBrowser(context.getString(R.string.link_developer_support))
    }
}

@Composable
fun ItemCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    iconPainter: Painter,
    iconSize: Dp = 36.dp,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.elevatedCardColors(
        contentColor = MaterialTheme.colorScheme.primary.copy(0.8F)
    ),
    elevation: Dp = 10.dp,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier.clip(shape),
        onClick = onClick,
        shape = shape,
        colors = colors,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
            )
            HorizontalSpacer(12.dp)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                if(subtitle != null) {
                    VerticalSpacer(4.dp)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    text: String,
    iconPainter: Painter,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.elevatedCardColors(
        contentColor = MaterialTheme.colorScheme.primary.copy(0.8F)
    ),
    elevation: Dp = 10.dp
) {
    ElevatedCard(
        modifier = modifier.clip(shape),
        shape = shape,
        colors = colors,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            HorizontalSpacer(10.dp)
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun CreditCard(
    modifier: Modifier = Modifier,
    credit: String,
    creditFor: String,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.elevatedCardColors(
        contentColor = MaterialTheme.colorScheme.primary.copy(0.8F)
    ),
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier.clip(shape),
        onClick = onClick,
        shape = shape,
        colors = colors
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = credit,
                style = MaterialTheme.typography.titleMedium
            )
            VerticalSpacer(4.dp)
            Text(
                text = creditFor,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}