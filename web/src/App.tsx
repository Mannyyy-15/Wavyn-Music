import React, { useState, useEffect, useCallback } from 'react';
import { LeftSidebar } from './components/LeftSidebar';
import { RightPanel } from './components/RightPanel';
import { NowPlayingModal } from './components/NowPlayingModal';
import { AnimatedMeshBackground } from './components/AnimatedMeshBackground';
import { BottomNav } from './components/BottomNav';
import { HomeView } from './views/HomeView';
import { DiscoverView } from './views/DiscoverView';
import { RadioView } from './views/RadioView';
import { PodcastView } from './views/PodcastView';
import { AlbumsView } from './views/AlbumsView';
import { SongsView } from './views/SongsView';
import { ArtistsView } from './views/ArtistsView';
import { SearchView } from './views/SearchView';
import { PlaylistDetailView } from './views/PlaylistDetailView';
import { ArtistDetailView } from './views/ArtistDetailView';
import { AlbumDetailView } from './views/AlbumDetailView';
import { AudioEngine } from './services/audioEngine';
import { StorageService } from './services/storage';
import { LiveMusicService } from './services/LiveMusicService';
import { ALL_SONGS, ALL_ALBUMS, FEATURED_PLAYLISTS, TOP_ARTISTS } from './services/musicData';
import type { Song, Album, Artist, Playlist, TabType, RepeatMode, VisualizerMode, LyricLine } from './types';

