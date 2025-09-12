# VPlay

A simple, modern Android media player using AndroidX Media3 with:

- Bottom navigation and a mini-player bar
- Video player with gestures (seek, volume, brightness, pinch zoom) and PiP
- Audio player screen with large artwork and basic controls
- Queue management with drag-and-drop reorder
- Media libraries (Videos, Music) with search, sort, and folder filter
- Playlists (Room), Online URL playback, and SAF picker
- Media-style notifications and headset/BT media button support

## Build

- AGP and dependency versions: see `gradle/libs.versions.toml`
- Kotlin: 2.0.21
- Min SDK 26 / Target 36

Open the project in Android Studio or build with Gradle.

## Run

- On first launch, you’ll see a brief onboarding; tap “Get started”
- Grant media permissions (READ_MEDIA_VIDEO/READ_MEDIA_AUDIO on 13+, or READ_EXTERNAL_STORAGE on older)
- Videos/Music tabs let you browse local media; search and sort are available
- Use the mini-player for quick controls; tap it to open the queue and drag to reorder
- Enter Picture-in-Picture by leaving the app during playback

## Architecture

- Media3 `ExoPlayer` and `MediaSession` in a foreground `PlaybackService`
- `PlayerManager` singleton for player lifecycle, queue ops, and persistence (queue and last position)
- Fragments + XML layouts for UI; RecyclerView for lists
- Room for playlists; Coil for thumbnails and artwork

## Troubleshooting

- Missing thumbnails/artwork? Ensure permissions are granted
- Android 13+: allow notifications/media access when prompted
- Unplugging headphones pauses playback by design (becoming-noisy)

## Community

Bug reports and suggestions are appreciated. Contact: t.me/vplaycommunity
