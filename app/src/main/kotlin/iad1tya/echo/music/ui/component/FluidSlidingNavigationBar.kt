package iad1tya.echo.music.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import iad1tya.echo.music.ui.screens.Screens

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FluidSlidingNavigationBar(
    modifier: Modifier = Modifier,
    items: List<Screens>,
    currentRoute: String,
    pureBlack: Boolean,
    slim: Boolean = false,
    onTabSelected: (Screens) -> Unit,
    onTabLongClick: ((Screens) -> Unit)? = null
) {
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    val barColor = if (pureBlack) Color(0xFF0D0D0D).copy(alpha = 0.80f) else MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.80f)

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .fillMaxWidth()
            .background(barColor)
    ) {
        val tabWidth = maxWidth / items.size
        val pillWidth = 48.dp
        val pillHeight = if (slim) 28.dp else 32.dp

        val indicatorOffset by animateDpAsState(
            targetValue = (tabWidth * selectedIndex) + ((tabWidth - pillWidth) / 2),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "PillSlider"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset, y = if (slim) 10.dp else 14.dp)
                .width(pillWidth)
                .height(pillHeight)
                .background(
                    color = if (pureBlack) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape
                )
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(item) },
                            onLongClick = onTabLongClick?.let { { it(item) } }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = if (slim) Arrangement.Center else Arrangement.Top
                ) {
                    if (!slim) {
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    Icon(
                        painter = painterResource(id = if (isSelected) item.iconIdActive else item.iconIdInactive),
                        contentDescription = stringResource(id = item.titleId),
                        tint = if (isSelected) {
                            if (pureBlack) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            if (pureBlack) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp)
                    )

                    if (!slim) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(id = item.titleId),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isSelected) {
                                if (pureBlack) Color.White else MaterialTheme.colorScheme.onSurface
                            } else {
                                if (pureBlack) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}
