package iad1tya.echo.music.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import iad1tya.echo.music.LocalPlayerAwareWindowInsets
import iad1tya.echo.music.R
import java.net.URLEncoder

data class SearchCategory(
    val title: String,
    val searchQuery: String,
    val gradientColors: List<Color>,
    val coverImageUrl: String,
    val params: String? = null,
    val browseId: String = "FEmusic_moods_and_genres_category"
)

data class FeaturedTag(
    val tag: String,
    val query: String,
    val imageUrl: String,
    val gradientColors: List<Color>
)

private val FeaturedTags = listOf(
    FeaturedTag(
        tag = "#TechnoBeats",
        query = "Techno Beats Hits",
        imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/e8/43/5f/e8435ffa-b6b9-b171-40ab-4ff3959ab661/886443919266.jpg/600x600bb.jpg",
        gradientColors = listOf(Color(0xFF0F172A), Color(0xFF0284C7))
    ),
    FeaturedTag(
        tag = "#RockAnthems",
        query = "Rock Anthems Classics",
        imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/4d/08/2a/4d082a9e-7898-1aa1-a02f-339810058d9e/14DMGIM05632.rgb.jpg/600x600bb.jpg",
        gradientColors = listOf(Color(0xFF1E1B4B), Color(0xFF991B1B))
    ),
    FeaturedTag(
        tag = "#PartyHits",
        query = "Party Dance Hits",
        imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/81/f4/ce/81f4ce8e-4ee2-64f4-aa3e-4001daa667f8/1200220041756.jpg/600x600bb.jpg",
        gradientColors = listOf(Color(0xFF831843), Color(0xFFDB2777))
    ),
    FeaturedTag(
        tag = "#ChillVibes",
        query = "Chill Lo-Fi Beats",
        imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/8f/ff/17/8fff17fc-c83d-1c8d-89d7-d9de8a587e1b/5060781128268.png/600x600bb.jpg",
        gradientColors = listOf(Color(0xFF064E3B), Color(0xFF059669))
    )
)

