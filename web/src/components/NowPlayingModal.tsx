import React, { useEffect, useRef } from 'react';
import {
  ChevronDown,
  Play,
  Pause,
  SkipBack,
  SkipForward,
  Shuffle,
  Repeat,
  Repeat1,
  Heart,
  Volume2,
  VolumeX,
  Sparkles,
  Radio
} from 'lucide-react';
import { Song, LyricLine, RepeatMode, VisualizerMode } from '../types';
import { Visualizer } from './Visualizer';

interface NowPlayingModalProps {
  isOpen: boolean;
  onClose: () => void;
  song: Song | null;
  isPlaying: boolean;
  currentTime: number;
  duration: number;
  volume: number;
  repeatMode: RepeatMode;
  isShuffle: boolean;
  isLiked: boolean;
  visualizerMode: VisualizerMode;
  lyrics: LyricLine[];
  onPlayPause: () => void;
  onPrev: () => void;
  onNext: () => void;
  onSeek: (seconds: number) => void;
  onVolumeChange: (vol: number) => void;
  onRepeatToggle: () => void;
  onShuffleToggle: () => void;
  onLikeToggle: () => void;
  onVisualizerCycle: () => void;
}

export const NowPlayingModal: React.FC<NowPlayingModalProps> = ({
  isOpen,
  onClose,
  song,
  isPlaying,
  currentTime,
  duration,
  volume,
  repeatMode,
  isShuffle,
  isLiked,
  visualizerMode,
  lyrics,
  onPlayPause,
  onPrev,
  onNext,
  onSeek,
  onVolumeChange,
  onRepeatToggle,
  onShuffleToggle,
  onLikeToggle,
  onVisualizerCycle,
}) => {
  const lyricsContainerRef = useRef<HTMLDivElement | null>(null);

  // Find active lyric index based on currentTime
  const activeLyricIndex = lyrics.findIndex((line, i) => {
    const nextLine = lyrics[i + 1];
    if (nextLine) {
      return currentTime >= line.time && currentTime < nextLine.time;
    }
    return currentTime >= line.time;
  });

  // Auto-scroll lyrics container to keep active line centered
  useEffect(() => {
    if (activeLyricIndex >= 0 && lyricsContainerRef.current) {
      const container = lyricsContainerRef.current;
      const activeEl = container.children[activeLyricIndex] as HTMLElement;
      if (activeEl) {
        container.scrollTo({
          top: activeEl.offsetTop - container.clientHeight / 2 + activeEl.clientHeight / 2,
          behavior: 'smooth'
        });
      }
    }
  }, [activeLyricIndex]);

  if (!isOpen || !song) return null;

  const formatTime = (secs: number) => {
    if (isNaN(secs) || secs < 0) return '0:00';
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  return (
    <div className="fixed inset-0 z-50 flex flex-col bg-[#08090e]/95 backdrop-blur-3xl overflow-hidden select-none animate-fadeIn">
      {/* Ambient background blur circles */}
      <div className="absolute -top-40 -left-40 w-[600px] h-[600px] bg-blue-600/25 rounded-full blur-[140px] pointer-events-none" />
      <div className="absolute -bottom-40 -right-40 w-[600px] h-[600px] bg-purple-600/20 rounded-full blur-[140px] pointer-events-none" />

      {/* Top Header */}
      <header className="flex items-center justify-between px-6 py-5 border-b border-white/5 relative z-10">
        <button
          onClick={onClose}
          className="p-2 rounded-full hover:bg-white/10 text-slate-400 hover:text-white transition-all"
        >
          <ChevronDown className="w-6 h-6" />
        </button>

        <div className="flex flex-col items-center">
          <span className="text-[11px] font-bold tracking-widest text-blue-400 uppercase flex items-center gap-1.5">
            <Radio className="w-3.5 h-3.5 animate-pulse" /> Playing From Wavyn Music
          </span>
          <span className="text-sm font-semibold text-white truncate max-w-xs">{song.album || song.title}</span>
        </div>

        <div className="flex items-center gap-2">
          <span className="px-2.5 py-1 rounded-full text-[10px] font-extrabold uppercase tracking-wider bg-blue-500/20 text-blue-300 border border-blue-500/30 flex items-center gap-1">
            <Sparkles className="w-3 h-3 text-blue-400" /> Lossless Hi-Fi
          </span>
        </div>
      </header>

      {/* Main Content: Left Album & Right Synced Lyrics */}
      <div className="flex-1 grid grid-cols-1 lg:grid-cols-2 gap-8 p-6 md:p-12 items-center overflow-y-auto z-10 max-w-7xl mx-auto w-full">
        {/* Left Column: Artwork & Track Metadata */}
        <div className="flex flex-col items-center justify-center max-w-md mx-auto w-full">
          <div className="relative w-64 h-64 md:w-80 md:h-80 rounded-3xl overflow-hidden shadow-2xl shadow-blue-900/30 ring-1 ring-white/20 mb-8 group">
            <img
              src={song.thumbnailUrl}
              alt={song.title}
              className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
            />
          </div>

          <div className="w-full flex items-center justify-between mb-4">
            <div className="flex flex-col truncate pr-4">
              <h2 className="text-2xl md:text-3xl font-extrabold text-white truncate">{song.title}</h2>
              <p className="text-base text-slate-400 font-medium truncate">{song.artist}</p>
            </div>
            <button
              onClick={onLikeToggle}
              className={`p-3 rounded-full transition-all ${
                isLiked ? 'text-pink-500 scale-110' : 'text-slate-400 hover:text-white hover:bg-white/10'
              }`}
            >
              <Heart className={`w-7 h-7 ${isLiked ? 'fill-pink-500' : ''}`} />
            </button>
          </div>

          {/* Visualizer Canvas under metadata */}
          {visualizerMode !== 'off' && (
            <div className="w-full flex justify-center mb-4">
              <Visualizer mode={visualizerMode} accentColor="#3b82f6" />
            </div>
          )}

          {/* Progress Slider */}
          <div className="w-full space-y-1.5 mb-6">
            <input
              type="range"
              min="0"
              max={duration || 100}
              value={currentTime}
              onChange={(e) => onSeek(parseFloat(e.target.value))}
              className="w-full h-1.5 bg-white/20 rounded-lg appearance-none cursor-pointer accent-blue-500 hover:h-2 transition-all"
            />
            <div className="flex justify-between text-xs font-mono text-slate-400">
              <span>{formatTime(currentTime)}</span>
              <span>{formatTime(duration)}</span>
            </div>
          </div>

          {/* Player Buttons */}
          <div className="flex items-center justify-between w-full max-w-xs mb-4">
            <button
              onClick={onShuffleToggle}
              className={`p-2 rounded-xl transition-all ${
                isShuffle ? 'text-blue-400 bg-blue-500/10' : 'text-slate-400 hover:text-white'
              }`}
            >
              <Shuffle className="w-5 h-5" />
            </button>

            <button
              onClick={onPrev}
              className="p-2 text-slate-200 hover:text-white hover:scale-110 active:scale-95 transition-all"
            >
              <SkipBack className="w-7 h-7 fill-current" />
            </button>

            <button
              onClick={onPlayPause}
              className="w-16 h-16 rounded-full bg-white text-black hover:scale-105 active:scale-95 flex items-center justify-center shadow-xl shadow-white/30 transition-all"
            >
              {isPlaying ? (
                <Pause className="w-7 h-7 fill-current" />
              ) : (
                <Play className="w-7 h-7 fill-current translate-x-0.5" />
              )}
            </button>

            <button
              onClick={onNext}
              className="p-2 text-slate-200 hover:text-white hover:scale-110 active:scale-95 transition-all"
            >
              <SkipForward className="w-7 h-7 fill-current" />
            </button>

            <button
              onClick={onRepeatToggle}
              className={`p-2 rounded-xl transition-all ${
                repeatMode !== 'off' ? 'text-blue-400 bg-blue-500/10' : 'text-slate-400 hover:text-white'
              }`}
            >
              {repeatMode === 'one' ? <Repeat1 className="w-5 h-5" /> : <Repeat className="w-5 h-5" />}
            </button>
          </div>
        </div>

        {/* Right Column: Live Karaoke Synchronized Lyrics */}
        <div className="h-full flex flex-col bg-white/5 rounded-3xl p-6 md:p-8 border border-white/10 backdrop-blur-md relative overflow-hidden">
          <div className="flex items-center justify-between mb-4 pb-3 border-b border-white/10">
            <span className="text-xs font-bold uppercase tracking-widest text-slate-400">Synchronized Lyrics</span>
            <button
              onClick={onVisualizerCycle}
              className="text-xs font-semibold text-blue-400 hover:text-blue-300"
            >
              Visualizer: {visualizerMode.toUpperCase()}
            </button>
          </div>

          <div
            ref={lyricsContainerRef}
            className="flex-1 overflow-y-auto space-y-6 pr-2 custom-scrollbar scroll-smooth"
          >
            {lyrics.length > 0 ? (
              lyrics.map((line, idx) => {
                const isActive = idx === activeLyricIndex;
                const isPassed = idx < activeLyricIndex;
                return (
                  <p
                    key={idx}
                    onClick={() => onSeek(line.time)}
                    className={`text-xl md:text-2xl font-bold cursor-pointer transition-all duration-300 ${
                      isActive
                        ? 'text-white scale-105 origin-left drop-shadow-[0_0_12px_rgba(255,255,255,0.7)]'
                        : isPassed
                        ? 'text-slate-500 hover:text-slate-300'
                        : 'text-slate-600 hover:text-slate-400'
                    }`}
                  >
                    {line.text}
                  </p>
                );
              })
            ) : (
              <div className="h-full flex flex-col items-center justify-center text-slate-500 space-y-2">
                <p className="text-base font-semibold">Instrumental or Lyrics not available</p>
                <p className="text-xs">Enjoy the rhythm and lossless sound</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
