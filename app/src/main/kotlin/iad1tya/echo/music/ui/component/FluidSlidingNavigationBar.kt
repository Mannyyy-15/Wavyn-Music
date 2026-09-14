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

    val dockGradient = if (pureBlack) {
        listOf(Color(0xF5101116), Color(0xF508080B))
    } else {
        listOf(Color(0xEA161924), Color(0xEA0E1018))
    }

    val dockBorderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (pureBlack) 0.20f else 0.25f),
            Color.White.copy(alpha = if (pureBlack) 0.05f else 0.08f)
        )
    )

    BoxWithConstraints(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(RoundedCornerShape(32.dp))
            .border(
                width = 1.dp,
                brush = dockBorderBrush,
                shape = RoundedCornerShape(32.dp)
            )
            .background(Brush.verticalGradient(dockGradient))
            .fillMaxWidth()
    ) {
        val tabWidth = maxWidth / items.size
        val pillWidth = if (slim) 52.dp else 56.dp
        val pillHeight = if (slim) 32.dp else 36.dp

        val indicatorOffset by animateDpAsState(
            targetValue = (tabWidth * selectedIndex) + ((tabWidth - pillWidth) / 2),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "DockPillOffset"
        )

        // Floating Active Capsule Indicator with soft glow and subtle border
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset, y = if (slim) 12.dp else 14.dp)
                .width(pillWidth)
                .height(pillHeight)
                .clip(CircleShape)
                .border(
                    width = 0.8.dp,
                    color = Color.White.copy(alpha = 0.18f),
                    shape = CircleShape
                )
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (pureBlack) 0.20f else 0.22f),
                            Color.White.copy(alpha = if (pureBlack) 0.08f else 0.10f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index

                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.12f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "TabIconScale"
                )

                val contentAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1.0f else 0.55f,
                    animationSpec = tween(durationMillis = 200),
                    label = "TabAlpha"
                )

                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.60f),
                    animationSpec = tween(durationMillis = 200),
                    label = "TabIconTint"
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
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    Icon(
                        painter = painterResource(id = if (isSelected) item.iconIdActive else item.iconIdInactive),
                        contentDescription = stringResource(id = item.titleId),
                        tint = iconTint,
                        modifier = Modifier
                            .size(23.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )

                    if (!slim) {
                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = stringResource(id = item.titleId),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White.copy(alpha = contentAlpha),
                            letterSpacing = 0.2.sp
                        )
                    }
                }
            }
        }
    }
}
