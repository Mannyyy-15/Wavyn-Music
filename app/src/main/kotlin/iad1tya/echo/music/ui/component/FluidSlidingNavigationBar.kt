package iad1tya.echo.music.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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

    val dockBgColor = if (pureBlack) {
        Color(0xF50A0B0E)
    } else {
        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.94f)
    }

    val dockBorderBrush = Brush.verticalGradient(
        listOf(
            if (pureBlack) Color.White.copy(alpha = 0.16f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            if (pureBlack) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.10f)
        )
    )

    BoxWithConstraints(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color.Black.copy(alpha = if (pureBlack) 0.6f else 0.35f),
                spotColor = Color.Black.copy(alpha = if (pureBlack) 0.7f else 0.45f)
            )
            .clip(RoundedCornerShape(26.dp))
            .border(
                width = 1.dp,
                brush = dockBorderBrush,
                shape = RoundedCornerShape(26.dp)
            )
            .background(dockBgColor)
            .fillMaxWidth()
    ) {
        val tabWidth = maxWidth / items.size
        val pillWidth = if (slim) 52.dp else 56.dp
        val pillHeight = if (slim) 32.dp else 34.dp

        val indicatorOffset by animateDpAsState(
            targetValue = (tabWidth * selectedIndex) + ((tabWidth - pillWidth) / 2),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "DockPillOffset"
        )

        // Floating Active Capsule Indicator styled with Theme primary container
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset, y = if (slim) 10.dp else 12.dp)
                .width(pillWidth)
                .height(pillHeight)
                .clip(CircleShape)
                .border(
                    width = 0.8.dp,
                    color = if (pureBlack) Color.White.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                    shape = CircleShape
                )
                .background(
                    if (pureBlack) {
                        Color.White.copy(alpha = 0.16f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
                    }
                )
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index

                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.10f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "TabIconScale"
                )

                val activeColor = if (pureBlack) Color.White else MaterialTheme.colorScheme.primary
                val inactiveColor = if (pureBlack) Color.White.copy(alpha = 0.55f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)

                val tabColor by animateColorAsState(
                    targetValue = if (isSelected) activeColor else inactiveColor,
                    animationSpec = tween(durationMillis = 200),
                    label = "TabColor"
                )

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
                        Spacer(modifier = Modifier.height(17.dp))
                    }

                    Icon(
                        painter = painterResource(id = if (isSelected) item.iconIdActive else item.iconIdInactive),
                        contentDescription = stringResource(id = item.titleId),
                        tint = tabColor,
                        modifier = Modifier
                            .size(23.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )

                    if (!slim) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(id = item.titleId),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = tabColor,
                            letterSpacing = 0.2.sp
                        )
                    }
                }
            }
        }
    }
}
