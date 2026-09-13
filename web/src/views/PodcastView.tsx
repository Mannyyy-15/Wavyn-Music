import React from 'react';
import { Mic, Play } from 'lucide-react';
import type { Song } from '../types';
import { FEATURED_SONGS } from '../services/musicData';

interface PodcastViewProps {
  onPlaySong: (song: Song) => void;
}

const PODCASTS = [
  { id: 'p1', title: 'Billboard Top Breakdown', host: 'Alex Rivera', ep: 'Episode 42', img: 'https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=400&auto=format&fit=crop&q=80' },
  { id: 'p2', title: 'Behind The Synthesizers', host: 'Elena Rostova', ep: 'Episode 18', img: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&auto=format&fit=crop&q=80' },
  { id: 'p3', title: 'The Audio Engineering Deep Dive', host: 'Dave Miller', ep: 'Episode 29', img: 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=400&auto=format&fit=crop&q=80' }
];

export const PodcastView: React.FC<PodcastViewProps> = ({ onPlaySong }) => {
  return (
    <div className="space-y-8 pb-12 select-none">
      <div>
        <span className="text-[11px] uppercase font-bold tracking-wider text-slate-400">Audio Shows & Talks</span>
        <h2 className="text-2xl font-black text-white flex items-center gap-2">
          <Mic className="w-6 h-6 text-purple-400" /> Podcasts
        </h2>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {PODCASTS.map((pod) => (
          <div
            key={pod.id}
            onClick={() => onPlaySong(FEATURED_SONGS[0])}
            className="group p-4 rounded-3xl bg-[#181a24] hover:bg-[#202330] border border-white/5 hover:border-purple-500/30 transition-all cursor-pointer shadow-xl"
          >
            <div className="relative aspect-square rounded-2xl overflow-hidden mb-3 shadow-lg">
              <img src={pod.img} alt={pod.title} className="w-full h-full object-cover group-hover:scale-105 transition-transform" />
              <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                <div className="w-12 h-12 rounded-full bg-white text-black flex items-center justify-center shadow-lg">
                  <Play className="w-5 h-5 fill-current translate-x-0.5" />
                </div>
              </div>
            </div>
            <span className="text-[10px] font-bold text-purple-400 uppercase">{pod.ep}</span>
            <h3 className="font-bold text-sm text-white truncate mt-0.5">{pod.title}</h3>
            <p className="text-xs text-slate-400">{pod.host}</p>
          </div>
        ))}
      </div>
    </div>
  );
};
