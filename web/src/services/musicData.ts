import type { Song, Album, Artist, Playlist, PodcastEpisode } from '../types';

export const ALL_SONGS: Song[] = [
  {
    id: 's1',
    rank: '01',
    title: 'Starboy (Remix)',
    artist: 'The Weeknd ft. Daft Punk',
    artistId: 'a1',
    album: 'Starboy',
    albumId: 'alb-starboy',
    thumbnailUrl: 'https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=600&auto=format&fit=crop&q=80',
    duration: 230,
    streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
    genres: ['Pop', 'Electronic', 'R&B'],
    year: '2023',
    likesCount: '1.4k',
    isLiked: true,
    lyrics: `[00:00.00] (Instrumental intro with synth bass)
[00:10.00] I'm tryna put you in the worst mood, ah
[00:13.50] P1 cleaner than your church shoes, ah
[00:17.00] Milli point two just to hurt you, ah
[00:20.50] All red Lamb' just to tease you, ah
[00:24.00] None of these toys on lease too, ah
[00:27.50] Made your whole year in a week too, yah
[00:31.00] Main bitch out of your league too, ah
[00:34.50] Side bitch out of your league too, ah
[00:38.00] Look what you've done
[00:41.00] I'm a motherfuckin' starboy
[00:45.00] Look what you've done
[00:48.00] I'm a motherfuckin' starboy
[00:52.00] Everyday a nigga try to test me, ah
[00:55.50] Every day a nigga try to end me, ah
[00:59.00] Pull up in a spaceship on them, ah
[01:03.00] Look what you've done
[01:06.00] I'm a motherfuckin' starboy`
  },
  {
    id: 's2',
    rank: '02',
    title: 'Blinding Lights',
    artist: 'The Weeknd',
    artistId: 'a1',
    album: 'After Hours',
    albumId: 'alb-after-hours',
    thumbnailUrl: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80',
    duration: 200,
    streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
    genres: ['Synthwave', 'Pop'],
    year: '2020',
    likesCount: '2.1k',
    isLiked: false,
    lyrics: `[00:00.00] (Synthwave intro kicking in)
[00:14.00] Yeah
[00:25.00] I've been tryna call
[00:28.00] I've been on my own for long enough
[00:32.50] Maybe you can show me how to love, maybe
[00:39.00] I'm going through withdrawals
[00:42.50] You don't even have to do too much
[00:47.00] You can turn me on with just a touch, baby
[00:54.00] I look around and Sin City's cold and empty
[00:59.00] No one's around to judge me
[01:03.00] I can't see clearly when you're gone
[01:08.00] I said, ooh, I'm blinded by the lights
[01:14.00] No, I can't sleep until I feel your touch`
  },
  {
    id: 's3',
    rank: '03',
    title: 'Midnight City',
    artist: 'M83',
    artistId: 'a4',
    album: 'Hurry Up, We\'re Dreaming',
    albumId: 'alb-m83',
    thumbnailUrl: 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80',
    duration: 243,
    streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3',
    genres: ['Electronic', 'Indie', 'Synthpop'],
    year: '2011',
    likesCount: '950',
    isLiked: true,
    lyrics: `[00:00.00] (Iconic synth riff echoes)
[00:20.00] Waiting in a car
[00:24.00] Waiting for a ride in the dark
[00:30.00] The night city grows
[00:35.00] Look and see her eyes, they glow
[00:44.00] Waiting in a car
[00:49.00] Waiting for a ride in the dark
[00:55.00] The city is my church
[01:01.00] It wraps in the blinding twilight
[01:10.00] The sky is screaming now
[01:16.00] Waiting in a car...`
  },
  {
    id: 's4',
    rank: '04',
    title: 'Levitating',
    artist: 'Dua Lipa',
    artistId: 'a2',
    album: 'Future Nostalgia',
    albumId: 'alb-future-nostalgia',
    thumbnailUrl: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80',
    duration: 203,
    streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3',
    genres: ['Nu-Disco', 'Pop'],
    year: '2020',
    likesCount: '1.8k',
    isLiked: false,
    lyrics: `[00:00.00] (Funky bass groove starts)
[00:08.00] If you wanna run away with me, I know a galaxy
[00:12.00] And I can take you for a ride
[00:15.50] I had a premonition that we fell into a rhythm
[00:20.00] Where the music don't stop for life
[00:23.50] Glitter in the sky, glitter in our eyes
[00:27.50] Shining just the way I like
[00:31.00] If you're feeling like you need a little bit of company
[00:35.50] You met me at the perfect time
[00:39.00] You want me, I want you, baby
[00:43.00] My sugarboo, I'm levitating`
  },
  {
    id: 's5',
    rank: '05',
    title: 'As It Was',
    artist: 'Harry Styles',
    artistId: 'a3',
    album: 'Harry\'s House',
    albumId: 'alb-harrys-house',
    thumbnailUrl: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80',
    duration: 167,
    streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3',
    genres: ['Indie Pop', 'Synthpop'],
    year: '2022',
    likesCount: '3.2k',
    isLiked: true,
    lyrics: `[00:00.00] "Come on Harry, we wanna say goodnight to you!"
[00:06.00] (Bouncy drum and 80s synth riff)
[00:14.00] Holdin' me back
[00:16.50] Gravity's holdin' me back
[00:19.50] I want you to hold out the palm of your hand
[00:23.00] Why don't we leave it at that?
[00:26.50] Nothin' to say
[00:29.50] When everything gets in the way
[00:33.00] Seems you cannot be replaced
[00:36.50] And I'm the one who will stay, oh-oh-oh
[00:40.50] You know it's not the same as it was
[00:45.00] In this world, it's just us
[00:48.00] You know it's not the same as it was`
  },
  {
    id: 's6',
    rank: '06',
    title: 'Nightcall (Drive Synth)',
    artist: 'Kavinsky',
    artistId: 'a4',
    album: 'OutRun',
    albumId: 'alb-outrun',
    thumbnailUrl: 'https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&auto=format&fit=crop&q=80',
    duration: 259,
    streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3',
    genres: ['Synthwave', 'Electro House', 'Chill'],
    year: '2010',
    likesCount: '890',
    isLiked: false,
    lyrics: `[00:00.00] (Heavy vocoder synth notes)
[00:24.00] I'm giving you a night call to tell you how I feel
[00:35.00] I want to drive you through the night, down the hills
[00:47.00] I'm gonna tell you something you don't want to hear
[00:58.00] I'm gonna show you where it's dark, but have no fear
[01:10.00] There's something inside you
[01:17.00] It's hard to explain
[01:23.00] They're talking about you, boy
[01:30.00] But you're still the same`
  },
  {
    id: 's7',
    rank: '07',
    title: 'Don\'t Start Now',
    artist: 'Dua Lipa',
    artistId: 'a2',
    album: 'Future Nostalgia',
    albumId: 'alb-future-nostalgia',
    thumbnailUrl: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80',
    duration: 183,
    streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3',
    genres: ['Disco', 'Pop'],
    year: '2020',
    likesCount: '2.4k',
    isLiked: true,
    lyrics: `[00:00.00] (Funky slap bass begins)
[00:09.00] Did a full 180, crazy
[00:13.00] Thinking 'bout the way I was
[00:18.00] Did the heartbreak change me? Maybe
[00:22.00] But look at where I ended up
[00:26.50] I'm all good already
[00:30.00] So moved on, it's scary
[00:35.00] I'm not where you left me at all`
  },
  {
    id: 's8',
    rank: '08',
    title: 'Save Your Tears',
    artist: 'The Weeknd',
    artistId: 'a1',
    album: 'After Hours',
    albumId: 'alb-after-hours',
    thumbnailUrl: 'https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=600&auto=format&fit=crop&q=80',
    duration: 215,
    streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3',
    genres: ['Synthpop', 'R&B'],
    year: '2020',
    likesCount: '3.1k',
    isLiked: true,
    lyrics: `[00:00.00] (Bright melodic synth chords)
[00:15.00] I saw you dancing in a crowded room
[00:22.00] You look so happy when I'm not with you
[00:30.00] But then you saw me, caught you by surprise
[00:38.00] A single teardrop falling from your eye
[00:46.00] I don't know why I run away
[00:54.00] Save your tears for another day`
  },
  {
    id: 's9',
    rank: '09',
    title: 'Watermelon Sugar',
    artist: 'Harry Styles',
    artistId: 'a3',
    album: 'Fine Line',
    albumId: 'alb-fine-line',
    thumbnailUrl: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80',
    duration: 174,
    streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3',
    genres: ['Pop Rock', 'Funk Pop'],
    year: '2019',
    likesCount: '2.9k',
    isLiked: false,
    lyrics: `[00:00.00] (Acoustic intro strumming)
[00:10.00] Tastes like strawberries on a summer evenin'
[00:18.00] And it sounds just like a song
[00:26.00] I want more berries and that summer feelin'
[00:34.00] It's so wonderful and warm
[00:42.00] Watermelon sugar high
[00:50.00] Watermelon sugar high`
  }
];

