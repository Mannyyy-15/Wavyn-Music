import React from 'react';
import {
  Play,
  Heart,
  Clock,
  ArrowLeft,
  Disc3,
  MoreHorizontal
} from 'lucide-react';
import type { Song, Album } from '../types';

interface AlbumDetailViewProps {
  album: Album;
  onBack: () => void;
  onPlaySong: (song: Song, queueList?: Song[]) => void;
  likedSongs: Song[];
  onToggleLike: (song: Song) => void;
}

export const AlbumDetailView: React.FC<AlbumDetailViewProps> = ({
  album,
  onBack,
  onPlaySong,
  likedSongs,
  onToggleLike,
}) => {
  const totalDuration = album.songs.reduce((acc, s) => acc + s.duration, 0);
  const totalMinutes = Math.floor(totalDuration / 60);

  const formatDuration = (secs: number) => {
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  const isLiked = (songId: string) => likedSongs.some((s) => s.id === songId);

  return (
    <div className="space-y-8 pb-16 select-none">
      <button
        onClick={onBack}
        className="flex items-center gap-2 text-xs font-bold text-slate-400 hover:text-white transition-colors"
      >
        <ArrowLeft className="w-4 h-4" /> Back
      </button>

      {/* Album Banner */}
      <div className="flex flex-col md:flex-row items-center md:items-end gap-8 p-6 md:p-8 rounded-3xl bg-gradient-to-r from-purple-900/40 via-indigo-900/30 to-[#121520] border border-white/10 shadow-2xl relative overflow-hidden">
        <div className="relative w-48 h-48 md:w-56 md:h-56 rounded-2xl overflow-hidden shadow-2xl ring-1 ring-white/20 shrink-0 group">
          <img
            src={album.thumbnailUrl}
            alt={album.title}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          />
        </div>

        <div className="flex-1 space-y-3 text-center md:text-left">
          <span className="text-[11px] font-bold uppercase tracking-widest text-purple-400">
            Album
          </span>
          <h1 className="text-3xl md:text-5xl font-black text-white leading-tight">
            {album.title}
          </h1>
          <p className="text-sm text-slate-300 font-semibold">{album.artist}</p>
          <div className="flex items-center justify-center md:justify-start gap-2 text-xs text-slate-400 font-medium">
            <span>{album.year}</span>
            <span>•</span>
            <span>{album.genre}</span>
            <span>•</span>
            <span>{album.songs.length} Tracks</span>
            <span>•</span>
            <span className="flex items-center gap-1">
              <Clock className="w-3.5 h-3.5" /> {totalMinutes} min
            </span>
          </div>
        </div>
      </div>

      {/* Action Play Button */}
      <div className="flex items-center gap-4">
        {album.songs.length > 0 && (
          <button
            onClick={() => onPlaySong(album.songs[0], album.songs)}
            className="flex items-center gap-2.5 px-7 py-3 rounded-full bg-purple-600 hover:bg-purple-500 text-white font-extrabold text-xs shadow-lg shadow-purple-500/25 hover:scale-105 active:scale-95 transition-all"
          >
            <Play className="w-4 h-4 fill-current translate-x-0.5" />
            <span>Play Album</span>
          </button>
        )}
      </div>

      {/* Tracks Table */}
      <div className="space-y-1">
        {album.songs.map((song, idx) => {
          const liked = isLiked(song.id);
          return (
            <div
              key={song.id}
              onClick={() => onPlaySong(song, album.songs)}
              className="grid grid-cols-12 gap-4 items-center px-4 py-2.5 rounded-2xl hover:bg-white/5 transition-all cursor-pointer group"
            >
              <div className="col-span-8 flex items-center gap-4 truncate">
                <span className="w-6 text-center text-xs font-mono font-bold text-slate-400 group-hover:text-purple-400">
                  {(idx + 1).toString().padStart(2, '0')}
                </span>
                <div className="truncate">
                  <h4 className="text-xs font-extrabold text-white group-hover:text-purple-400 transition-colors truncate">
                    {song.title}
                  </h4>
                  <p className="text-[11px] text-slate-400 truncate">{song.artist}</p>
                </div>
              </div>

              <div className="col-span-2 text-xs font-mono text-slate-400 text-right md:text-left">
                {formatDuration(song.duration)}
              </div>

              <div className="col-span-2 flex items-center justify-end gap-2">
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
  );
};
