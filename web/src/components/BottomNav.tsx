import React from 'react';
import { Home, Compass, Library, Settings } from 'lucide-react';
import { TabType } from '../types';

interface BottomNavProps {
  currentTab: TabType;
  onTabChange: (tab: TabType) => void;
}

export const BottomNav: React.FC<BottomNavProps> = ({ currentTab, onTabChange }) => {
  return (
    <nav className="md:hidden fixed bottom-0 left-0 right-0 h-16 bg-[#090b10]/95 backdrop-blur-2xl border-t border-white/10 flex items-center justify-around px-2 z-40">
      <button
        onClick={() => onTabChange('home')}
        className={`flex flex-col items-center gap-1 px-4 py-1.5 rounded-xl transition-all ${
          currentTab === 'home' ? 'text-blue-400 scale-105 font-bold' : 'text-slate-400 hover:text-slate-200'
        }`}
      >
        <Home className="w-5 h-5" />
        <span className="text-[11px]">Home</span>
      </button>

      <button
        onClick={() => onTabChange('search')}
        className={`flex flex-col items-center gap-1 px-4 py-1.5 rounded-xl transition-all ${
          currentTab === 'search' ? 'text-blue-400 scale-105 font-bold' : 'text-slate-400 hover:text-slate-200'
        }`}
      >
        <Compass className="w-5 h-5" />
        <span className="text-[11px]">Search</span>
      </button>

      <button
        onClick={() => onTabChange('library')}
        className={`flex flex-col items-center gap-1 px-4 py-1.5 rounded-xl transition-all ${
          currentTab === 'library' ? 'text-blue-400 scale-105 font-bold' : 'text-slate-400 hover:text-slate-200'
        }`}
      >
        <Library className="w-5 h-5" />
        <span className="text-[11px]">Library</span>
      </button>

      <button
        onClick={() => onTabChange('settings')}
        className={`flex flex-col items-center gap-1 px-4 py-1.5 rounded-xl transition-all ${
          currentTab === 'settings' ? 'text-blue-400 scale-105 font-bold' : 'text-slate-400 hover:text-slate-200'
        }`}
      >
        <Settings className="w-5 h-5" />
        <span className="text-[11px]">Settings</span>
      </button>
    </nav>
  );
};
