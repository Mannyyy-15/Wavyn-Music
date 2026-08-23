# Wavyn Music - Architecture Documentation

This document provides an accurate, comprehensive overview of Wavyn Music's architecture, design patterns, and technical implementation.

---

## Table of Contents

- [Overview](#overview)
- [Module Structure](#module-structure)
- [Technology Stack](#technology-stack)
- [Audio Streaming & Playback Architecture](#audio-streaming--playback-architecture)
- [Data Flow & State Management](#data-flow--state-management)
- [Key Subsystems](#key-subsystems)

---

## Overview

Wavyn Music is an open-source, Material Design YouTube Music and local audio player for Android. Built with 100% Kotlin and Jetpack Compose, it emphasizes fast performance, rich audio customization (Pro EQ, Spatial Audio, Loudness Normalization), offline caching/downloads, synchronized lyrics, Discord Rich Presence, and seamless casting.

### Core Principles
- **Clean Architecture & Separation of Concerns**: Clear demarcation between UI (Compose), State Management (Hilt ViewModels), Business Logic, and Data/Service Layers.
- **Dependency Injection**: Powered by Dagger Hilt across Application, Service, and ViewModel lifecycles.
- **Asynchronous Reactive Pipelines**: Kotlin Coroutines and StateFlow/SharedFlow for asynchronous events and state streams.
- **High-Performance Audio Engine**: Jetpack Media3 (ExoPlayer) paired with custom audio effect processing pipelines (Loudness Enhancer, Virtualizer, Bass Boost, Spatial Audio).

---

## Module Structure

The project is structured into targeted Gradle modules:

```
Wavyn Music/
├── app/                  # Main Android Application (iad1tya.echo.music)
│   ├── constants/        # Preference keys, audio quality enums, stream client constants
│   ├── db/               # Room Database (Entities, DAOs, Migrations)
│   ├── di/               # Dagger Hilt dependency injection modules
│   ├── lyrics/           # Lyrics providers (LrcLib, Kugou, YouTube, LyricsPlus)
│   ├── models/           # UI and domain data models
│   ├── playback/         # MusicService (MediaSessionService), ExoPlayer, DownloadUtil
│   ├── ui/               # Jetpack Compose screens, components, player, themes
│   ├── utils/            # YTPlayerUtils, StreamClientUtils, PoToken, SpatialAudio, AudioHaptics
│   └── viewmodels/       # Screen-level and feature ViewModels
├── innertube/            # YouTube Music Innertube API client & Extractor bridge
│   ├── models/           # YouTubeClient definitions, context schemas, response models
│   ├── pages/            # Parser and endpoint handlers (Search, Browse, Next, Player)
│   ├── NewPipe.kt        # NewPipe / Metrolist extractor integration & deobfuscation
│   └── YouTube.kt        # High-level YouTube Music facade
├── kizzy/                # Discord Rich Presence (RPC) gateway integration
├── kugou/                # Kugou synchronized lyrics client
├── lastfm/               # Last.fm scrobbling and authentication client
├── lrclib/               # LRCLIB synchronized lyrics API client
└── shazam.so/            # Native / JNI bindings for Shazam song recognition
```

---

## Technology Stack

- **UI Framework**: Jetpack Compose, Material 3, MaterialKolor (Dynamic Material You theming)
- **Dependency Injection**: Dagger Hilt (`hilt-android`, `hilt-navigation-compose`)
- **Audio Engine**: AndroidX Media3 (`media3-exoplayer`, `media3-session`, `media3-datasource-okhttp`, `media3-cast`)
- **Networking**: Ktor Client (OkHttp engine), OkHttp3, DNS-over-HTTPS (Cloudflare)
- **Local Persistence**: AndroidX Room with KSP, DataStore Preferences, SQLite
- **Stream Extraction**: Innertube API, MetrolistExtractor/NewPipeExtractor, BotGuard PoToken WebView
- **Image Loading**: Coil 3 (OkHttp network loader)
- **Lyrics Providers**: LrcLib, Kugou, YouTube Innertube, BetterLyrics

---

## Audio Streaming & Playback Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                       MusicService                          │
│               (Media3 MediaSessionService)                  │
└──────────────────────────────┬──────────────────────────────┘
                               │
                ResolvingDataSource.Factory
                               │
               ┌───────────────▼───────────────┐
               │        YTPlayerUtils          │
               │   (Stream Resolution Engine)  │
               └───────────────┬───────────────┘
                               │
       ┌───────────────────────┴───────────────────────┐
       │                                               │
┌──────▼──────┐                                ┌───────▼───────┐
│ MAIN_CLIENT │                                │ Fallback Chain │
│ (WEB_REMIX) │                                │ IOS, IPADOS,  │
│  (Metadata) │                                │ TVHTML5, etc. │
└─────────────┘                                └───────┬───────┘
                                                       │
                                            ┌──────────▼──────────┐
                                            │ NewPipe / Extractor │
                                            │ (Sig & N-Transform) │
                                            └──────────┬──────────┘
                                                       │
                                            ┌──────────▼──────────┐
                                            │   Stream Validation │
                                            │ (Range: bytes=0-0)  │
                                            └──────────┬──────────┘
                                                       │
                                            ┌──────────▼──────────┐
                                            │ ExoPlayer Playback  │
                                            └─────────────────────┘
```

1. **Resolution Pipeline**: `ResolvingDataSource` delegates playback URLs to `YTPlayerUtils.playerResponseForPlayback()`.
2. **Metadata & Playback Separation**: Fast metadata queries use `WEB_REMIX`, while audio stream extraction negotiates through a prioritized client fallback pipeline (`IOS`, `IPADOS`, `TVHTML5`, `ANDROID_CREATOR`, `ANDROID`, `ANDROID_VR`).
3. **Deobfuscation**: Signatures and CDN throttling parameters (`n` parameter) are resolved dynamically through `NewPipeUtils` / extractor JS player integration.
4. **Resilient Validation**: Stream URLs undergo non-destructive range validation (`Range: bytes=0-0`) with matched client headers and User-Agents (`StreamClientUtils`) to avoid false-negative 403 CDN errors.

---

## Data Flow & State Management

1. **Database & Cache**:
   - `MusicDatabase`: Tracks playlists, library songs, artists, albums, format details, and playback history.
   - `playerCache` / `downloadCache`: ExoPlayer `SimpleCache` instances handle progressive disk caching and offline audio playback.
2. **Preference Store**:
   - Centralized `DataStore` manages user preferences (Audio Quality, Preferred Stream Client, Equalizer profiles, UI layout, lyrics providers).
3. **Player State**:
   - `PlayerConnection`: Binds UI components to `MusicService` via `MediaController`, publishing reactive state flows for progress, playback state, queue, and current media metadata.
