import React, { useState } from 'react';
import { Heart, Plus, Play, Music, ArrowLeft, Trash2 } from 'lucide-react';
import { Song, Playlist } from '../types';
import { StorageService } from '../services/storage';

interface LibraryViewProps {
  onPlaySong: (song: Song, queueList?: Song[]) => void;
  likedSongs: Song[];
  customPlaylists: Playlist[];
  onCreatePlaylist: () => void;
  onRefreshLibrary: () => void;
}

export const LibraryView: React.FC<LibraryViewProps> = ({
  onPlaySong,
  likedSongs,
  customPlaylists,
  onCreatePlaylist,
}) => {
  const [selectedPlaylist, setSelectedPlaylist] = useState<Playlist | null>(null);

  if (selectedPlaylist) {
    return (
      <div className="space-y-8 pb-28">
        <button
          onClick={() => setSelectedPlaylist(null)}
          className="flex items-center gap-2 text-sm font-semibold text-slate-400 hover:text-white transition-colors"
        >
          <ArrowLeft className="w-4 h-4" /> Back to Library
        </button>

        {/* Playlist Banner Header */}
        <div className="flex flex-col sm:flex-row items-center sm:items-end gap-6 p-6 rounded-3xl bg-gradient-to-r from-blue-900/40 via-purple-900/30 to-slate-900/40 border border-white/10 backdrop-blur-xl">
          <img
            src={selectedPlaylist.thumbnailUrl}
            alt={selectedPlaylist.title}
            className="w-44 h-44 rounded-2xl object-cover shadow-2xl ring-1 ring-white/20 shrink-0"
          />
          <div className="space-y-2 text-center sm:text-left">
            <span className="text-xs font-bold uppercase tracking-widest text-blue-400">Playlist</span>
            <h1 className="text-2xl sm:text-4xl font-extrabold text-white">{selectedPlaylist.title}</h1>
            <p className="text-sm text-slate-400">{selectedPlaylist.description || 'Custom User Playlist'}</p>
            <p className="text-xs text-slate-500 font-medium">{selectedPlaylist.songs.length} Tracks</p>
            {selectedPlaylist.songs.length > 0 && (
              <button
                onClick={() => onPlaySong(selectedPlaylist.songs[0], selectedPlaylist.songs)}
                className="mt-2 inline-flex items-center gap-2 px-6 py-2.5 rounded-full bg-blue-600 hover:bg-blue-500 text-white font-bold text-sm shadow-lg shadow-blue-500/25 transition-all"
              >
                <Play className="w-4 h-4 fill-current" /> Play All
              </button>
            )}
          </div>
        </div>

        {/* Tracks List */}
        <div className="space-y-2">
          {selectedPlaylist.songs.length > 0 ? (
            selectedPlaylist.songs.map((song, i) => (
              <div
                key={song.id}
                onClick={() => onPlaySong(song, selectedPlaylist.songs)}
                className="flex items-center justify-between p-3 rounded-2xl hover:bg-white/5 border border-transparent hover:border-white/10 transition-all cursor-pointer group"
              >
                <div className="flex items-center gap-4">
                  <span className="w-6 text-center text-xs font-mono text-slate-500 group-hover:text-blue-400">
                    {i + 1}
                  </span>
                  <img src={song.thumbnailUrl} alt={song.title} className="w-11 h-11 rounded-lg object-cover" />
                  <div>
                    <h4 className="font-bold text-sm text-white group-hover:text-blue-400 transition-colors">
                      {song.title}
                    </h4>
                    <p className="text-xs text-slate-400">{song.artist}</p>
                  </div>
                </div>
                <span className="text-xs font-mono text-slate-500">
                  {Math.floor(song.duration / 60)}:{(song.duration % 60).toString().padStart(2, '0')}
                </span>
              </div>
            ))
          ) : (
            <div className="text-center py-12 text-slate-500 space-y-2">
              <p className="font-semibold text-sm">This playlist is currently empty</p>
              <p className="text-xs">Add songs from Search or Home to populate this mix</p>
            </div>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8 pb-28">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-white">Your Music Library</h1>
          <p className="text-xs text-slate-400">All your favorite tracks and custom curated playlists</p>
        </div>
        <button
          onClick={onCreatePlaylist}
          className="flex items-center gap-2 px-4 py-2.5 rounded-full bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs shadow-lg shadow-blue-500/20 transition-all"
        >
          <Plus className="w-4 h-4" /> Create Playlist
        </button>
      </div>

      {/* Liked Songs Big Card */}
      <div
        onClick={() => {
          if (likedSongs.length > 0) {
            setSelectedPlaylist({
              id: 'liked-playlist',
              title: 'Liked Songs',
              description: 'Collection of all your favorited tracks.',
              thumbnailUrl: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80',
              songs: likedSongs
            });
          }
        }}
        className="group relative p-6 rounded-3xl bg-gradient-to-br from-purple-800/50 via-indigo-900/40 to-blue-900/30 border border-white/10 hover:border-purple-500/40 transition-all cursor-pointer shadow-xl"
      >
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center text-white shadow-xl shadow-purple-500/30">
              <Heart className="w-8 h-8 fill-white" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-white">Liked Songs</h2>
              <p className="text-xs text-purple-300 font-medium">{likedSongs.length} Songs Saved</p>
            </div>
          </div>
          {likedSongs.length > 0 && (
            <div className="w-12 h-12 rounded-full bg-white text-black flex items-center justify-center shadow-lg group-hover:scale-105 transition-transform">
              <Play className="w-5 h-5 fill-current translate-x-0.5" />
            </div>
          )}
        </div>
      </div>

      {/* Custom & Local Playlists */}
      <section className="space-y-4">
        <h2 className="text-xl font-bold text-white">Custom Playlists ({customPlaylists.length})</h2>

        {customPlaylists.length > 0 ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
            {customPlaylists.map((pl) => (
              <div
                key={pl.id}
                onClick={() => setSelectedPlaylist(pl)}
                className="group p-4 rounded-2xl bg-white/5 hover:bg-white/10 border border-white/5 hover:border-white/15 transition-all cursor-pointer flex flex-col"
              >
                <div className="relative aspect-square rounded-xl overflow-hidden mb-3 shadow-lg">
                  <img src={pl.thumbnailUrl} alt={pl.title} className="w-full h-full object-cover" />
                  <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                    <Play className="w-6 h-6 text-white fill-current translate-x-0.5" />
                  </div>
                </div>
                <h3 className="font-bold text-sm text-white truncate">{pl.title}</h3>
                <p className="text-xs text-slate-400">{pl.songs.length} Tracks</p>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-8 rounded-2xl bg-white/5 border border-dashed border-white/10 text-center space-y-3">
            <Music className="w-8 h-8 text-slate-500 mx-auto" />
            <p className="text-sm font-semibold text-slate-300">No Custom Playlists Yet</p>
            <button
              onClick={onCreatePlaylist}
              className="px-4 py-2 rounded-full bg-white/10 hover:bg-white/20 text-white text-xs font-bold transition-colors"
            >
              + Create Your First Playlist
            </button>
          </div>
        )}
      </section>
    </div>
  );
};
