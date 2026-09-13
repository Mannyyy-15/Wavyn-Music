import React from 'react';
import {
  Home,
  Compass,
  Radio,
  Mic,
  Disc,
  Music2,
  Users,
  Search,
  Trash2,
  Plus
} from 'lucide-react';
import type { TabType, Playlist } from '../types';

interface LeftSidebarProps {
  currentTab: TabType;
  onTabChange: (tab: TabType) => void;
  searchQuery: string;
  onSearchChange: (q: string) => void;
  playlists: Playlist[];
  onPlaylistClick: (playlist: Playlist) => void;
  onCreatePlaylist: () => void;
  onDeletePlaylist: (id: string) => void;
}

export const LeftSidebar: React.FC<LeftSidebarProps> = ({
  currentTab,
  onTabChange,
  searchQuery,
  onSearchChange,
  playlists,
  onPlaylistClick,
  onCreatePlaylist,
  onDeletePlaylist,
}) => {
  return (
    <aside className="w-60 h-full bg-[#12141a] border-r border-white/5 flex flex-col p-4 select-none shrink-0 overflow-y-auto custom-scrollbar">
      {/* macOS Window Controls + Logo */}
      <div className="flex items-center gap-2 mb-6 px-1">
        <div className="w-3 h-3 rounded-full bg-[#ff5f56] shadow-sm hover:opacity-80 cursor-pointer" />
        <div className="w-3 h-3 rounded-full bg-[#ffbd2e] shadow-sm hover:opacity-80 cursor-pointer" />
        <div className="w-3 h-3 rounded-full bg-[#27c93f] shadow-sm hover:opacity-80 cursor-pointer" />
      </div>

      {/* Brand Title */}
      <div className="flex items-center gap-2.5 px-1 mb-6">
        <div className="w-7 h-7 rounded-lg bg-gradient-to-tr from-purple-600 to-pink-500 flex items-center justify-center shadow-lg shadow-purple-500/25">
          <Music2 className="w-4 h-4 text-white" />
        </div>
        <span className="font-extrabold text-lg tracking-wide text-white font-sans">
          Wavyn
        </span>
      </div>

      {/* Pill Search Box */}
      <div className="relative mb-6">
        <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
        <input
          type="text"
          placeholder="Search..."
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          className="w-full pl-9 pr-3 py-2 bg-white/5 hover:bg-white/10 focus:bg-white/10 rounded-full text-xs text-white placeholder-slate-400 border border-white/5 focus:border-purple-500 focus:outline-none transition-all"
        />
      </div>

      {/* SECTION: MENU */}
      <div className="space-y-1 mb-6">
        <span className="text-[11px] font-bold tracking-wider text-slate-400 uppercase px-3 mb-2 block">
          MENU
        </span>

        <button
          onClick={() => onTabChange('home')}
          className={`flex items-center justify-between w-full px-3 py-2 rounded-xl text-xs font-semibold transition-all relative ${
            currentTab === 'home'
              ? 'text-white bg-white/5 font-bold'
              : 'text-slate-400 hover:text-slate-200 hover:bg-white/5'
          }`}
        >
          <div className="flex items-center gap-3">
            <Home className={`w-4 h-4 ${currentTab === 'home' ? 'text-white' : 'text-slate-400'}`} />
            <span>Home</span>
          </div>
          {currentTab === 'home' && (
            <div className="w-1.5 h-4 rounded-full bg-[#10b981] shadow-sm shadow-[#10b981]/50" />
          )}
        </button>

        <button
          onClick={() => onTabChange('discover')}
          className={`flex items-center justify-between w-full px-3 py-2 rounded-xl text-xs font-semibold transition-all relative ${
            currentTab === 'discover'
              ? 'text-white bg-white/5 font-bold'
              : 'text-slate-400 hover:text-slate-200 hover:bg-white/5'
          }`}
        >
          <div className="flex items-center gap-3">
            <Compass className={`w-4 h-4 ${currentTab === 'discover' ? 'text-white' : 'text-slate-400'}`} />
            <span>Discover</span>
          </div>
          {currentTab === 'discover' && (
            <div className="w-1.5 h-4 rounded-full bg-[#10b981] shadow-sm shadow-[#10b981]/50" />
          )}
        </button>

        <button
          onClick={() => onTabChange('radio')}
          className={`flex items-center justify-between w-full px-3 py-2 rounded-xl text-xs font-semibold transition-all relative ${
            currentTab === 'radio'
              ? 'text-white bg-white/5 font-bold'
              : 'text-slate-400 hover:text-slate-200 hover:bg-white/5'
          }`}
        >
          <div className="flex items-center gap-3">
            <Radio className={`w-4 h-4 ${currentTab === 'radio' ? 'text-white' : 'text-slate-400'}`} />
            <span>Radio</span>
          </div>
          {currentTab === 'radio' && (
            <div className="w-1.5 h-4 rounded-full bg-[#10b981] shadow-sm shadow-[#10b981]/50" />
          )}
        </button>

        <button
          onClick={() => onTabChange('podcast')}
          className={`flex items-center justify-between w-full px-3 py-2 rounded-xl text-xs font-semibold transition-all relative ${
            currentTab === 'podcast'
              ? 'text-white bg-white/5 font-bold'
              : 'text-slate-400 hover:text-slate-200 hover:bg-white/5'
          }`}
        >
          <div className="flex items-center gap-3">
            <Mic className={`w-4 h-4 ${currentTab === 'podcast' ? 'text-white' : 'text-slate-400'}`} />
            <span>Podcast</span>
          </div>
          {currentTab === 'podcast' && (
            <div className="w-1.5 h-4 rounded-full bg-[#10b981] shadow-sm shadow-[#10b981]/50" />
          )}
        </button>
      </div>

      {/* SECTION: LIBRARY */}
      <div className="space-y-1 mb-6">
        <span className="text-[11px] font-bold tracking-wider text-slate-400 uppercase px-3 mb-2 block">
          LIBRARY
        </span>

        <button
          onClick={() => onTabChange('albums')}
          className={`flex items-center justify-between w-full px-3 py-2 rounded-xl text-xs font-semibold transition-all relative ${
            currentTab === 'albums'
              ? 'text-white bg-white/5 font-bold'
              : 'text-slate-400 hover:text-slate-200 hover:bg-white/5'
          }`}
        >
          <div className="flex items-center gap-3">
            <Disc className="w-4 h-4" />
            <span>Albums</span>
          </div>
          {currentTab === 'albums' && (
            <div className="w-1.5 h-4 rounded-full bg-[#10b981]" />
          )}
        </button>

        <button
          onClick={() => onTabChange('songs')}
          className={`flex items-center justify-between w-full px-3 py-2 rounded-xl text-xs font-semibold transition-all relative ${
            currentTab === 'songs'
              ? 'text-white bg-white/5 font-bold'
              : 'text-slate-400 hover:text-slate-200 hover:bg-white/5'
          }`}
        >
          <div className="flex items-center gap-3">
            <Music2 className="w-4 h-4" />
            <span>Song</span>
          </div>
          {currentTab === 'songs' && (
            <div className="w-1.5 h-4 rounded-full bg-[#10b981]" />
          )}
        </button>

        <button
          onClick={() => onTabChange('artists')}
          className={`flex items-center justify-between w-full px-3 py-2 rounded-xl text-xs font-semibold transition-all relative ${
            currentTab === 'artists'
              ? 'text-white bg-white/5 font-bold'
              : 'text-slate-400 hover:text-slate-200 hover:bg-white/5'
          }`}
        >
          <div className="flex items-center gap-3">
            <Users className="w-4 h-4" />
            <span>Artist</span>
          </div>
          {currentTab === 'artists' && (
            <div className="w-1.5 h-4 rounded-full bg-[#10b981]" />
          )}
        </button>
      </div>

      {/* SECTION: PLAYLIST */}
      <div className="space-y-1 flex-1">
        <div className="flex items-center justify-between px-3 mb-2">
          <span className="text-[11px] font-bold tracking-wider text-slate-400 uppercase">
            PLAYLIST
          </span>
          <button
            onClick={onCreatePlaylist}
            className="text-slate-400 hover:text-white p-0.5 rounded hover:bg-white/10"
            title="Create Playlist"
          >
            <Plus className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="space-y-0.5">
          {playlists.map((pl) => (
            <div
              key={pl.id}
              onClick={() => onPlaylistClick(pl)}
              className="flex items-center justify-between px-3 py-1.5 rounded-lg text-xs text-slate-400 hover:text-slate-200 hover:bg-white/5 transition-all group cursor-pointer"
            >
              <div className="flex items-center gap-2.5 truncate">
                <Music2 className="w-3.5 h-3.5 shrink-0 opacity-70 group-hover:text-purple-400" />
                <span className="truncate">{pl.title}</span>
              </div>
              {pl.isCustom && (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onDeletePlaylist(pl.id);
                  }}
                  className="opacity-0 group-hover:opacity-100 hover:text-rose-400 p-1 rounded"
                >
                  <Trash2 className="w-3 h-3" />
                </button>
              )}
            </div>
          ))}
        </div>
      </div>
    </aside>
  );
};
