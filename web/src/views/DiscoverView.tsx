import React from 'react';
import { Play, Sparkles } from 'lucide-react';
import type { Song, Playlist } from '../types';
import { FEATURED_SONGS, FEATURED_PLAYLISTS } from '../services/musicData';

interface DiscoverViewProps {
  onPlaySong: (song: Song, queueList?: Song[]) => void;
  onSelectPlaylist: (playlist: Playlist) => void;
}

export const DiscoverView: React.FC<DiscoverViewProps> = ({ onPlaySong, onSelectPlaylist }) => {
  return (
    <div className="space-y-8 pb-12 select-none">
      <div>
        <span className="text-[11px] uppercase font-bold tracking-wider text-slate-400">Explore New Beats</span>
        <h2 className="text-2xl font-black text-white">Discover</h2>
      </div>

      {/* Featured Mixes Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {FEATURED_PLAYLISTS.map((pl) => (
          <div
            key={pl.id}
            onClick={() => onSelectPlaylist(pl)}
            className="group relative p-4 rounded-3xl bg-[#181a24] hover:bg-[#202330] border border-white/5 hover:border-purple-500/30 transition-all duration-300 cursor-pointer flex flex-col shadow-xl"
          >
            <div className="relative aspect-video rounded-2xl overflow-hidden mb-4 shadow-lg">
              <img
                src={pl.thumbnailUrl}
                alt={pl.title}
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent" />
              <div className="absolute bottom-3 left-3 right-3 flex items-end justify-between">
                <span className="text-xs font-bold text-white/90">{pl.songs.length} Tracks</span>
                <div className="w-9 h-9 rounded-full bg-white text-black flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform">
                  <Play className="w-4 h-4 fill-current translate-x-0.5" />
                </div>
              </div>
            </div>
            <h3 className="font-bold text-sm text-white group-hover:text-purple-300 transition-colors truncate">
              {pl.title}
            </h3>
            <p className="text-xs text-slate-400 line-clamp-2 mt-1">{pl.description}</p>
          </div>
        ))}
      </div>

      {/* New Releases */}
      <div>
        <div className="flex items-center gap-2 mb-4">
          <Sparkles className="w-4 h-4 text-purple-400" />
          <h3 className="text-lg font-bold text-white">Fresh Releases</h3>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          {FEATURED_SONGS.slice(0, 6).map((song) => (
            <div
              key={`disc-${song.id}`}
              onClick={() => onPlaySong(song, FEATURED_SONGS)}
              className="flex items-center gap-3.5 p-3 rounded-2xl bg-white/5 hover:bg-white/10 border border-white/5 hover:border-purple-500/30 transition-all cursor-pointer group"
            >
              <img src={song.thumbnailUrl} alt={song.title} className="w-12 h-12 rounded-xl object-cover" />
              <div className="truncate flex-1">
                <h4 className="text-xs font-bold text-white group-hover:text-purple-400 transition-colors truncate">
                  {song.title}
                </h4>
                <p className="text-[11px] text-slate-400 truncate">{song.artist}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
