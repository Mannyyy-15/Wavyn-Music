import React, { useState } from 'react';
import {
  Play,
  Pause,
  SkipBack,
  SkipForward,
  Shuffle,
  Repeat,
  Repeat1,
  Volume2,
  VolumeX,
  Heart,
  ListMusic,
  Mic2,
  Activity,
  Maximize2
} from 'lucide-react';
import confetti from 'canvas-confetti';
import { Song, RepeatMode, VisualizerMode } from '../types';
import { Visualizer } from './Visualizer';

interface PlayerBarProps {
  currentSong: Song | null;
  isPlaying: boolean;
  currentTime: number;
  duration: number;
  volume: number;
  repeatMode: RepeatMode;
  isShuffle: boolean;
  isLiked: boolean;
  visualizerMode: VisualizerMode;
  onPlayPause: () => void;
  onPrev: () => void;
  onNext: () => void;
  onSeek: (seconds: number) => void;
  onVolumeChange: (vol: number) => void;
  onRepeatToggle: () => void;
  onShuffleToggle: () => void;
  onLikeToggle: () => void;
  onVisualizerCycle: () => void;
  onOpenLyrics: () => void;
  onOpenQueue: () => void;
  onOpenFullPlayer: () => void;
}

export const PlayerBar: React.FC<PlayerBarProps> = ({
  currentSong,
  isPlaying,
  currentTime,
  duration,
  volume,
  repeatMode,
  isShuffle,
  isLiked,
  visualizerMode,
  onPlayPause,
  onPrev,
  onNext,
  onSeek,
  onVolumeChange,
  onRepeatToggle,
  onShuffleToggle,
  onLikeToggle,
  onVisualizerCycle,
  onOpenLyrics,
  onOpenQueue,
  onOpenFullPlayer,
}) => {
  const [prevVolume, setPrevVolume] = useState(0.8);

  const formatTime = (secs: number) => {
    if (isNaN(secs) || secs < 0) return '0:00';
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  const handleLikeClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (!isLiked) {
      confetti({
        particleCount: 40,
        spread: 60,
        origin: { y: 0.9, x: 0.1 },
        colors: ['#ec4899', '#f43f5e', '#8b5cf6', '#3b82f6']
      });
    }
    onLikeToggle();
  };

  const handleMuteToggle = () => {
    if (volume > 0) {
      setPrevVolume(volume);
      onVolumeChange(0);
    } else {
      onVolumeChange(prevVolume || 0.8);
    }
  };

  if (!currentSong) {
    return (
      <div className="fixed bottom-16 md:bottom-0 left-0 right-0 h-20 bg-[#090b10]/90 backdrop-blur-2xl border-t border-white/10 flex items-center justify-center text-slate-500 text-sm font-medium z-30 select-none">
        <span>Select a song or playlist to start listening</span>
      </div>
    );
  }

  const progressPercent = duration > 0 ? (currentTime / duration) * 100 : 0;

  return (
    <div className="fixed bottom-16 md:bottom-0 left-0 right-0 h-22 bg-[#090b10]/95 backdrop-blur-2xl border-t border-white/10 px-4 md:px-6 flex items-center justify-between z-40 select-none shadow-2xl">
      {/* Top micro progress bar for mobile */}
      <div
        className="absolute top-0 left-0 right-0 h-1 bg-white/10 md:hidden cursor-pointer"
        onClick={(e) => {
          const rect = e.currentTarget.getBoundingClientRect();
          const clickX = e.clientX - rect.left;
          const newTime = (clickX / rect.width) * duration;
          onSeek(newTime);
        }}
      >
        <div
          className="h-full bg-gradient-to-r from-blue-500 to-indigo-500 rounded-r"
          style={{ width: `${progressPercent}%` }}
        />
      </div>

      {/* Left: Track Info & Like */}
      <div className="flex items-center gap-3.5 min-w-[200px] max-w-[280px]">
        <div
          className="relative group w-13 h-13 rounded-xl overflow-hidden shadow-lg shadow-black/40 ring-1 ring-white/15 cursor-pointer shrink-0"
          onClick={onOpenFullPlayer}
        >
          <img
            src={currentSong.thumbnailUrl}
            alt={currentSong.title}
            className={`w-full h-full object-cover transition-transform duration-500 ${
              isPlaying ? 'scale-105' : 'scale-100'
            }`}
          />
          <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
            <Maximize2 className="w-5 h-5 text-white" />
          </div>
        </div>

        <div className="flex flex-col truncate">
          <span
            className="font-bold text-sm text-white hover:underline cursor-pointer truncate"
            onClick={onOpenFullPlayer}
          >
            {currentSong.title}
          </span>
          <span className="text-xs text-slate-400 hover:text-slate-200 cursor-pointer truncate">
            {currentSong.artist}
          </span>
        </div>

        <button
          onClick={handleLikeClick}
          className={`p-2 rounded-full transition-all duration-200 ${
            isLiked
              ? 'text-pink-500 scale-110'
              : 'text-slate-400 hover:text-white hover:bg-white/5'
          }`}
          title={isLiked ? 'Remove from Liked' : 'Save to Liked'}
        >
          <Heart className={`w-5 h-5 ${isLiked ? 'fill-pink-500' : ''}`} />
        </button>
      </div>

      {/* Center: Controls & Time Slider */}
      <div className="flex flex-col items-center gap-1.5 flex-1 max-w-xl px-4">
        {/* Buttons Row */}
        <div className="flex items-center gap-4">
          <button
            onClick={onShuffleToggle}
            className={`p-1.5 rounded-lg transition-colors ${
              isShuffle ? 'text-blue-400 bg-blue-500/10' : 'text-slate-400 hover:text-white'
            }`}
            title="Shuffle"
          >
            <Shuffle className="w-4 h-4" />
          </button>

          <button
            onClick={onPrev}
            className="p-1.5 rounded-lg text-slate-300 hover:text-white hover:scale-110 active:scale-95 transition-all"
            title="Previous"
          >
            <SkipBack className="w-5 h-5 fill-current" />
          </button>

          <button
            onClick={onPlayPause}
            className="w-10 h-10 rounded-full bg-white text-black hover:scale-105 active:scale-95 flex items-center justify-center shadow-lg shadow-white/20 transition-all"
            title={isPlaying ? 'Pause' : 'Play'}
          >
            {isPlaying ? (
              <Pause className="w-5 h-5 fill-current" />
            ) : (
              <Play className="w-5 h-5 fill-current translate-x-0.5" />
            )}
          </button>

          <button
            onClick={onNext}
            className="p-1.5 rounded-lg text-slate-300 hover:text-white hover:scale-110 active:scale-95 transition-all"
            title="Next"
          >
            <SkipForward className="w-5 h-5 fill-current" />
          </button>

          <button
            onClick={onRepeatToggle}
            className={`p-1.5 rounded-lg transition-colors relative ${
              repeatMode !== 'off' ? 'text-blue-400 bg-blue-500/10' : 'text-slate-400 hover:text-white'
            }`}
            title={`Repeat: ${repeatMode}`}
          >
            {repeatMode === 'one' ? (
              <Repeat1 className="w-4 h-4" />
            ) : (
              <Repeat className="w-4 h-4" />
            )}
          </button>
        </div>

        {/* Progress Slider */}
        <div className="hidden md:flex items-center gap-3 w-full">
          <span className="text-[11px] font-mono text-slate-400 w-9 text-right">
            {formatTime(currentTime)}
          </span>
          <div className="relative flex-1 group py-2 cursor-pointer">
            <input
              type="range"
              min="0"
              max={duration || 100}
              value={currentTime}
              onChange={(e) => onSeek(parseFloat(e.target.value))}
              className="w-full h-1 bg-white/20 rounded-lg appearance-none cursor-pointer accent-blue-500 group-hover:h-1.5 transition-all"
            />
          </div>
          <span className="text-[11px] font-mono text-slate-400 w-9">
            {formatTime(duration)}
          </span>
        </div>
      </div>

      {/* Right: Extra Controls & Volume */}
      <div className="hidden lg:flex items-center gap-3 min-w-[200px] justify-end">
        {/* Live Audio Visualizer Canvas */}
        {visualizerMode !== 'off' && (
          <div className="hidden xl:block">
            <Visualizer mode={visualizerMode} accentColor="#3b82f6" />
          </div>
        )}

        {/* Visualizer Mode Cycle Button */}
        <button
          onClick={onVisualizerCycle}
          className={`p-2 rounded-xl transition-all ${
            visualizerMode !== 'off'
              ? 'text-blue-400 bg-blue-500/10 border border-blue-500/30'
              : 'text-slate-400 hover:text-white hover:bg-white/5'
          }`}
          title={`Audio Visualizer: ${visualizerMode}`}
        >
          <Activity className="w-4 h-4" />
        </button>

        {/* Lyrics Button */}
        <button
          onClick={onOpenLyrics}
          className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-white/5 transition-all"
          title="Synced Lyrics"
        >
          <Mic2 className="w-4 h-4" />
        </button>

        {/* Queue Button */}
        <button
          onClick={onOpenQueue}
          className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-white/5 transition-all"
          title="Current Queue"
        >
          <ListMusic className="w-4 h-4" />
        </button>

        {/* Volume */}
        <div className="flex items-center gap-2 pl-2 border-l border-white/10">
          <button
            onClick={handleMuteToggle}
            className="text-slate-400 hover:text-white transition-colors"
          >
            {volume === 0 ? <VolumeX className="w-4 h-4" /> : <Volume2 className="w-4 h-4" />}
          </button>
          <input
            type="range"
            min="0"
            max="1"
            step="0.01"
            value={volume}
            onChange={(e) => onVolumeChange(parseFloat(e.target.value))}
            className="w-20 h-1 bg-white/20 rounded-lg appearance-none cursor-pointer accent-blue-500"
          />
        </div>
      </div>
    </div>
  );
};
