import { Song, Playlist } from '../types';

const LIKED_KEY = 'wavyn_liked_songs';
const PLAYLISTS_KEY = 'wavyn_custom_playlists';
const RECENT_KEY = 'wavyn_recently_played';
const SETTINGS_KEY = 'wavyn_user_settings';

export interface UserSettings {
  pureBlack: boolean;
  audioQuality: 'auto' | 'high' | 'lossless';
  equalizer: 'flat' | 'bass_boost' | 'vocal' | 'electronic';
  autoPlaySimilar: boolean;
}

export const StorageService = {
  getLikedSongs(): Song[] {
    try {
      const data = localStorage.getItem(LIKED_KEY);
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  },

  toggleLike(song: Song): boolean {
    const likes = this.getLikedSongs();
    const index = likes.findIndex((s) => s.id === song.id);
    let isLiked = false;
    if (index >= 0) {
      likes.splice(index, 1);
      isLiked = false;
    } else {
      likes.unshift({ ...song, isLiked: true });
      isLiked = true;
    }
    localStorage.setItem(LIKED_KEY, JSON.stringify(likes));
    return isLiked;
  },

  isSongLiked(songId: string): boolean {
    const likes = this.getLikedSongs();
    return likes.some((s) => s.id === songId);
  },

  getCustomPlaylists(): Playlist[] {
    try {
      const data = localStorage.getItem(PLAYLISTS_KEY);
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  },

  createPlaylist(title: string, description: string = ''): Playlist {
    const playlists = this.getCustomPlaylists();
    const newPlaylist: Playlist = {
      id: `custom_${Date.now()}`,
      title,
      description,
      thumbnailUrl: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80',
      songs: [],
      isCustom: true
    };
    playlists.unshift(newPlaylist);
    localStorage.setItem(PLAYLISTS_KEY, JSON.stringify(playlists));
    return newPlaylist;
  },

  addSongToPlaylist(playlistId: string, song: Song) {
    const playlists = this.getCustomPlaylists();
    const target = playlists.find((p) => p.id === playlistId);
    if (target && !target.songs.some((s) => s.id === song.id)) {
      target.songs.push(song);
      localStorage.setItem(PLAYLISTS_KEY, JSON.stringify(playlists));
    }
  },

  getRecentlyPlayed(): Song[] {
    try {
      const data = localStorage.getItem(RECENT_KEY);
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  },

  addRecentlyPlayed(song: Song) {
    const recents = this.getRecentlyPlayed().filter((s) => s.id !== song.id);
    recents.unshift(song);
    localStorage.setItem(RECENT_KEY, JSON.stringify(recents.slice(0, 30)));
  },

  getSettings(): UserSettings {
    try {
      const data = localStorage.getItem(SETTINGS_KEY);
      return data ? JSON.parse(data) : {
        pureBlack: true,
        audioQuality: 'lossless',
        equalizer: 'bass_boost',
        autoPlaySimilar: true
      };
    } catch {
      return {
        pureBlack: true,
        audioQuality: 'lossless',
        equalizer: 'bass_boost',
        autoPlaySimilar: true
      };
    }
  },

  saveSettings(settings: UserSettings) {
    localStorage.setItem(SETTINGS_KEY, JSON.stringify(settings));
  }
};
