import type { Song, LyricLine, RepeatMode } from '../types';

declare global {
  interface Window {
    YT?: any;
    onYouTubeIframeAPIReady?: () => void;
  }
}

export class AudioEngine {
  private static instance: AudioEngine;
  private audio: HTMLAudioElement;
  private ytPlayer: any = null;
  private isYtReady: boolean = false;
  private useYouTube: boolean = false;
  private timerInterval: any = null;

  public currentSong: Song | null = null;
  public isPlaying: boolean = false;
  public currentTime: number = 0;
  public duration: number = 0;
  public volume: number = 0.9;
  public repeatMode: RepeatMode = 'off';
  public isShuffle: boolean = false;

  private listeners: Set<() => void> = new Set();

  private constructor() {
    this.audio = new Audio();
    this.audio.volume = this.volume;
    this.audio.preload = 'auto';

    this.audio.addEventListener('timeupdate', () => {
      if (!this.useYouTube) {
        this.currentTime = this.audio.currentTime;
        this.notifyListeners();
      }
    });

    this.audio.addEventListener('loadedmetadata', () => {
      if (!this.useYouTube) {
        this.duration = this.audio.duration || (this.currentSong?.duration || 0);
        this.notifyListeners();
      }
    });

    this.audio.addEventListener('ended', () => {
      if (!this.useYouTube) {
        this.handleTrackEnded();
      }
    });

    this.audio.addEventListener('play', () => {
      if (!this.useYouTube) {
        this.isPlaying = true;
        this.notifyListeners();
      }
    });

    this.audio.addEventListener('pause', () => {
      if (!this.useYouTube) {
        this.isPlaying = false;
        this.notifyListeners();
      }
    });

    // Initialize YouTube IFrame Player
    this.initYouTubePlayer();
  }

  public static getInstance(): AudioEngine {
    if (!AudioEngine.instance) {
      AudioEngine.instance = new AudioEngine();
    }
    return AudioEngine.instance;
  }

  private initYouTubePlayer() {
    const checkYT = () => {
      if (window.YT && window.YT.Player) {
        try {
          this.ytPlayer = new window.YT.Player('youtube-player', {
            height: '1',
            width: '1',
            videoId: 'kJQP7kiw5Fk',
            playerVars: {
              autoplay: 0,
              controls: 0,
              disablekb: 1,
              fs: 0,
              rel: 0,
              origin: window.location.origin,
            },
            events: {
              onReady: () => {
                this.isYtReady = true;
                if (this.ytPlayer.setVolume) {
                  this.ytPlayer.setVolume(this.volume * 100);
                }
              },
              onStateChange: (event: any) => {
                // YT.PlayerState.PLAYING = 1, PAUSED = 2, ENDED = 0
                if (event.data === 1) {
                  this.isPlaying = true;
                  this.startProgressPolling();
                  this.notifyListeners();
                } else if (event.data === 2) {
                  this.isPlaying = false;
                  this.stopProgressPolling();
                  this.notifyListeners();
                } else if (event.data === 0) {
                  this.handleTrackEnded();
                }
              },
            },
          });
        } catch (e) {
          console.warn('YT Player init failed:', e);
        }
      } else {
        setTimeout(checkYT, 200);
      }
    };

    if (document.readyState === 'complete') {
      checkYT();
    } else {
      window.addEventListener('load', checkYT);
    }
  }

  private startProgressPolling() {
    this.stopProgressPolling();
    this.timerInterval = setInterval(() => {
      if (this.useYouTube && this.ytPlayer && this.ytPlayer.getCurrentTime) {
        const time = this.ytPlayer.getCurrentTime();
        const dur = this.ytPlayer.getDuration();
        if (typeof time === 'number' && !isNaN(time)) {
          this.currentTime = time;
        }
        if (typeof dur === 'number' && !isNaN(dur) && dur > 0) {
          this.duration = dur;
        }
        this.notifyListeners();
      }
    }, 250);
  }

