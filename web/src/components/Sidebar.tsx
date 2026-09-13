import React from 'react';
import { Home, Compass, Library, Settings, Disc3, Plus, Heart } from 'lucide-react';
import { TabType, Playlist } from '../types';

interface SidebarProps {
  currentTab: TabType;
  onTabChange: (tab: TabType) => void;
  playlists: Playlist[];
  onPlaylistClick: (playlist: Playlist) => void;
  onCreatePlaylist: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  currentTab,
  onTabChange,
  playlists,
  onPlaylistClick,
  onCreatePlaylist,
}) => {
  return (
    <aside className="hidden md:flex flex-col w-64 h-full bg-[#0d0f17]/80 backdrop-blur-2xl border-r border-white/5 p-4 select-none shrink-0 z-30">
      {/* Brand Logo */}
      <div className="flex items-center gap-3 px-3 py-4 mb-4">
        <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-blue-600 via-indigo-500 to-purple-600 flex items-center justify-center shadow-lg shadow-blue-500/25 ring-1 ring-white/20">
          <Disc3 className="w-6 h-6 text-white animate-spin-slow" />
        </div>
        <div>
          <h1 className="font-extrabold text-xl tracking-wide bg-gradient-to-r from-white via-slate-200 to-blue-400 bg-clip-text text-transparent">
            WAVYN
          </h1>
          <span className="text-[10px] tracking-widest text-blue-400/80 font-bold uppercase">Web Player</span>
        </div>
      </div>

      {/* Main Nav Items */}
      <nav className="space-y-1.5 mb-6">
        <button
          onClick={() => onTabChange('home')}
          className={`flex items-center gap-3.5 w-full px-4 py-3 rounded-xl font-semibold text-sm transition-all duration-200 ${
            currentTab === 'home'
              ? 'bg-blue-600/20 text-blue-400 border border-blue-500/30 shadow-md shadow-blue-500/10'
              : 'text-slate-400 hover:text-white hover:bg-white/5'
          }`}
        >
          <Home className="w-5 h-5" />
          <span>Home</span>
        </button>

        <button
          onClick={() => onTabChange('search')}
          className={`flex items-center gap-3.5 w-full px-4 py-3 rounded-xl font-semibold text-sm transition-all duration-200 ${
            currentTab === 'search'
              ? 'bg-blue-600/20 text-blue-400 border border-blue-500/30 shadow-md shadow-blue-500/10'
              : 'text-slate-400 hover:text-white hover:bg-white/5'
          }`}
        >
          <Compass className="w-5 h-5" />
          <span>Search & Explore</span>
        </button>

        <button
          onClick={() => onTabChange('library')}
          className={`flex items-center gap-3.5 w-full px-4 py-3 rounded-xl font-semibold text-sm transition-all duration-200 ${
            currentTab === 'library'
              ? 'bg-blue-600/20 text-blue-400 border border-blue-500/30 shadow-md shadow-blue-500/10'
              : 'text-slate-400 hover:text-white hover:bg-white/5'
          }`}
        >
          <Library className="w-5 h-5" />
          <span>Your Library</span>
        </button>

        <button
          onClick={() => onTabChange('settings')}
          className={`flex items-center gap-3.5 w-full px-4 py-3 rounded-xl font-semibold text-sm transition-all duration-200 ${
            currentTab === 'settings'
              ? 'bg-blue-600/20 text-blue-400 border border-blue-500/30 shadow-md shadow-blue-500/10'
              : 'text-slate-400 hover:text-white hover:bg-white/5'
          }`}
        >
          <Settings className="w-5 h-5" />
          <span>Settings</span>
        </button>
      </nav>

      {/* Playlists Header */}
      <div className="flex items-center justify-between px-3 mb-2 text-xs font-bold uppercase tracking-wider text-slate-400">
        <span>Playlists</span>
        <button
          onClick={onCreatePlaylist}
          className="p-1 rounded-lg hover:bg-white/10 text-slate-400 hover:text-white transition-colors"
          title="Create Playlist"
        >
          <Plus className="w-4 h-4" />
        </button>
      </div>

      {/* Quick Liked Songs */}
      <button
        onClick={() => onTabChange('library')}
        className="flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-white/5 text-slate-300 hover:text-white transition-colors mb-2 group"
      >
        <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-purple-600 to-blue-600 flex items-center justify-center text-white shadow-md shadow-purple-500/20">
          <Heart className="w-4 h-4 fill-white" />
        </div>
        <span className="text-sm font-medium">Liked Songs</span>
      </button>

      {/* Playlists Scroll Area */}
      <div className="flex-1 overflow-y-auto space-y-1 pr-1 custom-scrollbar">
        {playlists.map((playlist) => (
          <button
            key={playlist.id}
            onClick={() => onPlaylistClick(playlist)}
            className="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-left text-sm text-slate-400 hover:text-slate-200 hover:bg-white/5 transition-all truncate"
          >
            <img
              src={playlist.thumbnailUrl}
              alt={playlist.title}
              className="w-7 h-7 rounded-md object-cover ring-1 ring-white/10 shrink-0"
            />
            <span className="truncate">{playlist.title}</span>
          </button>
        ))}
      </div>
    </aside>
  );
};