export const FEATURED_SONGS = ALL_SONGS;

export const ALL_ALBUMS: Album[] = [
  {
    id: 'alb-starboy',
    title: 'Starboy',
    artist: 'The Weeknd',
    artistId: 'a1',
    thumbnailUrl: 'https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=600&auto=format&fit=crop&q=80',
    year: '2023',
    genre: 'Pop / R&B',
    songs: [ALL_SONGS[0], ALL_SONGS[1], ALL_SONGS[7]]
  },
  {
    id: 'alb-after-hours',
    title: 'After Hours',
    artist: 'The Weeknd',
    artistId: 'a1',
    thumbnailUrl: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80',
    year: '2020',
    genre: 'Synthwave / Pop',
    songs: [ALL_SONGS[1], ALL_SONGS[7]]
  },
  {
    id: 'alb-future-nostalgia',
    title: 'Future Nostalgia',
    artist: 'Dua Lipa',
    artistId: 'a2',
    thumbnailUrl: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80',
    year: '2020',
    genre: 'Nu-Disco / Dance Pop',
    songs: [ALL_SONGS[3], ALL_SONGS[6]]
  },
  {
    id: 'alb-harrys-house',
    title: 'Harry\'s House',
    artist: 'Harry Styles',
    artistId: 'a3',
    thumbnailUrl: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80',
    year: '2022',
    genre: 'Indie Pop',
    songs: [ALL_SONGS[4], ALL_SONGS[8]]
  },
  {
    id: 'alb-outrun',
    title: 'OutRun',
    artist: 'Kavinsky',
    artistId: 'a4',
    thumbnailUrl: 'https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&auto=format&fit=crop&q=80',
    year: '2013',
    genre: 'Synthwave / Electro',
    songs: [ALL_SONGS[5], ALL_SONGS[2]]
  }
];

