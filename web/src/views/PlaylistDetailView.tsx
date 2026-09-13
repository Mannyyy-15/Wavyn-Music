import React from 'react';
import {
  Play,
  Heart,
  Clock,
  MoreHorizontal,
  Plus,
  ArrowLeft,
  Music2,
  Share2
} from 'lucide-react';
import type { Song, Playlist } from '../types';
import { ALL_SONGS } from '../services/musicData';

interface PlaylistDetailViewProps {
  playlist: Playlist;
  onBack: () => void;
  onPlaySong: (song: Song, queueList?: Song[]) => void;
  onToggleLike: (song: Song) => void;
  likedSongs: Song[];
  onAddSongToPlaylist?: (playlistId: string, song: Song) => void;
}

export const PlaylistDetailView: React.FC<PlaylistDetailViewProps> = ({
  playlist,
  onBack,
  onPlaySong,
  onToggleLike,
  likedSongs,
  onAddSongToPlaylist,
}) => {
  const totalDuration = playlist.songs.reduce((acc, s) => acc + s.duration, 0);
  const totalMinutes = Math.floor(totalDuration / 60);

  const formatDuration = (secs: number) => {
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  const isLiked = (songId: string) => likedSongs.some((s) => s.id === songId);

  // Recommendations to add to this playlist
  const recommendedSongs = ALL_SONGS.filter(
    (s) => !playlist.songs.some((ps) => ps.id === s.id)
  ).slice(0, 4);

  return (
    <div className="space-y-8 pb-16 select-none">
      {/* Back button */}
      <button
        onClick={onBack}
        className="flex items-center gap-2 text-xs font-bold text-slate-400 hover:text-white transition-colors"
      >
        <ArrowLeft className="w-4 h-4" /> Back
      </button>

      {/* Playlist Hero Banner */}
      <div className="flex flex-col md:flex-row items-center md:items-end gap-8 p-6 md:p-8 rounded-3xl bg-gradient-to-r from-purple-900/40 via-indigo-900/30 to-[#121520] border border-white/10 shadow-2xl relative overflow-hidden">
        <div className="relative w-48 h-48 md:w-56 md:h-56 rounded-2xl overflow-hidden shadow-2xl ring-1 ring-white/20 shrink-0 group">
          <img
            src={playlist.thumbnailUrl}
            alt={playlist.title}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          />
        </div>

        <div className="flex-1 space-y-3 text-center md:text-left">
          <span className="text-[11px] font-bold uppercase tracking-widest text-purple-400">
            Public Playlist
          </span>
          <h1 className="text-3xl md:text-5xl font-black text-white leading-tight">
            {playlist.title}
          </h1>
          <p className="text-sm text-slate-300 max-w-xl">{playlist.description}</p>
          <div className="flex items-center justify-center md:justify-start gap-2 text-xs text-slate-400 font-medium">
            <span className="text-white font-bold">{playlist.author || 'Wavyn Music'}</span>
            <span>•</span>
            <span>{playlist.songs.length} Songs</span>
            <span>•</span>
            <span className="flex items-center gap-1">
              <Clock className="w-3.5 h-3.5" /> about {totalMinutes} min
            </span>
          </div>
        </div>
      </div>

      {/* Action Buttons Row */}
      <div className="flex items-center gap-4">
        {playlist.songs.length > 0 && (
          <button
            onClick={() => onPlaySong(playlist.songs[0], playlist.songs)}
            className="flex items-center gap-2.5 px-7 py-3 rounded-full bg-purple-600 hover:bg-purple-500 text-white font-extrabold text-xs shadow-lg shadow-purple-500/25 hover:scale-105 active:scale-95 transition-all"
          >
            <Play className="w-4 h-4 fill-current translate-x-0.5" />
            <span>Play All</span>
          </button>
        )}

        <button
          onClick={() => {
            navigator.clipboard?.writeText(window.location.href);
          }}
          className="p-3 rounded-full bg-white/5 hover:bg-white/10 text-slate-400 hover:text-white transition-all"
          title="Share Playlist"
        >
          <Share2 className="w-4 h-4" />
        </button>
      </div>

      {/* Tracks Table */}
      <div className="w-full">
        {/* Table Header */}
        <div className="grid grid-cols-12 gap-4 px-4 py-2 text-[11px] font-bold text-slate-400 uppercase tracking-wider border-b border-white/5">
          <div className="col-span-6 md:col-span-5 flex items-center gap-4">
            <span className="w-6 text-center">#</span>
            <span>Title</span>
          </div>
          <div className="hidden md:block md:col-span-3">Album</div>
          <div className="col-span-3 md:col-span-2 text-right md:text-left">Duration</div>
          <div className="col-span-3 md:col-span-2 flex items-center justify-end md:justify-between">
            <span>Like</span>
            <span className="w-6" />
          </div>
        </div>

        {/* Rows */}
        <div className="space-y-1 mt-2">
          {playlist.songs.map((song, idx) => {
            const liked = isLiked(song.id);
            return (
              <div
                key={`${song.id}-${idx}`}
                onClick={() => onPlaySong(song, playlist.songs)}
                className="grid grid-cols-12 gap-4 items-center px-4 py-2.5 rounded-2xl hover:bg-white/5 transition-all cursor-pointer group"
              >
                <div className="col-span-6 md:col-span-5 flex items-center gap-4 truncate">
                  <span className="w-6 text-center text-xs font-mono font-bold text-slate-400 group-hover:text-purple-400">
                    {(idx + 1).toString().padStart(2, '0')}
                  </span>
                  <div className="relative w-11 h-11 rounded-xl overflow-hidden shadow-md shrink-0">
                    <img src={song.thumbnailUrl} alt={song.title} className="w-full h-full object-cover" />
                    <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                      <Play className="w-4 h-4 text-white fill-current translate-x-0.5" />
                    </div>
                  </div>
                  <div className="truncate">
                    <h4 className="text-xs font-extrabold text-white group-hover:text-purple-400 transition-colors truncate">
                      {song.title}
                    </h4>
                    <p className="text-[11px] text-slate-400 truncate">{song.artist}</p>
                  </div>
                </div>

                <div className="hidden md:block md:col-span-3 text-xs text-slate-400 truncate">
                  {song.album || song.title}
                </div>

                <div className="col-span-3 md:col-span-2 text-xs font-mono text-slate-400 text-right md:text-left">
                  {formatDuration(song.duration)}
                </div>

                <div className="col-span-3 md:col-span-2 flex items-center justify-end md:justify-between">
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      onToggleLike(song);
                    }}
                    className="p-1 rounded-full text-slate-400 hover:text-pink-500"
                  >
                    <Heart className={`w-3.5 h-3.5 ${liked ? 'fill-pink-500 text-pink-500' : ''}`} />
                  </button>
                  <button
                    onClick={(e) => e.stopPropagation()}
                    className="p-1 rounded-lg text-slate-400 hover:text-white opacity-0 group-hover:opacity-100 transition-opacity"
                  >
                    <MoreHorizontal className="w-4 h-4" />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Recommended Songs Section (Spotify Feature) */}
      {recommendedSongs.length > 0 && (
        <section className="pt-8 border-t border-white/5 space-y-4">
          <div className="flex items-center gap-2">
            <Music2 className="w-5 h-5 text-purple-400" />
            <h3 className="text-lg font-bold text-white">Recommended For This Playlist</h3>
          </div>
          <div className="space-y-2">
            {recommendedSongs.map((song) => (
              <div
                key={`rec-${song.id}`}
                className="flex items-center justify-between p-3 rounded-2xl bg-white/5 hover:bg-white/10 transition-all group"
              >
                <div
                  className="flex items-center gap-3.5 cursor-pointer truncate flex-1"
                  onClick={() => onPlaySong(song, ALL_SONGS)}
                >
                  <img src={song.thumbnailUrl} alt={song.title} className="w-11 h-11 rounded-xl object-cover" />
                  <div className="truncate">
                    <h4 className="text-xs font-bold text-white group-hover:text-purple-400 transition-colors truncate">
                      {song.title}
                    </h4>
                    <p className="text-[11px] text-slate-400 truncate">{song.artist}</p>
                  </div>
                </div>

                {onAddSongToPlaylist && (
                  <button
                    onClick={() => onAddSongToPlaylist(playlist.id, song)}
                    className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-white/10 hover:bg-purple-600 text-white font-bold text-xs transition-all"
                  >
                    <Plus className="w-3.5 h-3.5" /> Add
                  </button>
                )}
              </div>
            ))}
          </div>
        </section>
      )}
    </div>
  );
};
