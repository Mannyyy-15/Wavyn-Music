package iad1tya.echo.music.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import iad1tya.echo.music.ui.screens.Screens
import iad1tya.echo.music.ui.theme.SfProDisplay

/**
 * Liquid-style floating dock bottom navigation for Wavyn Music.
 * Features a snug, deeply-curved scoop pocket with large rounded corner radii
 * brought closely around the active pill with ample bottom dock margin.
 * The active pill is vertically centered within the scoop pocket.
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

    val dockBgColor = if (pureBlack) Color(0xFF000000) else Color(0xF5080A10)
    // Navy blue pill gradient
    val pillColorStart = Color(0xFF1A3A6B)
    val pillColorEnd = Color(0xFF2558A6)
    val activeContentColor = Color.White
    val inactiveIconColor = Color.White.copy(alpha = 0.50f)
    val highlightBorder = Color.White.copy(alpha = 0.09f)

    // Root dock coordinates for pixel-perfect relative positioning
    var dockCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    // Track tab center X and pill width for dynamic scoop sizing
    val tabCenters = remember { mutableStateMapOf<Int, Float>() }
    val tabPillWidths = remember { mutableStateMapOf<Int, Float>() }
    val activeCenterX = tabCenters[selectedIndex] ?: 0f
    val activePillWidth = tabPillWidths[selectedIndex] ?: 0f

    val density = LocalDensity.current
    val navHeightDp = if (slim) 72.dp else 80.dp

    // Elastic springy animation for scoop position
    val animatedScoopCenterX by animateFloatAsState(
        targetValue = activeCenterX,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = Spring.StiffnessLow
        ),
        label = "LiquidScoopCenter"
    )

    // Animate scoop width to match bulky pill
    val animatedPillWidth by animateFloatAsState(
        targetValue = activePillWidth,
        animationSpec = spring(
            dampingRatio = 0.70f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "LiquidScoopWidth"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                dockCoordinates = coordinates
            }
            .drawBehind {
                val w = size.width
                val h = size.height

                if (animatedScoopCenterX > 0f) {
                    // Generous 16dp bottom dock rim so it's comfortably away from the bottom phone edge
                    val bottomMarginPx = 16.dp.toPx()
                    val insetPx = bottomInsetDp.toPx()
                    val contentHeightPx = h - insetPx
                    val scoopDepth = (contentHeightPx - bottomMarginPx).coerceAtLeast(38.dp.toPx())

                    // Large, prominent corner radii for curvy aesthetics
                    val topRadius = 15.dp.toPx()
                    val bottomRadius = 18.dp.toPx()

                    // Snug 3.5dp gap between pill side edge and scoop wall
                    val sideGapPx = 3.5.dp.toPx()
                    val pillHalf = animatedPillWidth / 2f
                    val centerX = animatedScoopCenterX

                    val wallLeft = centerX - pillHalf - sideGapPx
                    val wallRight = centerX + pillHalf + sideGapPx

                    val floorLeft = wallLeft + bottomRadius
                    val floorRight = wallRight - bottomRadius

                    val leftShoulderStart = wallLeft - topRadius
                    val rightShoulderEnd = wallRight + topRadius

                    // Construct snug path with large rounded arcs
                    val liquidPath = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(leftShoulderStart.coerceAtLeast(0f), 0f)

                        // 1. Top-Left Rounded Shoulder (quarter-circle arc into vertical side)
                        cubicTo(
                            leftShoulderStart + topRadius * 0.552f, 0f,
                            wallLeft, topRadius * 0.448f,
                            wallLeft, topRadius
                        )

                        // 2. Left Wall down towards bottom corner
                        lineTo(wallLeft, scoopDepth - bottomRadius)

                        // 3. Bottom-Left Rounded Corner (quarter-circle arc onto floor)
                        cubicTo(
                            wallLeft, scoopDepth - bottomRadius * 0.448f,
                            floorLeft - bottomRadius * 0.552f, scoopDepth,
                            floorLeft, scoopDepth
                        )

                        // 4. Snug flat floor under the pill
                        lineTo(floorRight, scoopDepth)

                        // 5. Bottom-Right Rounded Corner (quarter-circle arc from floor up to wall)
                        cubicTo(
                            floorRight + bottomRadius * 0.552f, scoopDepth,
                            wallRight, scoopDepth - bottomRadius * 0.448f,
                            wallRight, scoopDepth - bottomRadius
                        )

                        // 6. Right Wall up towards top shoulder
                        lineTo(wallRight, topRadius)

                        // 7. Top-Right Rounded Shoulder (quarter-circle arc to top bar)
                        cubicTo(
                            wallRight, topRadius * 0.448f,
                            rightShoulderEnd - topRadius * 0.552f, 0f,
                            rightShoulderEnd.coerceAtMost(w), 0f
                        )

                        lineTo(w, 0f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }

                    // Fill dock shape
                    drawPath(liquidPath, color = dockBgColor, style = Fill)

                    // Draw subtle top contour hairline for depth
                    val contourPath = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(leftShoulderStart.coerceAtLeast(0f), 0f)
                        cubicTo(
                            leftShoulderStart + topRadius * 0.552f, 0f,
                            wallLeft, topRadius * 0.448f,
                            wallLeft, topRadius
                        )
                        lineTo(wallLeft, scoopDepth - bottomRadius)
                        cubicTo(
                            wallLeft, scoopDepth - bottomRadius * 0.448f,
                            floorLeft - bottomRadius * 0.552f, scoopDepth,
                            floorLeft, scoopDepth
                        )
                        lineTo(floorRight, scoopDepth)
                        cubicTo(
                            floorRight + bottomRadius * 0.552f, scoopDepth,
                            wallRight, scoopDepth - bottomRadius * 0.448f,
                            wallRight, scoopDepth - bottomRadius
                        )
                        lineTo(wallRight, topRadius)
                        cubicTo(
                            wallRight, topRadius * 0.448f,
                            rightShoulderEnd - topRadius * 0.552f, 0f,
                            rightShoulderEnd.coerceAtMost(w), 0f
                        )
                        lineTo(w, 0f)
                    }
                    drawPath(contourPath, color = highlightBorder, style = Stroke(width = 1.dp.toPx()))
                } else {
                    drawRect(dockBgColor)
                    drawLine(
                        color = highlightBorder,
                        start = Offset(0f, 0f),
                        end = Offset(w, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(navHeightDp)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index

                    // Active tab gets dynamic larger weight so text and icon fit with plenty of space
                    val tabWeight by animateFloatAsState(
                        targetValue = if (isSelected) 1.50f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = 0.72f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "TabWeight_$index"
                    )

                    Box(
                        modifier = Modifier
                            .weight(tabWeight)
                            .fillMaxHeight()
                            .onGloballyPositioned { coordinates ->
                                val dock = dockCoordinates
                                if (dock != null && dock.isAttached && coordinates.isAttached) {
                                    val posInDock = dock.localPositionOf(coordinates, Offset.Zero)
                                    val centerX = posInDock.x + coordinates.size.width / 2f
                                    tabCenters[index] = centerX
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            // Active Bulky Navy Blue Pill — vertically centered within the scoop pocket
                            Row(
                                modifier = Modifier
                                    .offset(y = (-4.5).dp)
                                    .onGloballyPositioned { coordinates ->
                                        val dock = dockCoordinates
                                        if (dock != null && dock.isAttached && coordinates.isAttached) {
                                            val posInDock = dock.localPositionOf(coordinates, Offset.Zero)
                                            val pillCenterX = posInDock.x + coordinates.size.width / 2f
                                            tabCenters[index] = pillCenterX
                                            tabPillWidths[index] = coordinates.size.width.toFloat()
                                        }
                                    }
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(pillColorStart, pillColorEnd)
                                        )
                                    )
                                    .combinedClickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true, radius = 34.dp),
                                        onClick = { onTabSelected(item) },
                                        onLongClick = onTabLongClick?.let { { it(item) } }
                                    )
                                    .padding(horizontal = 18.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = item.iconIdActive),
                                    contentDescription = stringResource(id = item.titleId),
                                    tint = activeContentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(id = item.titleId),
                                    color = activeContentColor,
                                    fontFamily = SfProDisplay,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        } else {
                            // Inactive icon
                            Box(
                                modifier = Modifier
                                    .offset(y = (-2).dp)
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .combinedClickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true, radius = 24.dp),
                                        onClick = { onTabSelected(item) },
                                        onLongClick = onTabLongClick?.let { { it(item) } }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = item.iconIdInactive),
                                    contentDescription = stringResource(id = item.titleId),
                                    tint = inactiveIconColor,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (bottomInsetDp > 0.dp) {
                Spacer(modifier = Modifier.height(bottomInsetDp))
            }
        }
    }
}
