import type { Song, Album, Artist, Playlist } from '../types';

export class LiveMusicService {
  private static instance: LiveMusicService;

  public static getInstance(): LiveMusicService {
    if (!LiveMusicService.instance) {
      LiveMusicService.instance = new LiveMusicService();
    }
    return LiveMusicService.instance;
  }

  // Format high-res artwork from Apple CDN (change 100x100 to 600x600)
  private getHighResArtwork(url?: string): string {
    if (!url) return 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80';
    return url.replace('100x100bb', '600x600bb').replace('60x60bb', '600x600bb');
  }

  // Real Global Live Search API
  public async search(query: string, limit = 20): Promise<{ songs: Song[]; artists: Artist[]; albums: Album[] }> {
    if (!query.trim()) {
      return { songs: [], artists: [], albums: [] };
    }

    try {
      const url = `https://itunes.apple.com/search?term=${encodeURIComponent(
        query.trim()
      )}&entity=song&limit=${limit}`;
      const res = await fetch(url);
      const data = await res.json();

      const rawResults = data.results || [];

      const songs: Song[] = rawResults.map((item: any, index: number) => ({
        id: `itunes_${item.trackId || index}`,
        rank: (index + 1).toString().padStart(2, '0'),
        title: item.trackName || 'Unknown Title',
        artist: item.artistName || 'Unknown Artist',
        artistId: item.artistId ? `art_${item.artistId}` : undefined,
        album: item.collectionName || item.trackName,
        albumId: item.collectionId ? `alb_${item.collectionId}` : undefined,
        thumbnailUrl: this.getHighResArtwork(item.artworkUrl100),
        duration: item.trackTimeMillis ? Math.floor(item.trackTimeMillis / 1000) : 180,
        streamUrl: item.previewUrl || '',
        genres: [item.primaryGenreName || 'Pop'],
        year: item.releaseDate ? new Date(item.releaseDate).getFullYear().toString() : '2024',
        likesCount: `${(Math.floor(Math.random() * 8) + 1)}.${Math.floor(Math.random() * 9)}k`,
        isLiked: false,
      }));

      // Extract unique artists
      const artistMap = new Map<string, Artist>();
      rawResults.forEach((item: any) => {
        if (item.artistName && !artistMap.has(item.artistName)) {
          artistMap.set(item.artistName, {
            id: `art_${item.artistId || item.artistName}`,
            name: item.artistName,
            albumsCount: Math.floor(Math.random() * 15) + 5,
            avatarUrl: this.getHighResArtwork(item.artworkUrl100),
            bannerUrl: this.getHighResArtwork(item.artworkUrl100),
            bio: `${item.artistName} is a globally renowned music artist with chart-topping tracks in ${item.primaryGenreName || 'Pop'}.`,
            followers: `${Math.floor(Math.random() * 80) + 10}M`,
            monthlyListeners: `${Math.floor(Math.random() * 50) + 20},${Math.floor(Math.random() * 900) + 100},000`,
            topSongs: songs.filter((s) => s.artist === item.artistName),
            albums: [],
          });
        }
      });

      // Extract unique albums
      const albumMap = new Map<string, Album>();
      rawResults.forEach((item: any) => {
        if (item.collectionName && !albumMap.has(item.collectionName)) {
          albumMap.set(item.collectionName, {
            id: `alb_${item.collectionId || item.collectionName}`,
            title: item.collectionName,
            artist: item.artistName || 'Various Artists',
            artistId: `art_${item.artistId || item.artistName}`,
            thumbnailUrl: this.getHighResArtwork(item.artworkUrl100),
            year: item.releaseDate ? new Date(item.releaseDate).getFullYear().toString() : '2024',
            genre: item.primaryGenreName || 'Pop',
            songs: songs.filter((s) => s.album === item.collectionName),
          });
        }
      });

      return {
        songs: songs.filter((s) => Boolean(s.streamUrl)),
        artists: Array.from(artistMap.values()),
        albums: Array.from(albumMap.values()),
      };
    } catch (e) {
      console.error('Live search error:', e);
      return { songs: [], artists: [], albums: [] };
    }
  }

  // Fetch real synchronized lyrics from LRCLIB
  public async fetchLyrics(trackTitle: string, artistName: string): Promise<string> {
    try {
      const cleanTitle = trackTitle.replace(/\([^)]*\)/g, '').trim();
      const cleanArtist = artistName.split(/ft\.|feat\.|&|,/i)[0].trim();

      const url = `https://lrclib.net/api/get?track_name=${encodeURIComponent(
        cleanTitle
      )}&artist_name=${encodeURIComponent(cleanArtist)}`;

      const res = await fetch(url);
      if (!res.ok) return '';
      const data = await res.json();
      return data.syncedLyrics || data.plainLyrics || '';
    } catch (e) {
      console.warn('Lyrics fetch failed:', e);
      return '';
    }
  }

  // Load real Initial Global Hits for Home and Top Trending
  public async getTopTrendingTracks(): Promise<Song[]> {
    try {
      // Query top real global hits (The Weeknd, Sammy Simorangkir, Dua Lipa, Harry Styles, Taylor Swift, Coldplay)
      const queries = [
        'Tak Mampu Pergi Sammy Simorangkir',
        'Kaulah Segalanya Sammy Simorangkir',
        'Lagu Rindu Kerispatih',
        'Starboy The Weeknd',
        'Blinding Lights The Weeknd',
        'Levitating Dua Lipa',
        'As It Was Harry Styles',
        'Nightcall Kavinsky',
        'Save Your Tears The Weeknd',
        'Don\'t Start Now Dua Lipa',
        'Watermelon Sugar Harry Styles'
      ];

      const songPromises = queries.map(async (q, index) => {
        try {
          const res = await fetch(`https://itunes.apple.com/search?term=${encodeURIComponent(q)}&entity=song&limit=1`);
          const data = await res.json();
          const item = data.results?.[0];
          if (item) {
            return {
              id: `real_track_${item.trackId}`,
              rank: (index + 1).toString().padStart(2, '0'),
              title: item.trackName,
              artist: item.artistName,
              artistId: `art_${item.artistId}`,
              album: item.collectionName || item.trackName,
              albumId: `alb_${item.collectionId}`,
              thumbnailUrl: this.getHighResArtwork(item.artworkUrl100),
              duration: item.trackTimeMillis ? Math.floor(item.trackTimeMillis / 1000) : 180,
              streamUrl: item.previewUrl,
              genres: [item.primaryGenreName || 'Pop'],
              year: item.releaseDate ? new Date(item.releaseDate).getFullYear().toString() : '2024',
              likesCount: `${Math.floor(Math.random() * 4) + 1}.${Math.floor(Math.random() * 9)}k`,
              isLiked: index === 0 || index === 2,
            } as Song;
          }
          return null;
        } catch {
          return null;
        }
      });

      const results = await Promise.all(songPromises);
      return results.filter((s): s is Song => Boolean(s && s.streamUrl));
    } catch (e) {
      console.error('getTopTrendingTracks error:', e);
      return [];
    }
  }
}
