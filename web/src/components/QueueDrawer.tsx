import React from 'react';
import { X, Play, Trash2, Sparkles, Music2 } from 'lucide-react';
import { Song } from '../types';

interface QueueDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  queue: Song[];
  currentIndex: number;
  onSongClick: (index: number) => void;
  onRemoveSong: (index: number) => void;
  onClearQueue: () => void;
}

export const QueueDrawer: React.FC<QueueDrawerProps> = ({
  isOpen,
  onClose,
  queue,
  currentIndex,
  onSongClick,
  onRemoveSong,
  onClearQueue,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-y-0 right-0 w-full sm:w-96 bg-[#0c0e15]/95 backdrop-blur-2xl border-l border-white/10 p-6 flex flex-col z-50 shadow-2xl animate-slideLeft select-none">
      {/* Header */}
      <div className="flex items-center justify-between pb-4 border-b border-white/10 mb-4">
        <div className="flex items-center gap-2.5">
          <Music2 className="w-5 h-5 text-blue-400" />
          <h2 className="text-lg font-bold text-white">Play Queue</h2>
        </div>
        <div className="flex items-center gap-2">
          {queue.length > 1 && (
            <button
              onClick={onClearQueue}
              className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 transition-colors"
              title="Clear Queue"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/10 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Queue List */}
      <div className="flex-1 overflow-y-auto space-y-2 pr-1 custom-scrollbar">
        {queue.map((song, idx) => {
          const isCurrent = idx === currentIndex;
          return (
            <div
              key={`${song.id}-${idx}`}
              onClick={() => onSongClick(idx)}
              className={`flex items-center justify-between p-2.5 rounded-xl cursor-pointer group transition-all ${
                isCurrent
                  ? 'bg-blue-600/20 border border-blue-500/40 text-white'
                  : 'hover:bg-white/5 text-slate-300'
              }`}
            >
              <div className="flex items-center gap-3 truncate">
                <div className="relative w-10 h-10 rounded-lg overflow-hidden shrink-0">
                  <img src={song.thumbnailUrl} alt={song.title} className="w-full h-full object-cover" />
                  {isCurrent && (
                    <div className="absolute inset-0 bg-blue-600/60 flex items-center justify-center">
                      <Play className="w-4 h-4 text-white fill-current animate-pulse" />
                    </div>
                  )}
                </div>

                <div className="truncate">
                  <p className={`text-sm font-semibold truncate ${isCurrent ? 'text-blue-400' : 'text-white'}`}>
                    {song.title}
                  </p>
                  <p className="text-xs text-slate-400 truncate">{song.artist}</p>
                </div>
              </div>

              {queue.length > 1 && (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onRemoveSong(idx);
                  }}
                  className="p-1.5 rounded-lg opacity-0 group-hover:opacity-100 hover:text-rose-400 transition-opacity"
                >
                  <X className="w-4 h-4" />
                </button>
              )}
            </div>
          );
        })}
      </div>

      {/* Smart Automix Suggestion Footer */}
      <div className="pt-4 border-t border-white/10 flex items-center justify-between text-xs text-slate-400">
        <span className="flex items-center gap-1.5 text-blue-400 font-semibold">
          <Sparkles className="w-3.5 h-3.5" /> Infinite Automix Active
        </span>
        <span>{queue.length} Tracks in Queue</span>
      </div>
    </div>
  );
};
