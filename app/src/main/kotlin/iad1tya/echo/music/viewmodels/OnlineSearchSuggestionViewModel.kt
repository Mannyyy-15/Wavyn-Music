package iad1tya.echo.music.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echo.innertube.YouTube
import com.echo.innertube.models.YTItem
import com.echo.innertube.models.filterExplicit
import iad1tya.echo.music.constants.HideExplicitKey
import iad1tya.echo.music.db.MusicDatabase
import iad1tya.echo.music.db.entities.Song
import iad1tya.echo.music.utils.dataStore
import iad1tya.echo.music.utils.get
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OnlineSearchSuggestionViewModel
@Inject
constructor(
    @ApplicationContext val context: Context,
    database: MusicDatabase,
) : ViewModel() {
    val query = MutableStateFlow("")
    private val _viewState = MutableStateFlow(SearchSuggestionViewState())
    val viewState = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            query
                .flatMapLatest { q ->
                    if (q.isEmpty()) {
                        database.events().map { events ->
                            val recentSongs = events
                                .map { it.song }
                                .distinctBy { it.id }
                                .take(20)
                            SearchSuggestionViewState(
                                recentSongs = recentSongs,
                            )
                        }
                    } else {
                        val hideExplicit = context.dataStore.get(HideExplicitKey, false)
                        val suggestionResult = withContext(Dispatchers.IO) {
                            YouTube.searchSuggestions(q).getOrNull()
                        }
                        val textSuggestions = suggestionResult?.queries?.take(5).orEmpty()

                        val songSearchResult = withContext(Dispatchers.IO) {
                            YouTube.search(q, YouTube.SearchFilter.FILTER_SONG).getOrNull()
                        }
                        val songItems = (songSearchResult?.items ?: suggestionResult?.recommendedItems)
                            .orEmpty()
                            .distinctBy { it.id }
                            .filterExplicit(hideExplicit)
                            .take(10)

                        flowOf(
                            SearchSuggestionViewState(
                                suggestions = textSuggestions,
                                items = songItems,
                            )
                        )
                    }
                }
                .flowOn(Dispatchers.IO)
                .collect {
                    _viewState.value = it
                }
        }
    }
}

data class SearchSuggestionViewState(
    val recentSongs: List<Song> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val items: List<YTItem> = emptyList(),
)