export const TOP_ARTISTS: Artist[] = [
  {
    id: 'a1',
    name: 'The Weeknd',
    rank: '01',
    albumsCount: 20,
    avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80',
    bannerUrl: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1200&auto=format&fit=crop&q=80',
    bio: 'Canadian singer, songwriter, and record producer known for his sonic versatility and dark lyricism.',
    followers: '98.4M',
    monthlyListeners: '108,421,902',
    topSongs: [ALL_SONGS[0], ALL_SONGS[1], ALL_SONGS[7]],
    albums: [ALL_ALBUMS[0], ALL_ALBUMS[1]]
  },
  {
    id: 'a2',
    name: 'Dua Lipa',
    rank: '02',
    albumsCount: 15,
    avatarUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop&q=80',
    bannerUrl: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=1200&auto=format&fit=crop&q=80',
    bio: 'Grammy-winning English pop superstar known for revitalizing nu-disco and dance pop.',
    followers: '76.2M',
    monthlyListeners: '74,105,400',
    topSongs: [ALL_SONGS[3], ALL_SONGS[6]],
    albums: [ALL_ALBUMS[2]]
  },
  {
    id: 'a3',
    name: 'Harry Styles',
    rank: '03',
    albumsCount: 10,
    avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80',
    bannerUrl: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=1200&auto=format&fit=crop&q=80',
    bio: 'English singer and songwriter with chart-topping global anthems across pop rock and indie pop.',
    followers: '55.8M',
    monthlyListeners: '62,810,120',
    topSongs: [ALL_SONGS[4], ALL_SONGS[8]],
    albums: [ALL_ALBUMS[3]]
  },
  {
    id: 'a4',
    name: 'Kavinsky & M83',
    rank: '04',
    albumsCount: 11,
    avatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80',
    bannerUrl: 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=1200&auto=format&fit=crop&q=80',
    bio: 'Pioneers of synthwave, French electro, and cinematic dream pop.',
    followers: '32.1M',
    monthlyListeners: '28,450,000',
    topSongs: [ALL_SONGS[2], ALL_SONGS[5]],
    albums: [ALL_ALBUMS[4]]
  }
];

