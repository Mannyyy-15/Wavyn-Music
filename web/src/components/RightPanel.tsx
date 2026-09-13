import React from 'react';
import {
  Bell,
  ChevronDown,
  Play,
  Pause,
  SkipBack,
  SkipForward,
  Shuffle,
  Repeat,
  Repeat1,
  Maximize2
} from 'lucide-react';
import type { Song, Artist, RepeatMode } from '../types';

interface RightPanelProps {
  currentSong: Song | null;
  isPlaying: boolean;
  currentTime: number;
  duration: number;
  repeatMode: RepeatMode;
  isShuffle: boolean;
  topArtists: Artist[];
  onPlayPause: () => void;
  onPrev: () => void;
  onNext: () => void;
  onSeek: (seconds: number) => void;
  onRepeatToggle: () => void;
  onShuffleToggle: () => void;
  onArtistClick: (artist: Artist) => void;
  onOpenFullPlayer: () => void;
}

export const RightPanel: React.FC<RightPanelProps> = ({
  currentSong,
  isPlaying,
  currentTime,
  duration,
  repeatMode,
  isShuffle,
  topArtists,
  onPlayPause,
  onPrev,
  onNext,
  onSeek,
  onRepeatToggle,
  onShuffleToggle,
  onArtistClick,
  onOpenFullPlayer,
}) => {
  const formatTime = (secs: number) => {
    if (isNaN(secs) || secs < 0) return '0:00';
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  const progressPercent = duration > 0 ? (currentTime / duration) * 100 : 0;

  return (
    <aside className="w-80 h-full bg-[#12141a] border-l border-white/5 p-5 flex flex-col justify-between select-none shrink-0 overflow-y-auto custom-scrollbar">
      {/* Top Section: User Profile Bar */}
      <div>
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-3">
            <div className="relative">
              <img
                src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80"
                alt="User Profile"
                className="w-10 h-10 rounded-full object-cover ring-2 ring-white/10"
              />
              <div className="absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full bg-[#10b981] ring-2 ring-[#12141a]" />
            </div>
            <div className="flex flex-col">
              <div className="flex items-center gap-1.5 cursor-pointer group">
                <span className="text-xs font-bold text-white group-hover:text-purple-400 transition-colors">
                  Wavyn Listener
                </span>
                <ChevronDown className="w-3.5 h-3.5 text-slate-400 group-hover:text-white transition-colors" />
              </div>
              <span className="text-[11px] text-slate-400">Premium Member</span>
            </div>
          </div>

          <button
            className="w-9 h-9 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center text-slate-400 hover:text-white transition-all relative"
            title="Notifications"
          >
            <Bell className="w-4 h-4" />
            <div className="absolute top-2 right-2 w-2 h-2 rounded-full bg-purple-500" />
          </button>
        </div>

        {/* Top Artist Header */}
        <div className="flex items-center justify-between mb-4">
          <div className="flex flex-col">
            <span className="text-[10px] uppercase font-bold tracking-wider text-slate-400">Top</span>
            <h3 className="text-base font-extrabold text-white">Artist</h3>
          </div>
          <button className="text-xs font-semibold text-slate-400 hover:text-purple-400 transition-colors">
            See all
          </button>
        </div>

        {/* Top Artists List */}
        <div className="space-y-3 mb-6">
          {topArtists.map((artist) => (
            <div
              key={artist.id}
              onClick={() => onArtistClick(artist)}
              className="flex items-center justify-between p-2 rounded-2xl hover:bg-white/5 transition-all cursor-pointer group"
            >
              <div className="flex items-center gap-3">
                <img
                  src={artist.avatarUrl}
                  alt={artist.name}
                  className="w-10 h-10 rounded-xl object-cover ring-1 ring-white/10 group-hover:scale-105 transition-transform"
                />
                <div className="flex flex-col">
                  <span className="text-xs font-bold text-white group-hover:text-purple-400 transition-colors truncate max-w-[130px]">
                    {artist.name}
                  </span>
                  <span className="text-[10px] text-slate-400 flex items-center gap-1">
                    📁 {artist.albumsCount} Albums
                  </span>
                </div>
              </div>

              <span className="text-xs font-mono font-bold text-slate-400 group-hover:text-purple-400">
                {artist.rank}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Bottom Floating Now Playing Card (Exact match to reference) */}
      {currentSong ? (
        <div className="relative rounded-3xl overflow-hidden bg-gradient-to-b from-purple-900/40 via-[#1c1a29]/80 to-[#12141a] border border-white/10 p-3 shadow-2xl group">
          {/* Header Image Artwork */}
          <div className="relative h-36 rounded-2xl overflow-hidden mb-3 shadow-lg cursor-pointer" onClick={onOpenFullPlayer}>
            <img
              src={currentSong.thumbnailUrl}
              alt={currentSong.title}
              className={`w-full h-full object-cover group-hover:scale-105 transition-transform duration-700 ${
                isPlaying ? 'scale-105' : ''
              }`}
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent" />
            <div className="absolute top-2.5 right-2.5 px-2 py-0.5 rounded-full bg-black/60 backdrop-blur-md text-[9px] font-mono font-bold text-white/90 uppercase border border-white/15">
              Hi-Fi
            </div>
            <button
              onClick={(e) => {
                e.stopPropagation();
                onOpenFullPlayer();
              }}
              className="absolute bottom-2.5 right-2.5 p-1.5 rounded-full bg-black/50 text-white/80 hover:text-white hover:bg-black/80 transition-all opacity-0 group-hover:opacity-100"
            >
              <Maximize2 className="w-3.5 h-3.5" />
            </button>
          </div>

          {/* Floating Controls Sub-Card */}
          <div className="bg-[#181a24]/90 backdrop-blur-xl border border-white/10 rounded-2xl p-3.5 shadow-xl space-y-3">
            {/* Title & Artist */}
            <div className="text-center truncate">
              <h4 className="text-xs font-extrabold text-white truncate cursor-pointer hover:underline" onClick={onOpenFullPlayer}>
                {currentSong.title}
              </h4>
              <p className="text-[11px] text-slate-400 truncate mt-0.5">{currentSong.artist}</p>
            </div>

            {/* Time Slider */}
            <div className="space-y-1">
              <div
                className="relative h-1 bg-white/15 rounded-full cursor-pointer overflow-hidden"
                onClick={(e) => {
                  const rect = e.currentTarget.getBoundingClientRect();
                  const clickX = e.clientX - rect.left;
                  const newTime = (clickX / rect.width) * duration;
                  onSeek(newTime);
                }}
              >
                <div
                  className="h-full bg-gradient-to-r from-purple-500 to-pink-500 rounded-full transition-all"
                  style={{ width: `${progressPercent}%` }}
                />
              </div>
              <div className="flex justify-between text-[10px] font-mono text-slate-400">
                <span>{formatTime(currentTime)}</span>
                <span>{formatTime(duration)}</span>
              </div>
            </div>

            {/* Buttons Row */}
            <div className="flex items-center justify-between px-1">
              <button
                onClick={onShuffleToggle}
                className={`p-1 transition-colors ${
                  isShuffle ? 'text-purple-400' : 'text-slate-400 hover:text-white'
                }`}
                title="Shuffle"
              >
                <Shuffle className="w-3.5 h-3.5" />
              </button>

              <button
                onClick={onPrev}
                className="p-1 text-slate-300 hover:text-white transition-colors"
                title="Previous"
              >
                <SkipBack className="w-4 h-4 fill-current" />
              </button>

              <button
                onClick={onPlayPause}
                className="w-9 h-9 rounded-full bg-white text-black flex items-center justify-center shadow-lg hover:scale-105 active:scale-95 transition-all"
                title={isPlaying ? 'Pause' : 'Play'}
              >
                {isPlaying ? (
                  <Pause className="w-4 h-4 fill-current" />
                ) : (
                  <Play className="w-4 h-4 fill-current translate-x-0.5" />
                )}
              </button>

              <button
                onClick={onNext}
                className="p-1 text-slate-300 hover:text-white transition-colors"
                title="Next"
              >
                <SkipForward className="w-4 h-4 fill-current" />
              </button>

              <button
                onClick={onRepeatToggle}
                className={`p-1 transition-colors ${
                  repeatMode !== 'off' ? 'text-purple-400' : 'text-slate-400 hover:text-white'
                }`}
                title={`Repeat: ${repeatMode}`}
              >
                {repeatMode === 'one' ? <Repeat1 className="w-3.5 h-3.5" /> : <Repeat className="w-3.5 h-3.5" />}
              </button>
            </div>
          </div>
        </div>
      ) : (
        <div className="p-4 rounded-2xl bg-white/5 border border-dashed border-white/10 text-center text-xs text-slate-400">
          Select a song to start listening
        </div>
      )}
    </aside>
  );
};
