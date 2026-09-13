import React from 'react';
import { Users } from 'lucide-react';
import type { Artist } from '../types';
import { TOP_ARTISTS } from '../services/musicData';

interface ArtistsViewProps {
  onArtistClick: (artist: Artist) => void;
}

export const ArtistsView: React.FC<ArtistsViewProps> = ({ onArtistClick }) => {
  return (
    <div className="space-y-8 pb-12 select-none">
      <div>
        <span className="text-[11px] uppercase font-bold tracking-wider text-slate-400">Library</span>
        <h2 className="text-2xl font-black text-white flex items-center gap-2">
          <Users className="w-6 h-6 text-purple-400" /> Artists
        </h2>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-5">
        {TOP_ARTISTS.map((artist) => (
          <div
            key={`art-${artist.id}`}
            onClick={() => onArtistClick(artist)}
            className="group p-5 rounded-3xl bg-[#181a24] hover:bg-[#202330] border border-white/5 hover:border-purple-500/30 transition-all cursor-pointer shadow-xl flex flex-col items-center text-center"
          >
            <img
              src={artist.avatarUrl}
              alt={artist.name}
              className="w-24 h-24 rounded-full object-cover ring-2 ring-white/10 group-hover:scale-105 transition-transform mb-3 shadow-lg"
            />
            <h3 className="font-bold text-sm text-white group-hover:text-purple-400 transition-colors">{artist.name}</h3>
            <p className="text-xs text-slate-400 mt-0.5">{artist.albumsCount} Albums • {artist.followers} Fans</p>
          </div>
        ))}
      </div>
    </div>
  );
};