export const FEATURED_PLAYLISTS: Playlist[] = [
  {
    id: 'pl-today-hits',
    title: 'Top Song Of The Week',
    description: 'The hottest trending tracks streamed worldwide right now.',
    thumbnailUrl: 'https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=600&auto=format&fit=crop&q=80',
    songs: ALL_SONGS,
    author: 'Wavyn Editorial',
    createdAt: 'Updated Today'
  },
  {
    id: 'pl-synthwave',
    title: 'Neon Nights & Synthwave',
    description: 'Retro-futuristic beats for late night drives and deep focus.',
    thumbnailUrl: 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80',
    songs: [ALL_SONGS[1], ALL_SONGS[2], ALL_SONGS[5]],
    author: 'Wavyn Editorial',
    createdAt: 'Curated Mix'
  },
  {
    id: 'pl-vibes',
    title: 'Global Top 50',
    description: 'Daily updated chart of the top 50 songs worldwide.',
    thumbnailUrl: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80',
    songs: ALL_SONGS,
    author: 'Wavyn Charts',
    createdAt: 'Daily Chart'
  },
  {
    id: 'pl-dance',
    title: 'Club Euphoria & Dance',
    description: 'Electrifying beats, disco grooves, and festival hits.',
    thumbnailUrl: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80',
    songs: [ALL_SONGS[0], ALL_SONGS[3], ALL_SONGS[6]],
    author: 'Wavyn Editorial',
    createdAt: 'Weekly Mix'
  }
];

export const PODCAST_EPISODES: PodcastEpisode[] = [
  {
    id: 'pod-1',
    title: 'How The Weeknd Recreated the 80s Synthwave Era',
    showTitle: 'Billboard Track Breakdown',
    host: 'Alex Rivera',
    duration: 1420,
    thumbnailUrl: 'https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=500&auto=format&fit=crop&q=80',
    description: 'An in-depth analysis of analog synthesizers and production techniques behind After Hours & Starboy.',
    publishDate: 'Aug 22, 2026',
    streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3'
  },
  {
    id: 'pod-2',
    title: 'The Evolution of Nu-Disco & Future Nostalgia',
    showTitle: 'Behind The Synthesizers',
    host: 'Elena Rostova',
    duration: 1850,
    thumbnailUrl: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop&q=80',
    description: 'How modern pop borrowed funky bass lines from 1970s studio sessions to make chart-topping hits.',
    publishDate: 'Aug 20, 2026',
    streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3'
  },
  {
    id: 'pod-3',
    title: 'The Art of Mixing Audio for Headphones vs Loudspeakers',
    showTitle: 'Audio Engineering Deep Dive',
    host: 'Dave Miller',
    duration: 2100,
    thumbnailUrl: 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&auto=format&fit=crop&q=80',
    description: 'Binaural panning, spatial audio algorithms, and EQ mastering secrets from top studio engineers.',
    publishDate: 'Aug 17, 2026',
    streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3'
  }
];

export const GENRE_CATEGORIES = [
  { id: 'pop', name: 'Pop', color: 'linear-gradient(135deg, #FF416C, #FF4B2B)', image: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400&q=80' },
  { id: 'electronic', name: 'Electronic', color: 'linear-gradient(135deg, #8A2387, #E94057, #F27121)', image: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&q=80' },
  { id: 'synthwave', name: 'Synthwave', color: 'linear-gradient(135deg, #654ea3, #eaafc8)', image: 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=400&q=80' },
  { id: 'hiphop', name: 'Hip-Hop', color: 'linear-gradient(135deg, #11998e, #38ef7d)', image: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400&q=80' },
  { id: 'rock', name: 'Rock & Indie', color: 'linear-gradient(135deg, #F00000, #DC281E)', image: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&q=80' },
  { id: 'chill', name: 'Lo-Fi & Chill', color: 'linear-gradient(135deg, #2193b0, #6dd5ed)', image: 'https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400&q=80' },
];
