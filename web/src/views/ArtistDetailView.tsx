import React, { useState } from 'react';
import {
  Play,
  Heart,
  BadgeCheck,
  ArrowLeft,
  Users,
  Disc3,
  MoreHorizontal
} from 'lucide-react';
import type { Song, Album, Artist } from '../types';

interface ArtistDetailViewProps {
  artist: Artist;
  onBack: () => void;
  onPlaySong: (song: Song, queueList?: Song[]) => void;
  onSelectAlbum: (album: Album) => void;
  likedSongs: Song[];
  onToggleLike: (song: Song) => void;
}

export const ArtistDetailView: React.FC<ArtistDetailViewProps> = ({
  artist,
  onBack,
  onPlaySong,
  onSelectAlbum,
  likedSongs,
  onToggleLike,
}) => {
  const [isFollowing, setIsFollowing] = useState<boolean>(false);

  const formatDuration = (secs: number) => {
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  const isLiked = (songId: string) => likedSongs.some((s) => s.id === songId);

  return (
    <div className="space-y-8 pb-16 select-none">
      <button
        onClick={onBack}
        className="flex items-center gap-2 text-xs font-bold text-slate-400 hover:text-white transition-colors"
      >
        <ArrowLeft className="w-4 h-4" /> Back
      </button>

      {/* Giant Artist Hero Header Banner */}
      <div className="relative rounded-3xl overflow-hidden min-h-[300px] md:min-h-[380px] p-6 md:p-10 flex flex-col justify-end shadow-2xl border border-white/10 group">
        <img
          src={artist.bannerUrl || artist.avatarUrl}
          alt={artist.name}
          className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-1000"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-[#0f1118] via-[#0f1118]/60 to-transparent" />

        <div className="relative z-10 space-y-3">
          <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-500/20 text-blue-300 border border-blue-500/30 text-[11px] font-bold w-fit">
            <BadgeCheck className="w-4 h-4 text-blue-400 fill-blue-400 text-black" />
            <span>Verified Artist</span>
          </div>

          <h1 className="text-4xl md:text-6xl font-black text-white leading-tight drop-shadow-lg">
            {artist.name}
          </h1>

          <div className="flex items-center gap-4 text-xs font-semibold text-slate-300">
            <span className="flex items-center gap-1">
              <Users className="w-4 h-4 text-purple-400" />
              {artist.monthlyListeners || '65,000,000'} Monthly Listeners
            </span>
            <span>•</span>
            <span>{artist.followers || '40M'} Followers</span>
          </div>

          <div className="flex items-center gap-4 pt-2">
            {artist.topSongs.length > 0 && (
              <button
                onClick={() => onPlaySong(artist.topSongs[0], artist.topSongs)}
                className="flex items-center gap-2.5 px-8 py-3 rounded-full bg-purple-600 hover:bg-purple-500 text-white font-black text-xs shadow-xl shadow-purple-600/30 hover:scale-105 active:scale-95 transition-all"
              >
                <Play className="w-4 h-4 fill-current translate-x-0.5" />
                <span>Play All</span>
              </button>
            )}

            <button
              onClick={() => setIsFollowing((prev) => !prev)}
              className={`px-6 py-2.5 rounded-full font-bold text-xs border transition-all ${
                isFollowing
                  ? 'bg-white text-black border-white'
                  : 'bg-transparent text-white border-white/30 hover:border-white'
              }`}
            >
              {isFollowing ? 'Following' : 'Follow'}
            </button>
          </div>
        </div>
      </div>

      {/* Popular Tracks Section */}
      <section className="space-y-4">
        <h2 className="text-xl font-bold text-white">Popular Tracks</h2>

        <div className="space-y-1">
          {artist.topSongs.map((song, i) => {
            const liked = isLiked(song.id);
            return (
              <div
                key={song.id}
                onClick={() => onPlaySong(song, artist.topSongs)}
                className="flex items-center justify-between p-3 rounded-2xl hover:bg-white/5 transition-all cursor-pointer group"
              >
                <div className="flex items-center gap-4 truncate">
                  <span className="w-6 text-center text-xs font-mono font-bold text-slate-400 group-hover:text-purple-400">
                    {i + 1}
                  </span>
                  <div className="relative w-11 h-11 rounded-xl overflow-hidden shrink-0">
                    <img src={song.thumbnailUrl} alt={song.title} className="w-full h-full object-cover" />
                    <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                      <Play className="w-4 h-4 text-white fill-current translate-x-0.5" />
                    </div>
                  </div>
                  <div className="truncate">
                    <h4 className="text-xs font-extrabold text-white group-hover:text-purple-400 transition-colors truncate">
                      {song.title}
                    </h4>
                    <p className="text-[11px] text-slate-400 truncate">{song.album || artist.name}</p>
                  </div>
                </div>

                <div className="flex items-center gap-4">
                  <span className="text-xs font-mono text-slate-400">{formatDuration(song.duration)}</span>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      onToggleLike(song);
                    }}
                    className="p-1 rounded-full text-slate-400 hover:text-pink-500"
                  >
                    <Heart className={`w-3.5 h-3.5 ${liked ? 'fill-pink-500 text-pink-500' : ''}`} />
                  </button>
                  <button
                    onClick={(e) => e.stopPropagation()}
                    className="p-1 rounded-lg text-slate-400 hover:text-white opacity-0 group-hover:opacity-100 transition-opacity"
                  >
                    <MoreHorizontal className="w-4 h-4" />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      </section>

      {/* Discography Albums Section */}
      <section className="space-y-4">
        <div className="flex items-center gap-2">
          <Disc3 className="w-5 h-5 text-purple-400" />
          <h2 className="text-xl font-bold text-white">Discography</h2>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
          {artist.albums.map((album) => (
            <div
              key={album.id}
              onClick={() => onSelectAlbum(album)}
              className="group p-4 rounded-3xl bg-[#181a24] hover:bg-[#202330] border border-white/5 hover:border-purple-500/30 transition-all cursor-pointer shadow-xl flex flex-col"
            >
              <div className="relative aspect-square rounded-2xl overflow-hidden mb-3 shadow-lg">
                <img src={album.thumbnailUrl} alt={album.title} className="w-full h-full object-cover group-hover:scale-105 transition-transform" />
                <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                  <div className="w-11 h-11 rounded-full bg-white text-black flex items-center justify-center shadow-lg">
                    <Play className="w-5 h-5 fill-current translate-x-0.5" />
                  </div>
                </div>
              </div>
              <h3 className="font-bold text-sm text-white truncate">{album.title}</h3>
              <p className="text-xs text-slate-400 mt-0.5">{album.year} • {album.genre}</p>
            </div>
          ))}
        </div>
      </section>

      {/* About Section */}
      {artist.bio && (
        <section className="p-6 md:p-8 rounded-3xl bg-[#181a24] border border-white/5 space-y-2">
          <h3 className="text-base font-bold text-white">About {artist.name}</h3>
          <p className="text-sm text-slate-300 leading-relaxed max-w-2xl">{artist.bio}</p>
        </section>
      )}
    </div>
  );
};
