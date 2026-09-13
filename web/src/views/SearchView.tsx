import React, { useState, useEffect } from 'react';
import { Search as SearchIcon, Play, X, Music2, Disc3, Users, Loader2 } from 'lucide-react';
import type { Song, Album, Artist, Playlist } from '../types';
import { LiveMusicService } from '../services/LiveMusicService';
import { GENRE_CATEGORIES } from '../services/musicData';

interface SearchViewProps {
  onPlaySong: (song: Song, queueList?: Song[]) => void;
  onSelectArtist: (artist: Artist) => void;
  onSelectAlbum: (album: Album) => void;
  onSelectPlaylist: (playlist: Playlist) => void;
}

export const SearchView: React.FC<SearchViewProps> = ({
  onPlaySong,
  onSelectArtist,
  onSelectAlbum,
}) => {
  const [query, setQuery] = useState('');
  const [selectedTag, setSelectedTag] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [searchResults, setSearchResults] = useState<{
    songs: Song[];
    artists: Artist[];
    albums: Album[];
  }>({ songs: [], artists: [], albums: [] });

  const liveService = LiveMusicService.getInstance();

  useEffect(() => {
    const searchTerm = selectedTag || query;
    if (!searchTerm.trim()) {
      // Default trending initial load
      setIsLoading(true);
      liveService.search('Top Hits', 25).then((res) => {
        setSearchResults(res);
        setIsLoading(false);
      });
      return;
    }

    setIsLoading(true);
    const timeoutId = setTimeout(() => {
      liveService.search(searchTerm, 30).then((res) => {
        setSearchResults(res);
        setIsLoading(false);
      });
    }, 300);

    return () => clearTimeout(timeoutId);
  }, [query, selectedTag]);

  return (
    <div className="space-y-8 pb-16 select-none">
      {/* Search Input Bar */}
      <div className="relative max-w-2xl">
        <SearchIcon className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
        <input
          type="text"
          placeholder="Search any song, artist, album globally..."
          value={query}
          onChange={(e) => {
            setSelectedTag(null);
            setQuery(e.target.value);
          }}
          autoFocus
          className="w-full pl-12 pr-12 py-3.5 rounded-2xl bg-white/10 border border-white/10 text-white placeholder-slate-400 focus:outline-none focus:border-purple-500 focus:ring-2 focus:ring-purple-500/20 text-sm md:text-base backdrop-blur-xl transition-all"
        />
        {isLoading ? (
          <Loader2 className="absolute right-4 top-1/2 -translate-y-1/2 w-5 h-5 text-purple-400 animate-spin" />
        ) : query ? (
          <button
            onClick={() => setQuery('')}
            className="absolute right-3 top-1/2 -translate-y-1/2 p-1 rounded-full text-slate-400 hover:text-white"
          >
            <X className="w-4 h-4" />
          </button>
        ) : null}
      </div>

      {/* Filter Tag Chips */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1 custom-scrollbar">
        <button
          onClick={() => {
            setSelectedTag(null);
            setQuery('');
          }}
          className={`px-4 py-1.5 rounded-full text-xs font-bold transition-all shrink-0 ${
            selectedTag === null && !query
              ? 'bg-purple-600 text-white shadow-lg shadow-purple-500/20'
              : 'bg-white/5 text-slate-300 hover:bg-white/10'
          }`}
        >
          All
        </button>
        {['Pop', 'Hip-Hop', 'Electronic', 'Synthwave', 'Indie', 'Bollywood', 'Rock', 'R&B'].map((tag) => (
          <button
            key={tag}
            onClick={() => {
              setQuery('');
              setSelectedTag(selectedTag === tag ? null : tag);
            }}
            className={`px-4 py-1.5 rounded-full text-xs font-bold transition-all shrink-0 ${
              selectedTag === tag
                ? 'bg-purple-600 text-white shadow-lg shadow-purple-500/20'
                : 'bg-white/5 text-slate-300 hover:bg-white/10'
            }`}
          >
            {tag}
          </button>
        ))}
      </div>

      {/* Top Results Section */}
      {searchResults.songs.length > 0 && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Top Result Featured Card */}
          <div
            onClick={() => onPlaySong(searchResults.songs[0], searchResults.songs)}
            className="p-6 rounded-3xl bg-[#181a24] hover:bg-[#202330] border border-white/5 hover:border-purple-500/30 transition-all cursor-pointer group shadow-xl flex flex-col justify-between"
          >
            <div>
              <span className="text-xs font-bold uppercase tracking-wider text-purple-400">Top Result</span>
              <img
                src={searchResults.songs[0].thumbnailUrl}
                alt={searchResults.songs[0].title}
                className="w-28 h-28 rounded-2xl object-cover shadow-lg my-4 group-hover:scale-105 transition-transform"
              />
              <h3 className="text-2xl font-black text-white">{searchResults.songs[0].title}</h3>
              <p className="text-sm text-slate-400 mt-1">{searchResults.songs[0].artist}</p>
            </div>

            <div className="flex items-center justify-between pt-4">
              <span className="text-[11px] font-bold text-white px-3 py-1 rounded-full bg-black/40 uppercase">
                {searchResults.songs[0].genres?.[0] || 'Song'}
              </span>
              <div className="w-12 h-12 rounded-full bg-purple-600 text-white flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform">
                <Play className="w-5 h-5 fill-current translate-x-0.5" />
              </div>
            </div>
          </div>

          {/* Matching Songs List */}
          <div className="lg:col-span-2 space-y-2">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Songs</span>
            {searchResults.songs.slice(0, 5).map((song) => (
              <div
                key={`q-song-${song.id}`}
                onClick={() => onPlaySong(song, searchResults.songs)}
                className="flex items-center justify-between p-2.5 rounded-2xl hover:bg-white/5 transition-all cursor-pointer group"
              >
                <div className="flex items-center gap-3.5 truncate">
                  <div className="relative w-11 h-11 rounded-xl overflow-hidden shrink-0">
                    <img src={song.thumbnailUrl} alt={song.title} className="w-full h-full object-cover" />
                    <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                      <Play className="w-4 h-4 text-white fill-current translate-x-0.5" />
                    </div>
                  </div>
                  <div className="truncate">
                    <h4 className="text-xs font-bold text-white group-hover:text-purple-400 transition-colors truncate">
                      {song.title}
                    </h4>
                    <p className="text-[11px] text-slate-400 truncate">{song.artist}</p>
                  </div>
                </div>
                <span className="text-xs font-mono text-slate-400">
                  {Math.floor(song.duration / 60)}:{(song.duration % 60).toString().padStart(2, '0')}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Artists Matches */}
      {searchResults.artists.length > 0 && (
        <section className="space-y-4">
          <h3 className="text-lg font-bold text-white flex items-center gap-2">
            <Users className="w-5 h-5 text-purple-400" /> Artists
          </h3>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {searchResults.artists.slice(0, 4).map((artist) => (
              <div
                key={artist.id}
                onClick={() => onSelectArtist(artist)}
                className="group p-4 rounded-3xl bg-[#181a24] hover:bg-[#202330] border border-white/5 hover:border-purple-500/30 transition-all cursor-pointer shadow-xl flex flex-col items-center text-center"
              >
                <img
                  src={artist.avatarUrl}
                  alt={artist.name}
                  className="w-20 h-20 rounded-full object-cover ring-2 ring-white/10 group-hover:scale-105 transition-transform mb-3"
                />
                <h4 className="font-bold text-xs text-white group-hover:text-purple-400 transition-colors truncate w-full">
                  {artist.name}
                </h4>
                <p className="text-[10px] text-slate-400 mt-0.5">Artist</p>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* Albums Matches */}
      {searchResults.albums.length > 0 && (
        <section className="space-y-4">
          <h3 className="text-lg font-bold text-white flex items-center gap-2">
            <Disc3 className="w-5 h-5 text-purple-400" /> Albums
          </h3>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {searchResults.albums.slice(0, 4).map((album) => (
              <div
                key={album.id}
                onClick={() => onSelectAlbum(album)}
                className="group p-4 rounded-3xl bg-[#181a24] hover:bg-[#202330] border border-white/5 hover:border-purple-500/30 transition-all cursor-pointer shadow-xl flex flex-col"
              >
                <div className="relative aspect-square rounded-2xl overflow-hidden mb-3 shadow-lg">
                  <img src={album.thumbnailUrl} alt={album.title} className="w-full h-full object-cover group-hover:scale-105 transition-transform" />
                  <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                    <Play className="w-5 h-5 text-white fill-current translate-x-0.5" />
                  </div>
                </div>
                <h4 className="font-bold text-xs text-white group-hover:text-purple-400 transition-colors truncate">
                  {album.title}
                </h4>
                <p className="text-[10px] text-slate-400 mt-0.5 truncate">{album.artist} • {album.year}</p>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* Browse All Genres Categories Grid */}
      <section className="space-y-4 pt-4">
        <h3 className="text-lg font-bold text-white">Browse Categories</h3>
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-3">
          {GENRE_CATEGORIES.map((cat) => (
            <div
              key={cat.id}
              onClick={() => {
                setQuery('');
                setSelectedTag(cat.name);
              }}
              style={{ background: cat.color }}
              className="p-4 rounded-2xl h-28 flex flex-col justify-between shadow-lg cursor-pointer hover:scale-105 transition-transform relative overflow-hidden"
            >
              <span className="font-extrabold text-sm text-white drop-shadow-md">{cat.name}</span>
              <img
                src={cat.image}
                alt={cat.name}
                className="absolute -right-2 -bottom-2 w-14 h-14 rounded-lg object-cover rotate-12 shadow-xl opacity-80"
              />
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};
