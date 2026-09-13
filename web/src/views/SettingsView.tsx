import React from 'react';
import { Sliders, Sparkles, Moon, Radio, Info } from 'lucide-react';
import { UserSettings, StorageService } from '../services/storage';

interface SettingsViewProps {
  settings: UserSettings;
  onUpdateSettings: (newSettings: UserSettings) => void;
}

export const SettingsView: React.FC<SettingsViewProps> = ({ settings, onUpdateSettings }) => {
  const handleQualityChange = (audioQuality: 'auto' | 'high' | 'lossless') => {
    const updated = { ...settings, audioQuality };
    onUpdateSettings(updated);
    StorageService.saveSettings(updated);
  };

  const handleEqualizerChange = (equalizer: 'flat' | 'bass_boost' | 'vocal' | 'electronic') => {
    const updated = { ...settings, equalizer };
    onUpdateSettings(updated);
    StorageService.saveSettings(updated);
  };

  const handlePureBlackToggle = () => {
    const updated = { ...settings, pureBlack: !settings.pureBlack };
    onUpdateSettings(updated);
    StorageService.saveSettings(updated);
  };

  const handleAutoPlayToggle = () => {
    const updated = { ...settings, autoPlaySimilar: !settings.autoPlaySimilar };
    onUpdateSettings(updated);
    StorageService.saveSettings(updated);
  };

  return (
    <div className="max-w-3xl space-y-8 pb-28">
      <div>
        <h1 className="text-2xl md:text-3xl font-extrabold text-white">Player Settings</h1>
        <p className="text-xs text-slate-400">Configure playback quality, audio equalizer, and visual theme</p>
      </div>

      {/* Audio Quality Section */}
      <div className="p-6 rounded-3xl bg-white/5 border border-white/10 backdrop-blur-xl space-y-4">
        <div className="flex items-center gap-3">
          <Sparkles className="w-5 h-5 text-blue-400" />
          <div>
            <h2 className="text-base font-bold text-white">Streaming Audio Quality</h2>
            <p className="text-xs text-slate-400">Higher quality provides richer depth and dynamic range</p>
          </div>
        </div>

        <div className="grid grid-cols-3 gap-3">
          {[
            { id: 'auto', name: 'Auto (160kbps)' },
            { id: 'high', name: 'High (320kbps)' },
            { id: 'lossless', name: 'Lossless Hi-Fi' }
          ].map((item) => (
            <button
              key={item.id}
              onClick={() => handleQualityChange(item.id as 'auto' | 'high' | 'lossless')}
              className={`p-3 rounded-2xl text-xs font-bold transition-all border ${
                settings.audioQuality === item.id
                  ? 'bg-blue-600/20 border-blue-500 text-blue-400 shadow-md shadow-blue-500/10'
                  : 'bg-white/5 border-transparent text-slate-300 hover:bg-white/10'
              }`}
            >
              {item.name}
            </button>
          ))}
        </div>
      </div>

      {/* Equalizer Presets */}
      <div className="p-6 rounded-3xl bg-white/5 border border-white/10 backdrop-blur-xl space-y-4">
        <div className="flex items-center gap-3">
          <Sliders className="w-5 h-5 text-purple-400" />
          <div>
            <h2 className="text-base font-bold text-white">Equalizer Sound Preset</h2>
            <p className="text-xs text-slate-400">Acoustic profile tuning for your headphones or speakers</p>
          </div>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          {[
            { id: 'flat', name: 'Flat (Studio)' },
            { id: 'bass_boost', name: 'Bass Boost' },
            { id: 'vocal', name: 'Vocal Boost' },
            { id: 'electronic', name: 'Electronic / Dance' }
          ].map((eq) => (
            <button
              key={eq.id}
              onClick={() => handleEqualizerChange(eq.id as 'flat' | 'bass_boost' | 'vocal' | 'electronic')}
              className={`p-3 rounded-2xl text-xs font-bold transition-all border ${
                settings.equalizer === eq.id
                  ? 'bg-purple-600/20 border-purple-500 text-purple-300 shadow-md shadow-purple-500/10'
                  : 'bg-white/5 border-transparent text-slate-300 hover:bg-white/10'
              }`}
            >
              {eq.name}
            </button>
          ))}
        </div>
      </div>

      {/* Visual & Playback Toggles */}
      <div className="p-6 rounded-3xl bg-white/5 border border-white/10 backdrop-blur-xl space-y-4">
        {/* AMOLED Pure Black Toggle */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Moon className="w-5 h-5 text-indigo-400" />
            <div>
              <h3 className="text-sm font-bold text-white">AMOLED Pure Black Theme</h3>
              <p className="text-xs text-slate-400">Pure pitch black background for OLED screens & battery savings</p>
            </div>
          </div>
          <button
            onClick={handlePureBlackToggle}
            className={`w-12 h-6 rounded-full transition-colors relative ${
              settings.pureBlack ? 'bg-blue-600' : 'bg-slate-700'
            }`}
          >
            <div
              className={`w-4 h-4 rounded-full bg-white transition-transform absolute top-1 ${
                settings.pureBlack ? 'left-7' : 'left-1'
              }`}
            />
          </button>
        </div>

        <div className="h-px bg-white/5" />

        {/* Continuous Autoplay Similar Toggle */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Radio className="w-5 h-5 text-emerald-400" />
            <div>
              <h3 className="text-sm font-bold text-white">Infinite Radio Autoplay</h3>
              <p className="text-xs text-slate-400">Continuously queue similar recommended tracks when queue ends</p>
            </div>
          </div>
          <button
            onClick={handleAutoPlayToggle}
            className={`w-12 h-6 rounded-full transition-colors relative ${
              settings.autoPlaySimilar ? 'bg-emerald-600' : 'bg-slate-700'
            }`}
          >
            <div
              className={`w-4 h-4 rounded-full bg-white transition-transform absolute top-1 ${
                settings.autoPlaySimilar ? 'left-7' : 'left-1'
              }`}
            />
          </button>
        </div>
      </div>

      {/* About Box */}
      <div className="p-6 rounded-3xl bg-white/5 border border-white/10 backdrop-blur-xl flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Info className="w-5 h-5 text-blue-400" />
          <div>
            <h4 className="text-sm font-bold text-white">Wavyn Music Web Player</h4>
            <p className="text-xs text-slate-400">Version 1.0.0 • Connected to Wavyn Core</p>
          </div>
        </div>
        <span className="text-xs font-mono text-emerald-400 bg-emerald-500/10 px-3 py-1 rounded-full border border-emerald-500/20">
          Online
        </span>
      </div>
    </div>
  );
};