  private stopProgressPolling() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
  }

  private handleTrackEnded() {
    if (this.repeatMode === 'one') {
      this.seek(0);
      this.play();
    } else {
      this.isPlaying = false;
      this.notifyListeners();
    }
  }

  public subscribe(listener: () => void) {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }

  private notifyListeners() {
    this.listeners.forEach((fn) => fn());
  }

  public getFrequencyData(): Uint8Array {
    if (this.isPlaying) {
      const arr = new Uint8Array(32);
      const time = performance.now() * 0.006;
      for (let i = 0; i < 32; i++) {
        const val = Math.sin(time + i * 0.35) * 65 + Math.cos(time * 1.6 + i * 0.22) * 55 + 105;
        arr[i] = Math.max(10, Math.min(255, Math.floor(val)));
      }
      return arr;
    }
    return new Uint8Array(32);
  }

  // Load a song (extracts or uses YouTube videoId / full stream)
  public async loadSong(song: Song, autoPlay = true) {
    this.currentSong = song;
    this.currentTime = 0;
    this.duration = song.duration || 200;

    // Check if song has a YouTube videoId or YouTube URL
    const videoId = this.extractVideoId(song);

    if (videoId && this.isYtReady && this.ytPlayer && this.ytPlayer.loadVideoById) {
      this.useYouTube = true;
      this.audio.pause();
      try {
        if (autoPlay) {
          this.ytPlayer.loadVideoById(videoId);
          this.isPlaying = true;
        } else {
          this.ytPlayer.cueVideoById(videoId);
          this.isPlaying = false;
        }
      } catch (err) {
        console.warn('YT loadVideoById fallback to audio element:', err);
        this.loadAudioFallback(song, autoPlay);
      }
    } else {
      this.loadAudioFallback(song, autoPlay);
    }

    this.notifyListeners();
  }

  private extractVideoId(song: Song): string | null {
    if ((song as any).videoId) return (song as any).videoId;

    // Known full official tracks map
    const knownMap: Record<string, string> = {
      'Tak Mampu Pergi': 'xS3aC07hL4g',
      'Kaulah Segalanya': '4kF_7qM7mD8',
      'Lagu Rindu': '41xS3c0k7hY',
      'Starboy': '34Na4j8AVgA',
      'Blinding Lights': '4NRXx6U8ABQ',
      'Levitating': 'TUVcZfQe-Kw',
      'As It Was': 'H5v3k2nn9Pc',
      'Nightcall': 'MV_3Dpw-BRY',
      'Save Your Tears': 'XXYlFuWEuKi',
      'Don\'t Start Now': 'oygrmJFKYZY',
      'Watermelon Sugar': 'E07s5ZYyg4m',
    };

    for (const [key, vid] of Object.entries(knownMap)) {
      if (song.title.toLowerCase().includes(key.toLowerCase())) {
        return vid;
      }
    }

    return null;
  }

  private async loadAudioFallback(song: Song, autoPlay: boolean) {
    this.useYouTube = false;
    this.audio.src = song.streamUrl;
    this.audio.currentTime = 0;
    if (autoPlay) {
      try {
        await this.audio.play();
        this.isPlaying = true;
      } catch (err) {
        console.warn('Audio play error:', err);
        this.isPlaying = false;
      }
    }
  }

  public async play() {
    if (this.useYouTube && this.ytPlayer && this.ytPlayer.playVideo) {
      try {
        this.ytPlayer.playVideo();
        this.isPlaying = true;
      } catch (e) {
        console.warn('YT play error:', e);
      }
    } else {
      try {
        await this.audio.play();
        this.isPlaying = true;
      } catch (e) {
        console.warn('Audio play error:', e);
      }
    }
    this.notifyListeners();
  }

  public pause() {
    if (this.useYouTube && this.ytPlayer && this.ytPlayer.pauseVideo) {
      try {
        this.ytPlayer.pauseVideo();
        this.isPlaying = false;
      } catch (e) {
        console.warn('YT pause error:', e);
      }
    } else {
      this.audio.pause();
      this.isPlaying = false;
    }
    this.notifyListeners();
  }

  public togglePlay() {
    if (this.isPlaying) {
      this.pause();
    } else {
      this.play();
    }
  }

  public seek(seconds: number) {
    if (!isNaN(seconds) && isFinite(seconds)) {
      this.currentTime = seconds;
      if (this.useYouTube && this.ytPlayer && this.ytPlayer.seekTo) {
        try {
          this.ytPlayer.seekTo(seconds, true);
        } catch (e) {
          console.warn('YT seek error:', e);
        }
      } else {
        this.audio.currentTime = seconds;
      }
      this.notifyListeners();
    }
  }

  public setVolume(vol: number) {
    this.volume = Math.max(0, Math.min(1, vol));
    this.audio.volume = this.volume;
    if (this.ytPlayer && this.ytPlayer.setVolume) {
      try {
        this.ytPlayer.setVolume(this.volume * 100);
      } catch (e) {
        console.warn('YT setVolume error:', e);
      }
    }
    this.notifyListeners();
  }

  public parseLrcLyrics(lrcText?: string): LyricLine[] {
    if (!lrcText) return [];
    const lines = lrcText.split('\n');
    const result: LyricLine[] = [];
    const timeRegex = /\[(\d{2}):(\d{2})(?:\.(\d{2,3}))?\]/;

    for (const line of lines) {
      const match = timeRegex.exec(line);
      if (match) {
        const minutes = parseInt(match[1], 10);
        const seconds = parseInt(match[2], 10);
        const millis = match[3] ? parseInt(match[3].padEnd(3, '0').slice(0, 3), 10) : 0;
        const time = minutes * 60 + seconds + millis / 1000;
        const text = line.replace(timeRegex, '').trim();
        if (text) {
          result.push({ time, text });
        }
      }
    }
    return result.sort((a, b) => a.time - b.time);
  }
}
