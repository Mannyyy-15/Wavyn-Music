export interface Song {
  id: string;
  rank?: string;
  title: string;
  artist: string;
  artistId?: string;
  album?: string;
  albumId?: string;
  thumbnailUrl: string;
  duration: number; // in seconds
  streamUrl: string;
  lyrics?: string; // LRC formatted string
  genres?: string[];
  year?: string;
  likesCount?: string;
  isLiked?: boolean;
}

export interface Album {
  id: string;
  title: string;
  artist: string;
  artistId: string;
  thumbnailUrl: string;
  year: string;
  genre: string;
  songs: Song[];
}

export interface Artist {
  id: string;
  name: string;
  rank?: string;
  albumsCount: number;
  avatarUrl: string;
  bannerUrl?: string;
  bio?: string;
  followers?: string;
  monthlyListeners?: string;
  topSongs: Song[];
  albums: Album[];
}

export interface Playlist {
  id: string;
  title: string;
  description: string;
  thumbnailUrl: string;
  songs: Song[];
  isCustom?: boolean;
  author?: string;
  createdAt?: string;
}

export interface PodcastEpisode {
  id: string;
  title: string;
  showTitle: string;
  host: string;
  duration: number;
  thumbnailUrl: string;
  description: string;
  publishDate: string;
  streamUrl: string;
}

export interface LyricLine {
  time: number; // in seconds
  text: string;
}

export interface ToastMessage {
  id: string;
  text: string;
  type?: 'success' | 'info' | 'error';
}

export type RepeatMode = 'off' | 'all' | 'one';
export type VisualizerMode = 'bars' | 'wave' | 'circle' | 'off';
export type TabType =
  | 'home'
  | 'discover'
  | 'radio'
  | 'podcast'
  | 'albums'
  | 'songs'
  | 'artists'
  | 'search'
  | 'library'
  | 'playlist-detail'
  | 'artist-detail'
  | 'album-detail'
  | 'settings';
