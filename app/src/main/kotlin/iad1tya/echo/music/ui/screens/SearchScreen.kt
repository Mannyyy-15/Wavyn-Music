package iad1tya.echo.music.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import iad1tya.echo.music.LocalPlayerAwareWindowInsets
import iad1tya.echo.music.R
import java.net.URLEncoder

data class SearchCategory(
    val title: String,
    val bgText: String,
    val gradientColors: List<Color>,
    val searchQuery: String = title
)

private val BrowseCategories = listOf(
    SearchCategory(
        title = "Pop",
        bgText = "POP",
        gradientColors = listOf(Color(0xFFEC4899), Color(0xFFBE185D))
    ),
    SearchCategory(
        title = "Focus",
        bgText = "FOCUS",
        gradientColors = listOf(Color(0xFF475569), Color(0xFF334155))
    ),
    SearchCategory(
        title = "Workout",
        bgText = "WORKOUT",
        gradientColors = listOf(Color(0xFFF97316), Color(0xFFC2410C))
    ),
    SearchCategory(
        title = "Chill",
        bgText = "CHILL",
        gradientColors = listOf(Color(0xFF2563EB), Color(0xFF1E3A8A))
    ),
    SearchCategory(
        title = "Rock",
        bgText = "ROCK",
        gradientColors = listOf(Color(0xFF991B1B), Color(0xFF1F2937))
    ),
    SearchCategory(
        title = "Jazz",
        bgText = "JAZZ",
        gradientColors = listOf(Color(0xFFD97706), Color(0xFF78350F))
    ),
    SearchCategory(
        title = "Electronic",
        bgText = "ELECTRONIC",
        gradientColors = listOf(Color(0xFF0D9488), Color(0xFF064E3B))
    ),
    SearchCategory(
        title = "Ambient",
        bgText = "AMBIENT",
        gradientColors = listOf(Color(0xFF7C3AED), Color(0xFF4C1D95))
    ),
    SearchCategory(
        title = "Hip Hop",
        bgText = "HIP HOP",
        gradientColors = listOf(Color(0xFFB45309), Color(0xFF78350F))
    ),
    SearchCategory(
        title = "Party",
        bgText = "PARTY",
        gradientColors = listOf(Color(0xFFA855F7), Color(0xFF7E22CE))
    ),
    SearchCategory(
        title = "Romance",
        bgText = "ROMANCE",
        gradientColors = listOf(Color(0xFFE11D48), Color(0xFF9F1239))
    ),
    SearchCategory(
        title = "Dance",
        bgText = "DANCE",
        gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF0284C7))
    ),
    SearchCategory(
        title = "Indie",
        bgText = "INDIE",
        gradientColors = listOf(Color(0xFF65A30D), Color(0xFF365314))
    ),
    SearchCategory(
        title = "Sleep",
        bgText = "SLEEP",
        gradientColors = listOf(Color(0xFF312E81), Color(0xFF1E1B4B))
    ),
    SearchCategory(
        title = "Classical",
        bgText = "CLASSIC",
        gradientColors = listOf(Color(0xFF713F12), Color(0xFF451A03))
    ),
    SearchCategory(
        title = "Gaming",
        bgText = "GAMING",
        gradientColors = listOf(Color(0xFF6366F1), Color(0xFF3730A3))
    ),
    SearchCategory(
        title = "Metal",
        bgText = "METAL",
        gradientColors = listOf(Color(0xFF374151), Color(0xFF111827))
    ),
    SearchCategory(
        title = "R&B",
        bgText = "R&B",
        gradientColors = listOf(Color(0xFF831843), Color(0xFF500724))
    )
)

@Composable
fun SearchScreen(
    navController: NavController,
    onSearchBarClick: () -> Unit
) {
    // Navigate back to home when pressing back on this screen
    BackHandler {
        navController.navigate(Screens.Home.route) {
            popUpTo(Screens.Home.route) { inclusive = true }
        }
    }

    val playerAwareInsets = LocalPlayerAwareWindowInsets.current.asPaddingValues()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = playerAwareInsets.calculateBottomPadding() + 24.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Large "Search" Header Title
        item(span = { GridItemSpan(2) }) {
            Text(
                text = "Search",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Spotify-style Search Input Bar Pill
        item(span = { GridItemSpan(2) }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = Color.Black.copy(alpha = 0.40f)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 0.8.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Color(0xFF191C24))
                    .clickable(onClick = onSearchBarClick)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.70f),
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = "What do you want to listen to?",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // "Browse all" Section Header
        item(span = { GridItemSpan(2) }) {
            Text(
                text = "Browse all",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }

        // 2-Column Category Cards Grid
        items(BrowseCategories) { category ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(102.dp)
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = Color.Black.copy(alpha = 0.40f),
                        spotColor = Color.Black.copy(alpha = 0.50f)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 0.8.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.22f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        brush = Brush.linearGradient(
                            colors = category.gradientColors
                        )
                    )
                    .clickable {
                        navController.navigate("search/${URLEncoder.encode(category.searchQuery, "UTF-8")}")
                    }
            ) {
                // Large stylized subtle watermark in background
                Text(
                    text = category.bgText,
                    color = Color.White.copy(alpha = 0.20f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 6.dp, y = 4.dp)
                        .rotate(-8f)
                )

                // Category Title in top-left
                Text(
                    text = category.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = 12.dp)
                )
            }
        }
    }
}
