import React from 'react';
import { Play } from 'lucide-react';
import type { Song } from '../types';
import { FEATURED_SONGS } from '../services/musicData';

interface AlbumsViewProps {
  onPlaySong: (song: Song, queueList?: Song[]) => void;
}

export const AlbumsView: React.FC<AlbumsViewProps> = ({ onPlaySong }) => {
  return (
    <div className="space-y-8 pb-12 select-none">
      <div>
        <span className="text-[11px] uppercase font-bold tracking-wider text-slate-400">Library</span>
        <h2 className="text-2xl font-black text-white">Albums</h2>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-5">
        {FEATURED_SONGS.map((song) => (
          <div
            key={`alb-${song.id}`}
            onClick={() => onPlaySong(song, FEATURED_SONGS)}
            className="group p-4 rounded-3xl bg-[#181a24] hover:bg-[#202330] border border-white/5 hover:border-purple-500/30 transition-all cursor-pointer shadow-xl flex flex-col"
          >
            <div className="relative aspect-square rounded-2xl overflow-hidden mb-3 shadow-lg">
              <img src={song.thumbnailUrl} alt={song.album || song.title} className="w-full h-full object-cover group-hover:scale-105 transition-transform" />
              <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                <div className="w-11 h-11 rounded-full bg-white text-black flex items-center justify-center shadow-lg">
                  <Play className="w-5 h-5 fill-current translate-x-0.5" />
                </div>
              </div>
            </div>
            <h3 className="font-bold text-sm text-white truncate">{song.album || song.title}</h3>
            <p className="text-xs text-slate-400 truncate">{song.artist} • {song.year}</p>
          </div>
        ))}
      </div>
    </div>
  );
};
