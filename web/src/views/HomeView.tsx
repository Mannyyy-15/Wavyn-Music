import React from 'react';
import {
  Play,
  Heart,
  MoreVertical,
  ChevronLeft,
  ChevronRight
} from 'lucide-react';
import type { Song, Playlist } from '../types';
import { FEATURED_SONGS, FEATURED_PLAYLISTS } from '../services/musicData';

interface HomeViewProps {
  onPlaySong: (song: Song, queueList?: Song[]) => void;
  onSelectPlaylist: (playlist: Playlist) => void;
  likedSongs: Song[];
  onToggleLike: (song: Song) => void;
}

export const HomeView: React.FC<HomeViewProps> = ({
  onPlaySong,
  onSelectPlaylist,
  likedSongs,
  onToggleLike,
}) => {
  const formatDuration = (secs: number) => {
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  const isLiked = (songId: string) => likedSongs.some((s) => s.id === songId);

  return (
    <div className="space-y-8 pb-12 select-none">
      {/* Top Header Navigation Arrows */}
      <div className="flex items-center gap-2">
        <button className="w-8 h-8 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center text-slate-400 hover:text-white transition-all">
          <ChevronLeft className="w-4 h-4" />
        </button>
        <button className="w-8 h-8 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center text-slate-400 hover:text-white transition-all">
          <ChevronRight className="w-4 h-4" />
        </button>
      </div>

      {/* SECTION: Top Trending */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <div>
            <span className="text-[11px] uppercase font-bold tracking-wider text-slate-400">Top</span>
            <h2 className="text-2xl font-black text-white">Trending</h2>
          </div>
          <button className="text-xs font-semibold text-slate-400 hover:text-purple-400 transition-colors">
            See all
          </button>
        </div>

        {/* Hero Mesh Gradient Banner */}
        <div className="relative rounded-3xl overflow-hidden p-8 md:p-10 shadow-2xl border border-white/10 min-h-[220px] flex flex-col justify-between group">
          {/* Animated Vibrant Mesh Gradient Background */}
          <div className="absolute inset-0 bg-gradient-to-r from-[#8b5cf6] via-[#ec4899] to-[#3b82f6] opacity-90 transition-transform duration-1000 group-hover:scale-105" />
          <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent" />
          <div className="absolute -right-10 -bottom-10 w-96 h-96 bg-purple-500/30 rounded-full blur-3xl pointer-events-none" />

          {/* Banner Content */}
          <div className="relative z-10 space-y-2 max-w-md">
            <span className="text-xs font-bold uppercase tracking-wider text-white/80">
              Playlist
            </span>
            <h1 className="text-3xl md:text-4xl font-extrabold text-white leading-tight drop-shadow-md">
              Top Song <br /> Of The Week
            </h1>
          </div>

          {/* Banner Action Buttons */}
          <div className="relative z-10 flex items-center gap-3 pt-6">
            <button
              onClick={() => onPlaySong(FEATURED_SONGS[0], FEATURED_SONGS)}
              className="flex items-center gap-2 px-6 py-2.5 rounded-full bg-white text-black font-extrabold text-xs shadow-xl shadow-black/30 hover:scale-105 active:scale-95 transition-all"
            >
              <Play className="w-3.5 h-3.5 fill-current" />
              <span>Play</span>
            </button>

            <button
              onClick={() => onSelectPlaylist(FEATURED_PLAYLISTS[0])}
              className="flex items-center gap-2 px-6 py-2.5 rounded-full bg-white/15 hover:bg-white/25 text-white font-bold text-xs backdrop-blur-md border border-white/20 hover:scale-105 active:scale-95 transition-all"
            >
              <span>View Playlist</span>
            </button>
          </div>
        </div>
      </div>

      {/* SECTION: Global Top 50 Songs Table */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <div>
            <span className="text-[11px] uppercase font-bold tracking-wider text-slate-400">Global</span>
            <h2 className="text-2xl font-black text-white">Top 50</h2>
          </div>
          <button className="text-xs font-semibold text-slate-400 hover:text-purple-400 transition-colors">
            See all
          </button>
        </div>

        {/* Table Container */}
        <div className="w-full">
          {/* Table Header */}
          <div className="grid grid-cols-12 gap-4 px-4 py-2 text-[11px] font-bold text-slate-400 uppercase tracking-wider border-b border-white/5">
            <div className="col-span-6 md:col-span-5 flex items-center gap-4">
              <span className="w-6 text-center">#</span>
              <span>Name Song</span>
            </div>
            <div className="hidden md:block md:col-span-3">Artist</div>
            <div className="col-span-3 md:col-span-2 text-right md:text-left">Time</div>
            <div className="col-span-3 md:col-span-2 flex items-center justify-end md:justify-between">
              <span>Like</span>
              <span className="w-6" />
            </div>
          </div>

          {/* Table Rows */}
          <div className="space-y-1 mt-2">
            {FEATURED_SONGS.map((song) => {
              const liked = isLiked(song.id);
              return (
                <div
                  key={song.id}
                  onClick={() => onPlaySong(song, FEATURED_SONGS)}
                  className="grid grid-cols-12 gap-4 items-center px-4 py-2.5 rounded-2xl hover:bg-white/5 transition-all cursor-pointer group"
                >
                  {/* Rank & Title */}
                  <div className="col-span-6 md:col-span-5 flex items-center gap-4 truncate">
                    <span className="w-6 text-center text-xs font-mono font-bold text-slate-400 group-hover:text-purple-400">
                      {song.rank}
                    </span>
                    <div className="relative w-11 h-11 rounded-xl overflow-hidden shadow-md shrink-0">
                      <img
                        src={song.thumbnailUrl}
                        alt={song.title}
                        className="w-full h-full object-cover group-hover:scale-105 transition-transform"
                      />
                      <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                        <Play className="w-4 h-4 text-white fill-current translate-x-0.5" />
                      </div>
                    </div>
                    <div className="truncate">
                      <h4 className="text-xs font-extrabold text-white group-hover:text-purple-400 transition-colors truncate">
                        {song.title}
                      </h4>
                      <p className="text-[11px] text-slate-400 truncate md:hidden">{song.artist}</p>
                    </div>
                  </div>

                  {/* Artist */}
                  <div className="hidden md:block md:col-span-3 text-xs text-slate-300 truncate font-medium">
                    {song.artist}
                  </div>

                  {/* Duration */}
                  <div className="col-span-3 md:col-span-2 text-xs font-mono text-slate-400 text-right md:text-left">
                    {formatDuration(song.duration)}
                  </div>

                  {/* Like & Menu */}
                  <div className="col-span-3 md:col-span-2 flex items-center justify-end md:justify-between">
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        onToggleLike(song);
                      }}
                      className="flex items-center gap-1 text-xs text-slate-400 hover:text-pink-500 transition-colors"
                    >
                      <Heart
                        className={`w-3.5 h-3.5 ${
                          liked ? 'fill-pink-500 text-pink-500' : 'text-slate-400'
                        }`}
                      />
                      <span className="text-[11px] font-mono">{song.likesCount || '1k'}</span>
                    </button>

                    <button
                      onClick={(e) => e.stopPropagation()}
                      className="p-1 rounded-lg text-slate-400 hover:text-white opacity-0 group-hover:opacity-100 transition-opacity"
                    >
                      <MoreVertical className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
