import React from 'react';
import { Radio, Play } from 'lucide-react';
import type { Song } from '../types';
import { FEATURED_SONGS } from '../services/musicData';

interface RadioViewProps {
  onPlaySong: (song: Song, queueList?: Song[]) => void;
}

const STATIONS = [
  { id: 'r1', name: 'Synthwave FM', desc: 'Non-stop retro 80s synth and chill drives', color: 'from-purple-600 to-indigo-600', songIndex: 1 },
  { id: 'r2', name: 'Pop Hits Radio', desc: 'Chart-topping pop and upbeat dance anthems', color: 'from-pink-600 to-rose-600', songIndex: 3 },
  { id: 'r3', name: 'Night Drive Station', desc: 'Atmospheric electronic & nightcall beats', color: 'from-blue-600 to-cyan-600', songIndex: 5 },
  { id: 'r4', name: 'Chill & Lo-Fi Lounge', desc: 'Smooth relaxing vibes for focus and coding', color: 'from-emerald-600 to-teal-600', songIndex: 2 }
];

export const RadioView: React.FC<RadioViewProps> = ({ onPlaySong }) => {
  return (
    <div className="space-y-8 pb-12 select-none">
      <div>
        <span className="text-[11px] uppercase font-bold tracking-wider text-slate-400">Live Infinite Streams</span>
        <h2 className="text-2xl font-black text-white flex items-center gap-2">
          <Radio className="w-6 h-6 text-purple-400" /> Radio Stations
        </h2>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
        {STATIONS.map((station) => (
          <div
            key={station.id}
            onClick={() => onPlaySong(FEATURED_SONGS[station.songIndex], FEATURED_SONGS)}
            className={`p-6 rounded-3xl bg-gradient-to-br ${station.color} shadow-2xl hover:scale-[1.02] transition-all cursor-pointer relative overflow-hidden group`}
          >
            <div className="absolute inset-0 bg-black/20" />
            <div className="relative z-10 flex items-start justify-between">
              <div className="space-y-2 max-w-[200px]">
                <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-black/40 text-[10px] font-bold uppercase tracking-wider text-white">
                  <span className="w-1.5 h-1.5 rounded-full bg-red-500 animate-ping" /> Live Radio
                </span>
                <h3 className="text-xl font-black text-white">{station.name}</h3>
                <p className="text-xs text-white/80">{station.desc}</p>
              </div>

              <div className="w-12 h-12 rounded-full bg-white text-black flex items-center justify-center shadow-xl group-hover:scale-110 transition-transform">
                <Play className="w-5 h-5 fill-current translate-x-0.5" />
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
