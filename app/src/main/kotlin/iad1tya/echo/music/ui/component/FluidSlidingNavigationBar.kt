package iad1tya.echo.music.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import iad1tya.echo.music.ui.screens.Screens

/**
 * Docked Animated Menu Bar bottom navigation for Wavyn Music.
 * Active item expands into a Brand Orange pill containing both icon and text label.
 * Inactive items display clean, muted icons.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FluidSlidingNavigationBar(
    modifier: Modifier = Modifier,
    items: List<Screens>,
    currentRoute: String,
    pureBlack: Boolean,
    slim: Boolean = false,
    bottomInsetDp: Dp = 0.dp,
    onTabSelected: (Screens) -> Unit,
    onTabLongClick: ((Screens) -> Unit)? = null,
) {
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    val navBarBgColor = if (pureBlack) {
        Color(0xFA000000)
    } else {
        Color(0xF60A0B0E)
    }

    val topBorderColor = if (pureBlack) {
        Color.White.copy(alpha = 0.10f)
    } else {
        Color.White.copy(alpha = 0.14f)
    }

    val brandOrange = Color(0xFFE85002)
    val flameOrange = Color(0xFFF16001)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(navBarBgColor)
            .drawBehind {
                drawLine(
                    color = topBorderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        // Main Tab Content Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (slim) 54.dp else 62.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index

                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "TabIconScale"
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .then(
                            if (isSelected) {
                                Modifier
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(brandOrange, flameOrange)
                                        )
                                    )
                                    .border(
                                        width = 0.75.dp,
                                        color = Color.White.copy(alpha = 0.35f),
                                        shape = CircleShape
                                    )
                            } else {
                                Modifier
                            }
                        )
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, radius = 28.dp),
                            onClick = { onTabSelected(item) },
                            onLongClick = onTabLongClick?.let { { it(item) } }
                        )
                        .padding(
                            horizontal = if (isSelected) 15.dp else 10.dp,
                            vertical = if (slim) 6.dp else 8.dp
                        )
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = if (isSelected) item.iconIdActive else item.iconIdInactive),
                            contentDescription = stringResource(id = item.titleId),
                            tint = if (isSelected) Color.White else Color(0xFFA7A7A7),
                            modifier = Modifier
                                .size(if (slim) 20.dp else 22.dp)
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                        )

                        if (isSelected) {
                            Text(
                                text = stringResource(id = item.titleId),
                                color = Color.White,
                                fontSize = if (slim) 12.sp else 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }
                }
            }
        }

        // Gesture navigation bar spacer
        if (bottomInsetDp > 0.dp) {
            Spacer(modifier = Modifier.height(bottomInsetDp))
        }
    }
}