export const App: React.FC = () => {
  const [currentTab, setCurrentTab] = useState<TabType>('home');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [likedSongs, setLikedSongs] = useState<Song[]>(StorageService.getLikedSongs());
  const [customPlaylists, setCustomPlaylists] = useState<Playlist[]>(StorageService.getCustomPlaylists());

  // Dynamic Live Songs from API
  const [liveTrendingSongs, setLiveTrendingSongs] = useState<Song[]>(ALL_SONGS);

  // Navigation Selection State
  const [selectedPlaylist, setSelectedPlaylist] = useState<Playlist | null>(null);
  const [selectedArtist, setSelectedArtist] = useState<Artist | null>(null);
  const [selectedAlbum, setSelectedAlbum] = useState<Album | null>(null);

  // Player State
  const [currentSong, setCurrentSong] = useState<Song | null>(ALL_SONGS[0]);
  const [queue, setQueue] = useState<Song[]>(ALL_SONGS);
  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const [currentTime, setCurrentTime] = useState<number>(0);
  const [duration, setDuration] = useState<number>(ALL_SONGS[0].duration);
  const [volume, setVolume] = useState<number>(0.9);
  const [repeatMode, setRepeatMode] = useState<RepeatMode>('off');
  const [isShuffle, setIsShuffle] = useState<boolean>(false);
  const [visualizerMode, setVisualizerMode] = useState<VisualizerMode>('bars');
  const [lyrics, setLyrics] = useState<LyricLine[]>([]);

  // Modals & Toast
  const [isFullPlayerOpen, setIsFullPlayerOpen] = useState<boolean>(false);
  const [isCreatePlaylistOpen, setIsCreatePlaylistOpen] = useState<boolean>(false);
  const [newPlaylistTitle, setNewPlaylistTitle] = useState<string>('');
  const [toastText, setToastText] = useState<string | null>(null);

  const engine = AudioEngine.getInstance();
  const liveService = LiveMusicService.getInstance();

  const showToast = (text: string) => {
    setToastText(text);
    setTimeout(() => setToastText(null), 3000);
  };

  // Load real trending tracks from API on mount
  useEffect(() => {
    liveService.getTopTrendingTracks().then((tracks) => {
      if (tracks && tracks.length > 0) {
        setLiveTrendingSongs(tracks);
        setQueue(tracks);
        if (!isPlaying && tracks[0]) {
          setCurrentSong(tracks[0]);
          setDuration(tracks[0].duration);
        }
      }
    });
  }, []);

  useEffect(() => {
    const unsubscribe = engine.subscribe(() => {
      setIsPlaying(engine.isPlaying);
      setCurrentTime(engine.currentTime);
      setDuration(engine.duration);
    });
    return () => unsubscribe();
  }, [engine]);

  // Media Session API Support (Lock-screen and system media controls)
  useEffect(() => {
    if ('mediaSession' in navigator && currentSong) {
      navigator.mediaSession.metadata = new MediaMetadata({
        title: currentSong.title,
        artist: currentSong.artist,
        album: currentSong.album || 'Wavyn Music',
        artwork: [{ src: currentSong.thumbnailUrl, sizes: '512x512', type: 'image/jpeg' }]
      });

      navigator.mediaSession.setActionHandler('play', () => engine.play());
      navigator.mediaSession.setActionHandler('pause', () => engine.pause());
      navigator.mediaSession.setActionHandler('previoustrack', () => handlePrev());
      navigator.mediaSession.setActionHandler('nexttrack', () => handleNext());
    }
  }, [currentSong]);

  // Play Song Handler with real audio streaming & lyrics
  const handlePlaySong = async (song: Song, newQueue?: Song[]) => {
    if (newQueue) {
      setQueue(newQueue);
      const index = newQueue.findIndex((s) => s.id === song.id);
      setCurrentIndex(index >= 0 ? index : 0);
    } else if (!queue.some((s) => s.id === song.id)) {
      setQueue((prev) => [...prev, song]);
      setCurrentIndex(queue.length);
    }

    setCurrentSong(song);
    
    // Parse immediate or fetch lyrics from LRCLIB
    if (song.lyrics) {
      setLyrics(engine.parseLrcLyrics(song.lyrics));
    } else {
      liveService.fetchLyrics(song.title, song.artist).then((lrc) => {
        if (lrc) {
          setLyrics(engine.parseLrcLyrics(lrc));
        }
      });
    }

    engine.loadSong(song, true);
    StorageService.addRecentlyPlayed(song);
  };

  // Next / Previous Handlers
  const handleNext = useCallback(() => {
    if (queue.length === 0) return;
    let nextIdx = currentIndex + 1;
    if (isShuffle) {
      nextIdx = Math.floor(Math.random() * queue.length);
    } else if (nextIdx >= queue.length) {
      if (repeatMode === 'all') {
        nextIdx = 0;
      } else {
        return;
      }
    }
    setCurrentIndex(nextIdx);
    const nextSong = queue[nextIdx];
    handlePlaySong(nextSong, queue);
  }, [queue, currentIndex, isShuffle, repeatMode]);

  const handlePrev = useCallback(() => {
    if (currentTime > 3) {
      engine.seek(0);
      return;
    }
    if (queue.length === 0) return;
    let prevIdx = currentIndex - 1;
    if (prevIdx < 0) {
      prevIdx = queue.length - 1;
    }
    setCurrentIndex(prevIdx);
    const prevSong = queue[prevIdx];
    handlePlaySong(prevSong, queue);
  }, [currentTime, queue, currentIndex]);

  // Global Keyboard Shortcuts (Space for Play/Pause)
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.target instanceof HTMLInputElement || e.target instanceof HTMLTextAreaElement) return;
      if (e.code === 'Space') {
        e.preventDefault();
        engine.togglePlay();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [engine]);

  const handleToggleLike = (song: Song) => {
    const liked = StorageService.toggleLike(song);
    setLikedSongs(StorageService.getLikedSongs());
    showToast(liked ? `Added "${song.title}" to Liked Songs` : `Removed "${song.title}" from Liked Songs`);
  };

  const handleRepeatToggle = () => {
    const modes: RepeatMode[] = ['off', 'all', 'one'];
    const nextMode = modes[(modes.indexOf(repeatMode) + 1) % modes.length];
    setRepeatMode(nextMode);
    engine.repeatMode = nextMode;
  };

  const handleCreatePlaylistSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newPlaylistTitle.trim()) return;
    const pl = StorageService.createPlaylist(newPlaylistTitle.trim());
    setCustomPlaylists(StorageService.getCustomPlaylists());
    setNewPlaylistTitle('');
    setIsCreatePlaylistOpen(false);
    showToast(`Created playlist "${pl.title}"`);
  };

  const handleDeletePlaylist = (id: string) => {
    const updated = customPlaylists.filter((p) => p.id !== id);
    setCustomPlaylists(updated);
    localStorage.setItem('wavyn_custom_playlists', JSON.stringify(updated));
    showToast('Playlist deleted');
  };

  const handleAddSongToPlaylist = (playlistId: string, song: Song) => {
    StorageService.addSongToPlaylist(playlistId, song);
    setCustomPlaylists(StorageService.getCustomPlaylists());
    showToast(`Added "${song.title}" to playlist`);
  };

  const allPlaylists = [...FEATURED_PLAYLISTS, ...customPlaylists];

  return (
    <div className="relative flex h-screen w-screen overflow-hidden bg-black font-sans text-[#F9F9F9] selection:bg-[#E85002] selection:text-white">
      {/* Ambient Animated Mesh Background */}
      <AnimatedMeshBackground />

      {/* 1. Left Sidebar */}
      <LeftSidebar
        currentTab={currentTab}
        onTabChange={(tab) => {
          setSelectedPlaylist(null);
          setSelectedArtist(null);
          setSelectedAlbum(null);
          setCurrentTab(tab);
        }}
        searchQuery={searchQuery}
        onSearchChange={(q) => {
          setSearchQuery(q);
          if (q.trim()) {
            setCurrentTab('search');
          }
        }}
        playlists={allPlaylists}
        onPlaylistClick={(pl) => {
          setSelectedPlaylist(pl);
          setCurrentTab('playlist-detail');
        }}
        onCreatePlaylist={() => setIsCreatePlaylistOpen(true)}
        onDeletePlaylist={handleDeletePlaylist}
      />

      {/* 2. Center Main View Area */}
      <main className="flex-1 h-full overflow-y-auto p-6 md:p-10 pb-24 md:pb-10 custom-scrollbar bg-transparent">
        {currentTab === 'home' && (
          <HomeView
            onPlaySong={handlePlaySong}
            onSelectPlaylist={(pl) => {
              setSelectedPlaylist(pl);
              setCurrentTab('playlist-detail');
            }}
            likedSongs={likedSongs}
            onToggleLike={handleToggleLike}
          />
        )}

        {currentTab === 'search' && (
          <SearchView
            onPlaySong={handlePlaySong}
            onSelectArtist={(artist) => {
              setSelectedArtist(artist);
              setCurrentTab('artist-detail');
            }}
            onSelectAlbum={(album) => {
              setSelectedAlbum(album);
              setCurrentTab('album-detail');
            }}
            onSelectPlaylist={(pl) => {
              setSelectedPlaylist(pl);
              setCurrentTab('playlist-detail');
            }}
          />
        )}

        {currentTab === 'discover' && (
          <DiscoverView
            onPlaySong={handlePlaySong}
            onSelectPlaylist={(pl) => {
              setSelectedPlaylist(pl);
              setCurrentTab('playlist-detail');
            }}
          />
        )}

        {currentTab === 'radio' && <RadioView onPlaySong={handlePlaySong} />}
        {currentTab === 'podcast' && <PodcastView onPlaySong={handlePlaySong} />}
        {currentTab === 'albums' && <AlbumsView onPlaySong={handlePlaySong} />}
        {currentTab === 'songs' && (
          <SongsView
            onPlaySong={handlePlaySong}
            likedSongs={likedSongs}
            onToggleLike={handleToggleLike}
          />
        )}
        {currentTab === 'artists' && (
          <ArtistsView
            onArtistClick={(artist: Artist) => {
              setSelectedArtist(artist);
              setCurrentTab('artist-detail');
            }}
          />
        )}

        {currentTab === 'playlist-detail' && selectedPlaylist && (
          <PlaylistDetailView
            playlist={selectedPlaylist}
            onBack={() => setCurrentTab('home')}
            onPlaySong={handlePlaySong}
            likedSongs={likedSongs}
            onToggleLike={handleToggleLike}
            onAddSongToPlaylist={handleAddSongToPlaylist}
          />
        )}

        {currentTab === 'artist-detail' && selectedArtist && (
          <ArtistDetailView
            artist={selectedArtist}
            onBack={() => setCurrentTab('home')}
            onPlaySong={handlePlaySong}
            onSelectAlbum={(alb) => {
              setSelectedAlbum(alb);
              setCurrentTab('album-detail');
            }}
            likedSongs={likedSongs}
            onToggleLike={handleToggleLike}
          />
        )}

        {currentTab === 'album-detail' && selectedAlbum && (
          <AlbumDetailView
            album={selectedAlbum}
            onBack={() => setCurrentTab('albums')}
            onPlaySong={handlePlaySong}
            likedSongs={likedSongs}
            onToggleLike={handleToggleLike}
          />
        )}
      </main>

      {/* 3. Right Sidebar Panel */}
      <RightPanel
        currentSong={currentSong}
        isPlaying={isPlaying}
        currentTime={currentTime}
        duration={duration}
        repeatMode={repeatMode}
        isShuffle={isShuffle}
        topArtists={TOP_ARTISTS}
        onPlayPause={() => engine.togglePlay()}
        onPrev={handlePrev}
        onNext={handleNext}
        onSeek={(sec) => engine.seek(sec)}
        onRepeatToggle={handleRepeatToggle}
        onShuffleToggle={() => setIsShuffle((prev) => !prev)}
        onArtistClick={(artist: Artist) => {
          setSelectedArtist(artist);
          setCurrentTab('artist-detail');
        }}
        onOpenFullPlayer={() => setIsFullPlayerOpen(true)}
      />

      {/* Full-Screen Synced Lyrics & Live Visualizer Modal */}
      <NowPlayingModal
        isOpen={isFullPlayerOpen}
        onClose={() => setIsFullPlayerOpen(false)}
        song={currentSong}
        isPlaying={isPlaying}
        currentTime={currentTime}
        duration={duration}
        volume={volume}
        repeatMode={repeatMode}
        isShuffle={isShuffle}
        isLiked={currentSong ? StorageService.isSongLiked(currentSong.id) : false}
        visualizerMode={visualizerMode}
        lyrics={lyrics}
        onPlayPause={() => engine.togglePlay()}
        onPrev={handlePrev}
        onNext={handleNext}
        onSeek={(sec) => engine.seek(sec)}
        onVolumeChange={(vol) => {
          setVolume(vol);
          engine.setVolume(vol);
        }}
        onRepeatToggle={handleRepeatToggle}
        onShuffleToggle={() => setIsShuffle((prev) => !prev)}
        onLikeToggle={() => currentSong && handleToggleLike(currentSong)}
        onVisualizerCycle={() => {
          const modes: VisualizerMode[] = ['bars', 'wave', 'off'];
          setVisualizerMode(modes[(modes.indexOf(visualizerMode) + 1) % modes.length]);
        }}
      />

      {/* Mobile/Tablet Bottom Navigation Bar (Normal, Docked) */}
      <BottomNav currentTab={currentTab} onTabChange={setCurrentTab} />

      {/* Toast Notification */}
      {toastText && (
        <div className="fixed bottom-20 md:bottom-6 left-1/2 -translate-x-1/2 z-50 px-5 py-2.5 rounded-full bg-[#E85002]/95 text-white font-bold text-xs shadow-2xl backdrop-blur-xl border border-white/20 animate-fadeIn">
          {toastText}
        </div>
      )}

      {/* Create Playlist Modal */}
      {isCreatePlaylistOpen && (
        <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-md flex items-center justify-center p-4">
          <div className="bg-[#0f1118] border border-white/10 p-6 rounded-3xl max-w-sm w-full shadow-2xl space-y-4 animate-scaleUp">
            <h3 className="text-lg font-bold text-white">Create New Playlist</h3>
            <form onSubmit={handleCreatePlaylistSubmit} className="space-y-4">
              <input
                type="text"
                placeholder="Playlist name..."
                value={newPlaylistTitle}
                onChange={(e) => setNewPlaylistTitle(e.target.value)}
                autoFocus
                className="w-full px-4 py-3 rounded-xl bg-white/10 border border-white/10 text-white placeholder-[#A7A7A7] focus:outline-none focus:border-[#E85002] text-sm"
              />
              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setIsCreatePlaylistOpen(false)}
                  className="px-4 py-2 rounded-xl text-xs font-bold text-[#A7A7A7] hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-xl text-xs font-bold bg-[#E85002] hover:bg-[#cf4502] text-white shadow-lg shadow-[#E85002]/25"
                >
                  Create
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
export default App;