private val MoodAndGenres = listOf(
    SearchCategory(
        title = "Rock",
        searchQuery = "Rock Anthems Classics",
        gradientColors = listOf(Color(0xFFEF4444), Color(0xFF991B1B)),
        coverImageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/4d/08/2a/4d082a9e-7898-1aa1-a02f-339810058d9e/14DMGIM05632.rgb.jpg/600x600bb.jpg",
        params = "ggMPOg1uXzJKTm5jUEZ5Uzlu"
    ),
    SearchCategory(
        title = "Hip Hop",
        searchQuery = "Hip Hop Hits",
        gradientColors = listOf(Color(0xFF0284C7), Color(0xFF0369A1)),
        coverImageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/7d/4f/94/7d4f9468-56e1-3a2d-7186-c8088170ef58/196871341899.jpg/600x600bb.jpg",
        params = "ggMPOg1uX0M2dmRieXNxTW1s"
    ),
    SearchCategory(
        title = "Pop",
        searchQuery = "Pop Hits",
        gradientColors = listOf(Color(0xFF10B981), Color(0xFF047857)),
        coverImageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/11/a6/80/11a680e6-2e48-08fa-5e87-3f18e838d31f/23UM1IM11868.rgb.jpg/600x600bb.jpg",
        params = "ggMPOg1uX1lLQkxHbHhWQUUy"
    ),
    SearchCategory(
        title = "Dance & EDM",
        searchQuery = "Electronic Dance Music",
        gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
        coverImageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/e8/43/5f/e8435ffa-b6b9-b171-40ab-4ff3959ab661/886443919266.jpg/600x600bb.jpg",
        params = "ggMPOg1uX1NPTld3SDN3WGs4"
    ),
    SearchCategory(
        title = "R&B & Soul",
        searchQuery = "R&B Soul Hits",
        gradientColors = listOf(Color(0xFFA855F7), Color(0xFF7E22CE)),
        coverImageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/2b/b9/fe/2bb9fef5-d7f3-8345-25a9-db0e79fde4e4/20UMGIM11048.rgb.jpg/600x600bb.jpg",
        params = "ggMPOg1uX2JxQ2hxc2J5UFhR"
    ),
    SearchCategory(
        title = "Chill",
        searchQuery = "Chill Relaxing Music",
        gradientColors = listOf(Color(0xFF0EA5E9), Color(0xFF0369A1)),
        coverImageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/8f/ff/17/8fff17fc-c83d-1c8d-89d7-d9de8a587e1b/5060781128268.png/600x600bb.jpg",
        params = "ggMPOg1uX1JOQWZFeDByc2Jm"
    ),
    SearchCategory(
        title = "Party",
        searchQuery = "Party Club Hits",
        gradientColors = listOf(Color(0xFFF43F5E), Color(0xFFBE123C)),
        coverImageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/81/f4/ce/81f4ce8e-4ee2-64f4-aa3e-4001daa667f8/1200220041756.jpg/600x600bb.jpg",
        params = "ggMPOg1uX0pmQ0s2V0JRclZs"
    ),
    SearchCategory(
        title = "Workout",
        searchQuery = "Workout Motivation Hits",
        gradientColors = listOf(Color(0xFFF97316), Color(0xFFC2410C)),
        coverImageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music118/v4/dd/5c/e6/dd5ce621-f7d2-f767-7a08-e7a7eaa7870b/00602537526994.rgb.jpg/600x600bb.jpg",
        params = "ggMPOg1uX09LWkhnTjRGRUJh"
    ),
    SearchCategory(
        title = "Romance",
        searchQuery = "Romantic Love Songs",
        gradientColors = listOf(Color(0xFFFB7185), Color(0xFFE11D48)),
        coverImageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/15/e6/e8/15e6e8a4-4190-6a8b-86c3-ab4a51b88288/190295851286.jpg/600x600bb.jpg",
        params = "ggMPOg1uX0FzQ2FhZWtUY211"
    ),
    SearchCategory(
        title = "Indie",
        searchQuery = "Indie Alternative Rock",
        gradientColors = listOf(Color(0xFF6366F1), Color(0xFF4338CA)),
        coverImageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/69/9c/b5/699cb5d6-115c-ff73-9d26-e57ea4350d72/887828031795.png/600x600bb.jpg",
        params = "ggMPOg1uX21NWWpBbU01SDgy"
    ),
    SearchCategory(
        title = "Bollywood",
        searchQuery = "Bollywood Hits Hindi",
        gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFB45309)),
        coverImageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/fc/50/b3/fc50b3ca-c94b-58eb-a10c-80287d82eba3/8903431981196_cover.jpg/600x600bb.jpg",
        params = "ggMPOg1uX2ZvbzNJMzJwRkFT"
    ),
    SearchCategory(
        title = "Punjabi",
        searchQuery = "Punjabi Hits",
        gradientColors = listOf(Color(0xFFEC4899), Color(0xFF9D174D)),
        coverImageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/97/69/58/976958ae-725e-bd41-6755-f0921c697840/810063889609_cover.jpg/600x600bb.jpg",
        params = "ggMPOg1uX1ZKNkRodjF2YWxv"
    ),
    SearchCategory(
        title = "Metal",
        searchQuery = "Heavy Metal Hits",
        gradientColors = listOf(Color(0xFF475569), Color(0xFF1E293B)),
        coverImageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Features125/v4/0f/7a/74/0f7a7472-92fa-e77d-384a-1e4304705e83/dj.jbiruenb.png/600x600bb.jpg",
        params = "ggMPOg1uXzdlSXhKZ0hMV1Z4"
    ),
    SearchCategory(
        title = "Jazz",
        searchQuery = "Jazz Classics",
        gradientColors = listOf(Color(0xFFD97706), Color(0xFF92400E)),
        coverImageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music/7f/9f/d6/mzi.vtnaewef.jpg/600x600bb.jpg",
        params = "ggMPOg1uX3lPcDFRaE9wM1BS"
    ),
    SearchCategory(
        title = "Feel Good",
        searchQuery = "Feel Good Happy Hits",
        gradientColors = listOf(Color(0xFF14B8A6), Color(0xFF0F766E)),
        coverImageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/e3/47/a0/e347a0cc-87ce-5d05-d560-176c7d48f66e/075679904119.jpg/600x600bb.jpg",
        params = "ggMPOg1uXzZQbDB5eThLRTQ3"
    ),
    SearchCategory(
        title = "Sad",
        searchQuery = "Sad Melancholic Songs",
        gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
        coverImageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/02/ed/8c/02ed8cab-c089-2fdd-7ce6-ab334a9a4e19/21UMGIM26093.rgb.jpg/600x600bb.jpg",
        params = "ggMPOg1uX0JLQ0gySWZKZVY1"
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
            top = 14.dp,
            bottom = playerAwareInsets.calculateBottomPadding() + 32.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Modern Pill Search Bar
        item(span = { GridItemSpan(2) }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = Color.Black.copy(alpha = 0.40f)
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .border(
                        width = 0.9.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .background(Color(0xFF141720))
                    .clickable(onClick = onSearchBarClick)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Search for a song, artist or playlist",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Section 1 Header: Explore Your Wavyn
        item(span = { GridItemSpan(2) }) {
            Text(
                text = "Explore Your Wavyn",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 18.dp, bottom = 2.dp)
            )
        }

        // Section 1 Carousel: Explore Cards
        item(span = { GridItemSpan(2) }) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(FeaturedTags) { featured ->
                    Box(
                        modifier = Modifier
                            .width(135.dp)
                            .height(180.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = Color.Black.copy(alpha = 0.40f)
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 0.8.dp,
                                color = Color.White.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(Color(0xFF151922))
                            .clickable {
                                navController.navigate("search/${URLEncoder.encode(featured.query, "UTF-8")}?filter=playlists")
                            }
                    ) {
                        // Background Cover Image
                        AsyncImage(
                            model = featured.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Bottom Gradient Scrim
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.35f),
                                            Color.Black.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        )

                        // Tag Label at Bottom
                        Text(
                            text = featured.tag,
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        )
                    }
                }
            }
        }

        // Section 2 Header: Mood & Genres
        item(span = { GridItemSpan(2) }) {
            Text(
                text = "Mood & Genres",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 18.dp, bottom = 4.dp)
            )
        }

        // Section 2: 2-Column Grid of Mood & Genre Cards with Tilted Album Cover
        items(MoodAndGenres) { category ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(98.dp)
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color.Black.copy(alpha = 0.35f)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 0.8.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.22f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(
                        brush = Brush.linearGradient(
                            colors = category.gradientColors
                        )
                    )
                    .clickable {
                        val encodedTitle = URLEncoder.encode(category.title, "UTF-8")
                        if (!category.params.isNullOrEmpty()) {
                            navController.navigate("youtube_browse/${category.browseId}?params=${category.params}&title=$encodedTitle")
                        } else {
                            navController.navigate("search/${URLEncoder.encode(category.searchQuery, "UTF-8")}?filter=playlists")
                        }
                    }
            ) {
                // Tilted Album Art positioned at the bottom-right corner
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 10.dp, y = 12.dp)
                        .rotate(18f)
                        .size(68.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(10.dp),
                            ambientColor = Color.Black.copy(alpha = 0.60f),
                            spotColor = Color.Black.copy(alpha = 0.80f)
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E222D))
                ) {
                    AsyncImage(
                        model = category.coverImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Category Title in top-left
                Text(
                    text = category.title,
                    color = Color.White,
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 14.dp, top = 14.dp)
                )
            }
        }
    }
}
