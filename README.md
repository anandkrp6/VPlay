# VPlay

A simple, modern Android media player using AndroidX Media3 with:

- Bottom navigation and a mini-player bar
- Video player with gestures (seek, volume, brightness, pinch zoom) and PiP
- Audio player screen with large artwork and basic controls
- Queue management with drag-and-drop reorder
- Media libraries (Videos, Music) with search, sort, and folder filter
- Playlists (Room), Online URL playback, and SAF picker
- Media-style notifications and headset/BT media button support
- Downloads manager (enqueue from Online, manage in-app)

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
- Online tab: paste a direct media URL to play or download; tap "Manage Downloads" to view status

## Downloads

VPlay uses the system DownloadManager to fetch online media:

- From Online: tap "Download" to enqueue; the system shows a notification on completion.
- Manage Downloads: in Online (or Options), open the Downloads screen to see progress, open completed files, or cancel in-flight tasks.
- Files are saved in the public Downloads directory.

## Architecture

- Media3 `ExoPlayer` and `MediaSession` in a foreground `PlaybackService`
- `PlayerManager` singleton for player lifecycle, queue ops, and persistence (queue and last position)
- Fragments + XML layouts for UI; RecyclerView for lists
- Room for playlists; Coil for thumbnails and artwork

## Roadmap (short)

- Subtitles: selection dialog, styling, and offset controls
- Download details: error messages, clear-completed, and swipe-to-remove
- Music: crossfade, gapless toggle, and lyrics view
- Settings hub: player defaults and permission states

## Troubleshooting

- Missing thumbnails/artwork? Ensure permissions are granted
- Android 13+: allow notifications/media access when prompted
- Unplugging headphones pauses playback by design (becoming-noisy)

## Community

Bug reports and suggestions are appreciated. Contact: t.me/vplaycommunity
