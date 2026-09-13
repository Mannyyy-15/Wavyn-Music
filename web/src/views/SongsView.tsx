import React from 'react';
import { Play, Heart } from 'lucide-react';
import type { Song } from '../types';
import { FEATURED_SONGS } from '../services/musicData';

interface SongsViewProps {
  onPlaySong: (song: Song, queueList?: Song[]) => void;
  likedSongs: Song[];
  onToggleLike: (song: Song) => void;
}

export const SongsView: React.FC<SongsViewProps> = ({ onPlaySong, likedSongs, onToggleLike }) => {
  const isLiked = (songId: string) => likedSongs.some((s) => s.id === songId);

  return (
    <div className="space-y-8 pb-12 select-none">
      <div>
        <span className="text-[11px] uppercase font-bold tracking-wider text-slate-400">Library</span>
        <h2 className="text-2xl font-black text-white">All Songs</h2>
      </div>

      <div className="space-y-1">
        {FEATURED_SONGS.map((song) => {
          const liked = isLiked(song.id);
          return (
            <div
              key={`all-s-${song.id}`}
              onClick={() => onPlaySong(song, FEATURED_SONGS)}
              className="flex items-center justify-between p-3 rounded-2xl hover:bg-white/5 transition-all cursor-pointer group"
            >
              <div className="flex items-center gap-4 truncate">
                <span className="w-6 text-center text-xs font-mono font-bold text-slate-400 group-hover:text-purple-400">
                  {song.rank}
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

              <div className="flex items-center gap-4">
                <span className="text-xs font-mono text-slate-400">
                  {Math.floor(song.duration / 60)}:{(song.duration % 60).toString().padStart(2, '0')}
                </span>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onToggleLike(song);
                  }}
                  className="p-1 rounded-full text-slate-400 hover:text-pink-500"
                >
                  <Heart className={`w-4 h-4 ${liked ? 'fill-pink-500 text-pink-500' : ''}`} />
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
