package com.bytecoder.vplay.backend.managers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bytecoder.vplay.backend.managers.AnalyticsManager
import com.bytecoder.vplay.backend.data.database.VPlayDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

// Data Classes for Music Library
@Entity(tableName = "music_tracks")
data class MusicTrack(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String? = null,
    val composer: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val duration: Long, // in milliseconds
    val filePath: String,
    val fileSize: Long,
    val bitrate: Int? = null,
    val sampleRate: Int? = null,
    val channels: Int? = null,
    val format: String? = null,
    val albumArtPath: String? = null,
    val playCount: Int = 0,
    val lastPlayed: Long? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val rating: Float = 0.0f, // 0.0 to 5.0
    val resumePosition: Long = 0, // in milliseconds
    @ColumnInfo(name = "lyrics_path") val lyricsPath: String? = null
)

@Entity(tableName = "music_albums")
data class MusicAlbum(
    @PrimaryKey val id: String,
    val name: String,
    val artist: String,
    val albumArtist: String? = null,
    val year: Int? = null,
    val genre: String? = null,
    val trackCount: Int = 0,
    val totalDuration: Long = 0,
    val albumArtPath: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val playCount: Int = 0,
    val lastPlayed: Long? = null,
    val isFavorite: Boolean = false,
    val rating: Float = 0.0f
)

@Entity(tableName = "music_artists")
data class MusicArtist(
    @PrimaryKey val id: String,
    val name: String,
    val albumCount: Int = 0,
    val trackCount: Int = 0,
    val totalDuration: Long = 0,
    val artistImagePath: String? = null,
    val genres: String? = null, // Comma-separated genres
    val dateAdded: Long = System.currentTimeMillis(),
    val playCount: Int = 0,
    val lastPlayed: Long? = null,
    val isFavorite: Boolean = false,
    val biography: String? = null
)

@Entity(tableName = "music_genres")
data class MusicGenre(
    @PrimaryKey val id: String,
    val name: String,
    val trackCount: Int = 0,
    val artistCount: Int = 0,
    val albumCount: Int = 0,
    val totalDuration: Long = 0,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "music_playlists")
data class MusicPlaylist(
    @PrimaryKey val id: String,
    val name: String,
    val description: String? = null,
    val trackCount: Int = 0,
    val totalDuration: Long = 0,
    val coverArtPath: String? = null,
    val dateCreated: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis(),
    val playCount: Int = 0,
    val lastPlayed: Long? = null,
    val isSmartPlaylist: Boolean = false,
    val smartCriteria: String? = null, // JSON criteria for smart playlists
    val isFavorite: Boolean = false
)

@Entity(tableName = "music_playlist_tracks")
data class MusicPlaylistTrack(
    @PrimaryKey val id: String,
    val playlistId: String,
    val trackId: String,
    val position: Int,
    val dateAdded: Long = System.currentTimeMillis()
)

data class MusicLibraryStats(
    val totalTracks: Int,
    val totalAlbums: Int,
    val totalArtists: Int,
    val totalGenres: Int,
    val totalDuration: Long,
    val totalSize: Long,
    val favoriteTracks: Int,
    val favoriteAlbums: Int,
    val favoriteArtists: Int,
    val playedTracks: Int,
    val averageRating: Float,
    val mostPlayedTracks: List<MusicTrack>,
    val recentlyAddedTracks: List<MusicTrack>,
    val topArtists: List<MusicArtist>,
    val topGenres: List<MusicGenre>
)

enum class MusicSortOrder {
    TITLE_ASC,
    TITLE_DESC,
    ARTIST_ASC,
    ARTIST_DESC,
    ALBUM_ASC,
    ALBUM_DESC,
    YEAR_ASC,
    YEAR_DESC,
    DURATION_ASC,
    DURATION_DESC,
    DATE_ADDED_DESC,
    DATE_ADDED_ASC,
    PLAY_COUNT_DESC,
    RATING_DESC,
    RECENTLY_PLAYED,
    SHUFFLE
}

enum class MusicViewMode {
    LIST,
    GRID,
    ALBUM_GRID,
    ARTIST_GRID,
    GENRE_LIST,
    PLAYLIST_VIEW,
    NOW_PLAYING_QUEUE
}

enum class MusicGroupBy {
    NONE,
    ARTIST,
    ALBUM,
    GENRE,
    YEAR,
    FOLDER
}

class MusicLibraryManager(private val context: Context) {
    
    private val database = VPlayDatabase.getDatabase(context)
    // Analytics is handled by the singleton object
    
    // Music Library DAOs
    private val trackDao = database.musicTrackDao()
    private val albumDao = database.musicAlbumDao()
    private val artistDao = database.musicArtistDao()
    private val genreDao = database.musicGenreDao()
    private val playlistDao = database.musicPlaylistDao()
    private val playlistTrackDao = database.musicPlaylistTrackDao()
    
    // Core Library Operations
    
    suspend fun getAllTracks(): List<MusicTrack> = withContext(Dispatchers.IO) {
        try {
            val dbTracks = trackDao.getAllTracks()
            if (dbTracks.isEmpty()) {
                // If no tracks in database, scan from MediaStore and save them
                val scannedTracks = scanMusicFromMediaStore()
                if (scannedTracks.isNotEmpty()) {
                    trackDao.insertTracks(scannedTracks)
                    // Also generate and save albums, artists, genres
                    refreshDerivedData(scannedTracks)
                }
                scannedTracks
            } else {
                dbTracks
            }
        } catch (e: Exception) {
            AnalyticsManager.trackError("music_library_load_failed", e.message)
            emptyList()
        }
    }
    
    suspend fun getAllAlbums(): List<MusicAlbum> = withContext(Dispatchers.IO) {
        try {
            val dbAlbums = albumDao.getAllAlbums()
            if (dbAlbums.isEmpty()) {
                // Generate albums from tracks if not present
                val generatedAlbums = generateAlbumsFromTracks()
                if (generatedAlbums.isNotEmpty()) {
                    albumDao.insertAlbums(generatedAlbums)
                }
                generatedAlbums
            } else {
                dbAlbums
            }
        } catch (e: Exception) {
            AnalyticsManager.trackError("music_albums_load_failed", e.message)
            emptyList()
        }
    }
    
    suspend fun getAllArtists(): List<MusicArtist> = withContext(Dispatchers.IO) {
        try {
            val dbArtists = artistDao.getAllArtists()
            if (dbArtists.isEmpty()) {
                // Generate artists from tracks if not present
                val generatedArtists = generateArtistsFromTracks()
                if (generatedArtists.isNotEmpty()) {
                    artistDao.insertArtists(generatedArtists)
                }
                generatedArtists
            } else {
                dbArtists
            }
        } catch (e: Exception) {
            AnalyticsManager.trackError("music_artists_load_failed", e.message)
            emptyList()
        }
    }
    
    suspend fun getAllGenres(): List<MusicGenre> = withContext(Dispatchers.IO) {
        try {
            val dbGenres = genreDao.getAllGenres()
            if (dbGenres.isEmpty()) {
                // Generate genres from tracks if not present
                val generatedGenres = generateGenresFromTracks()
                if (generatedGenres.isNotEmpty()) {
                    genreDao.insertGenres(generatedGenres)
                }
                generatedGenres
            } else {
                dbGenres
            }
        } catch (e: Exception) {
            AnalyticsManager.trackError("music_genres_load_failed", e.message)
            emptyList()
        }
    }
    
    suspend fun getAllPlaylists(): List<MusicPlaylist> = withContext(Dispatchers.IO) {
        try {
            playlistDao.getAllPlaylists()
        } catch (e: Exception) {
            AnalyticsManager.trackError("music_playlists_load_failed", e.message)
            emptyList()
        }
    }
    
    // Search and Filter Operations
    
    suspend fun searchTracks(
        query: String,
        sortOrder: MusicSortOrder = MusicSortOrder.TITLE_ASC,
        groupBy: MusicGroupBy = MusicGroupBy.NONE,
        favoritesOnly: Boolean = false,
        genre: String? = null,
        artist: String? = null,
        album: String? = null,
        year: Int? = null
    ): List<MusicTrack> = withContext(Dispatchers.IO) {
        try {
            // Use DAO search for basic query
            var filteredTracks = if (query.isBlank()) {
                trackDao.getAllTracks()
            } else {
                trackDao.searchTracks(query)
            }
            
            // Apply additional filters
            if (favoritesOnly) {
                filteredTracks = filteredTracks.filter { it.isFavorite }
            }
            
            if (genre != null) {
                filteredTracks = filteredTracks.filter { it.genre == genre }
            }
            
            if (artist != null) {
                filteredTracks = filteredTracks.filter { it.artist == artist }
            }
            
            if (album != null) {
                filteredTracks = filteredTracks.filter { it.album == album }
            }
            
            if (year != null) {
                filteredTracks = filteredTracks.filter { it.year == year }
            }
            
            // Apply sorting
            filteredTracks = when (sortOrder) {
                MusicSortOrder.TITLE_ASC -> filteredTracks.sortedBy { it.title }
                MusicSortOrder.TITLE_DESC -> filteredTracks.sortedByDescending { it.title }
                MusicSortOrder.ARTIST_ASC -> filteredTracks.sortedBy { it.artist }
                MusicSortOrder.ARTIST_DESC -> filteredTracks.sortedByDescending { it.artist }
                MusicSortOrder.ALBUM_ASC -> filteredTracks.sortedBy { it.album }
                MusicSortOrder.ALBUM_DESC -> filteredTracks.sortedByDescending { it.album }
                MusicSortOrder.YEAR_ASC -> filteredTracks.sortedBy { it.year ?: 0 }
                MusicSortOrder.YEAR_DESC -> filteredTracks.sortedByDescending { it.year ?: 0 }
                MusicSortOrder.DURATION_ASC -> filteredTracks.sortedBy { it.duration }
                MusicSortOrder.DURATION_DESC -> filteredTracks.sortedByDescending { it.duration }
                MusicSortOrder.DATE_ADDED_DESC -> filteredTracks.sortedByDescending { it.dateAdded }
                MusicSortOrder.DATE_ADDED_ASC -> filteredTracks.sortedBy { it.dateAdded }
                MusicSortOrder.PLAY_COUNT_DESC -> filteredTracks.sortedByDescending { it.playCount }
                MusicSortOrder.RATING_DESC -> filteredTracks.sortedByDescending { it.rating }
                MusicSortOrder.RECENTLY_PLAYED -> filteredTracks.sortedByDescending { it.lastPlayed ?: 0 }
                MusicSortOrder.SHUFFLE -> filteredTracks.shuffled()
            }
            
            AnalyticsManager.trackFeatureUsage("music_search", mapOf(
                "query" to query,
                "result_count" to filteredTracks.size.toString(),
                "sort_order" to sortOrder.name,
                "group_by" to groupBy.name
            ))
            
            filteredTracks
            
        } catch (e: Exception) {
            AnalyticsManager.trackError("music_search_failed", e.message)
            emptyList()
        }
    }
    
    suspend fun getTracksByAlbum(albumId: String): List<MusicTrack> = withContext(Dispatchers.IO) {
        try {
            trackDao.getTracksByAlbum(albumId)
        } catch (e: Exception) {
            AnalyticsManager.trackError("tracks_by_album_failed", e.message)
            emptyList()
        }
    }
    
    suspend fun getTracksByArtist(artistName: String): List<MusicTrack> = withContext(Dispatchers.IO) {
        try {
            trackDao.getTracksByArtist(artistName)
        } catch (e: Exception) {
            AnalyticsManager.trackError("tracks_by_artist_failed", e.message)
            emptyList()
        }
    }
    
    suspend fun getTracksByGenre(genreName: String): List<MusicTrack> = withContext(Dispatchers.IO) {
        try {
            trackDao.getTracksByGenre(genreName)
        } catch (e: Exception) {
            AnalyticsManager.trackError("tracks_by_genre_failed", e.message)
            emptyList()
        }
    }
    
    // Recently Played and Favorites
    
    suspend fun getRecentlyPlayedTracks(limit: Int = 50): List<MusicTrack> = withContext(Dispatchers.IO) {
        try {
            trackDao.getRecentlyPlayedTracks(limit)
        } catch (e: Exception) {
            AnalyticsManager.trackError("recently_played_failed", e.message)
            emptyList()
        }
    }
    
    suspend fun getRecentlyAddedTracks(limit: Int = 50): List<MusicTrack> = withContext(Dispatchers.IO) {
        try {
            trackDao.getRecentlyAddedTracks(limit)
        } catch (e: Exception) {
            AnalyticsManager.trackError("recently_added_failed", e.message)
            emptyList()
        }
    }
    
    suspend fun getFavoriteTracks(): List<MusicTrack> = withContext(Dispatchers.IO) {
        try {
            trackDao.getFavoriteTracks()
        } catch (e: Exception) {
            AnalyticsManager.trackError("favorite_tracks_failed", e.message)
            emptyList()
        }
    }
    
    suspend fun getFavoriteAlbums(): List<MusicAlbum> = withContext(Dispatchers.IO) {
        try {
            albumDao.getFavoriteAlbums()
        } catch (e: Exception) {
            AnalyticsManager.trackError("favorite_albums_failed", e.message)
            emptyList()
        }
    }
    
    suspend fun getFavoriteArtists(): List<MusicArtist> = withContext(Dispatchers.IO) {
        try {
            artistDao.getFavoriteArtists()
        } catch (e: Exception) {
            AnalyticsManager.trackError("favorite_artists_failed", e.message)
            emptyList()
        }
    }
    
    // Playlist Management
    
    suspend fun createPlaylist(
        name: String,
        description: String? = null,
        isSmartPlaylist: Boolean = false,
        smartCriteria: String? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            val playlistId = UUID.randomUUID().toString()
            val playlist = MusicPlaylist(
                id = playlistId,
                name = name,
                description = description,
                isSmartPlaylist = isSmartPlaylist,
                smartCriteria = smartCriteria
            )
            
            playlistDao.insertPlaylist(playlist)
            
            AnalyticsManager.trackFeatureUsage("playlist_created", mapOf(
                "playlist_name" to name,
                "is_smart" to isSmartPlaylist.toString()
            ))
            
            playlistId
        } catch (e: Exception) {
            AnalyticsManager.trackError("playlist_creation_failed", e.message)
            ""
        }
    }
    
    suspend fun addTrackToPlaylist(playlistId: String, trackId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentTracks = playlistTrackDao.getPlaylistTracks(playlistId)
            val maxPosition = playlistTrackDao.getMaxPosition(playlistId) ?: -1
            val newPosition = maxPosition + 1
            
            val playlistTrack = MusicPlaylistTrack(
                id = UUID.randomUUID().toString(),
                playlistId = playlistId,
                trackId = trackId,
                position = newPosition
            )
            
            playlistTrackDao.insertPlaylistTrack(playlistTrack)
            
            // Update playlist stats
            val newCount = currentTracks.size + 1
            val totalDuration = calculatePlaylistDuration(playlistId)
            playlistDao.updatePlaylistStats(playlistId, newCount, totalDuration)
            
            AnalyticsManager.trackFeatureUsage("track_added_to_playlist", mapOf(
                "playlist_id" to playlistId,
                "track_id" to trackId
            ))
            
            true
        } catch (e: Exception) {
            AnalyticsManager.trackError("add_track_to_playlist_failed", e.message)
            false
        }
    }
    
    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            playlistTrackDao.removeTrackFromPlaylist(playlistId, trackId)
            
            // Update playlist stats
            val currentTracks = playlistTrackDao.getPlaylistTracks(playlistId)
            val newCount = currentTracks.size
            val totalDuration = calculatePlaylistDuration(playlistId)
            playlistDao.updatePlaylistStats(playlistId, newCount, totalDuration)
            
            AnalyticsManager.trackFeatureUsage("track_removed_from_playlist", mapOf(
                "playlist_id" to playlistId,
                "track_id" to trackId
            ))
            
            true
        } catch (e: Exception) {
            AnalyticsManager.trackError("remove_track_from_playlist_failed", e.message)
            false
        }
    }
    
    suspend fun getPlaylistTracks(playlistId: String): List<MusicTrack> = withContext(Dispatchers.IO) {
        try {
            val playlistTracks = playlistTrackDao.getPlaylistTracks(playlistId)
            val trackIds = playlistTracks.map { it.trackId }
            val tracks = mutableListOf<MusicTrack>()
            
            for (trackId in trackIds) {
                trackDao.getTrack(trackId)?.let { tracks.add(it) }
            }
            
            tracks
        } catch (e: Exception) {
            AnalyticsManager.trackError("playlist_tracks_failed", e.message)
            emptyList()
        }
    }
    
    suspend fun updatePlaylist(
        playlistId: String,
        name: String,
        description: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val existingPlaylist = playlistDao.getPlaylist(playlistId)
            if (existingPlaylist != null) {
                val updatedPlaylist = existingPlaylist.copy(
                    name = name,
                    description = description
                )
                playlistDao.updatePlaylist(updatedPlaylist)
                // Log playlist update (using trackError since trackEvent is private)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            AnalyticsManager.trackError("update_playlist_failed", e.message)
            false
        }
    }
    
    suspend fun deletePlaylist(playlistId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // First remove all tracks from the playlist
            playlistTrackDao.clearPlaylist(playlistId)
            
            // Then get the playlist and delete it
            val playlist = playlistDao.getPlaylist(playlistId)
            if (playlist != null) {
                playlistDao.deletePlaylist(playlist)
            }
            
            true
        } catch (e: Exception) {
            AnalyticsManager.trackError("delete_playlist_failed", e.message)
            false
        }
    }
    
    // Smart Playlist Operations
    
    suspend fun generateSmartPlaylist(criteria: String): List<MusicTrack> = withContext(Dispatchers.IO) {
        try {
            when (criteria.lowercase()) {
                "recently_played" -> trackDao.getRecentlyPlayedTracks(25)
                "top_rated" -> trackDao.getTopRatedTracks(4.0f, 50)
                "never_played" -> trackDao.getNeverPlayedTracks(100)
                "favorites" -> trackDao.getFavoriteTracks()
                else -> {
                    // Parse more complex criteria like "genre:Rock AND year:>2000 AND rating:>3"
                    parseSmartPlaylistCriteria(criteria)
                }
            }
        } catch (e: Exception) {
            AnalyticsManager.trackError("smart_playlist_generation_failed", e.message)
            emptyList()
        }
    }
    
    private suspend fun parseSmartPlaylistCriteria(criteria: String): List<MusicTrack> {
        try {
            val allTracks = trackDao.getAllTracks()
            
            // Simple criteria parser - can be enhanced for more complex queries
            var filteredTracks = allTracks
            
            val conditions = criteria.split(" AND ")
            for (condition in conditions) {
                val parts = condition.split(":")
                if (parts.size == 2) {
                    val field = parts[0].trim().lowercase()
                    val value = parts[1].trim()
                    
                    filteredTracks = when (field) {
                        "genre" -> filteredTracks.filter { it.genre?.equals(value, ignoreCase = true) == true }
                        "artist" -> filteredTracks.filter { it.artist.contains(value, ignoreCase = true) }
                        "album" -> filteredTracks.filter { it.album.contains(value, ignoreCase = true) }
                        "year" -> {
                            if (value.startsWith(">")) {
                                val yearThreshold = value.substring(1).toIntOrNull() ?: 0
                                filteredTracks.filter { (it.year ?: 0) > yearThreshold }
                            } else if (value.startsWith("<")) {
                                val yearThreshold = value.substring(1).toIntOrNull() ?: 9999
                                filteredTracks.filter { (it.year ?: 9999) < yearThreshold }
                            } else {
                                val exactYear = value.toIntOrNull()
                                filteredTracks.filter { it.year == exactYear }
                            }
                        }
                        "rating" -> {
                            if (value.startsWith(">")) {
                                val ratingThreshold = value.substring(1).toFloatOrNull() ?: 0f
                                filteredTracks.filter { it.rating > ratingThreshold }
                            } else if (value.startsWith("<")) {
                                val ratingThreshold = value.substring(1).toFloatOrNull() ?: 5f
                                filteredTracks.filter { it.rating < ratingThreshold }
                            } else {
                                val exactRating = value.toFloatOrNull()
                                filteredTracks.filter { it.rating == exactRating }
                            }
                        }
                        "playcount" -> {
                            if (value.startsWith(">")) {
                                val countThreshold = value.substring(1).toIntOrNull() ?: 0
                                filteredTracks.filter { it.playCount > countThreshold }
                            } else {
                                val exactCount = value.toIntOrNull() ?: 0
                                filteredTracks.filter { it.playCount == exactCount }
                            }
                        }
                        else -> filteredTracks
                    }
                }
            }
            
            return filteredTracks
        } catch (e: Exception) {
            AnalyticsManager.trackError("smart_playlist_criteria_parsing_failed", e.message)
            return emptyList()
        }
    }
    
    // Album Art and Metadata Management
    
    suspend fun extractAndSaveAlbumArt(trackPath: String): String? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(trackPath)
            
            val artBytes = retriever.embeddedPicture
            retriever.release()
            
            if (artBytes != null) {
                val bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
                val albumArtDir = File(context.filesDir, "album_art")
                if (!albumArtDir.exists()) albumArtDir.mkdirs()
                
                val artFile = File(albumArtDir, "${UUID.randomUUID()}.jpg")
                val outputStream = FileOutputStream(artFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                outputStream.close()
                
                artFile.absolutePath
            } else null
            
        } catch (e: Exception) {
            AnalyticsManager.trackError("album_art_extraction_failed", e.message)
            null
        }
    }
    
    suspend fun updateTrackMetadata(
        trackId: String,
        updates: Map<String, Any>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val track = trackDao.getTrack(trackId) ?: return@withContext false
            
            // Apply updates to create new track instance
            val updatedTrack = track.copy(
                title = updates["title"] as? String ?: track.title,
                artist = updates["artist"] as? String ?: track.artist,
                album = updates["album"] as? String ?: track.album,
                albumArtist = updates["albumArtist"] as? String ?: track.albumArtist,
                composer = updates["composer"] as? String ?: track.composer,
                genre = updates["genre"] as? String ?: track.genre,
                year = updates["year"] as? Int ?: track.year,
                trackNumber = updates["trackNumber"] as? Int ?: track.trackNumber,
                discNumber = updates["discNumber"] as? Int ?: track.discNumber,
                rating = updates["rating"] as? Float ?: track.rating,
                isFavorite = updates["isFavorite"] as? Boolean ?: track.isFavorite,
                lyricsPath = updates["lyricsPath"] as? String ?: track.lyricsPath,
                dateModified = System.currentTimeMillis()
            )
            
            trackDao.updateTrack(updatedTrack)
            
            AnalyticsManager.trackFeatureUsage("track_metadata_updated", mapOf(
                "track_id" to trackId,
                "updated_fields" to updates.keys.joinToString(",")
            ))
            
            true
        } catch (e: Exception) {
            AnalyticsManager.trackError("track_metadata_update_failed", e.message)
            false
        }
    }
    
    // Playback State Management
    
    suspend fun updatePlayCount(trackId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            trackDao.incrementPlayCount(trackId)
            true
        } catch (e: Exception) {
            AnalyticsManager.trackError("play_count_update_failed", e.message)
            false
        }
    }
    
    suspend fun updateLastPlayed(trackId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            trackDao.incrementPlayCount(trackId) // This updates both playCount and lastPlayed
            true
        } catch (e: Exception) {
            AnalyticsManager.trackError("last_played_update_failed", e.message)
            false
        }
    }
    
    suspend fun updateResumePosition(trackId: String, position: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            trackDao.updateResumePosition(trackId, position)
            true
        } catch (e: Exception) {
            AnalyticsManager.trackError("resume_position_update_failed", e.message)
            false
        }
    }
    
    suspend fun toggleFavorite(trackId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val track = trackDao.getTrack(trackId) ?: return@withContext false
            val newFavoriteStatus = !track.isFavorite
            trackDao.updateFavoriteStatus(trackId, newFavoriteStatus)
            
            AnalyticsManager.trackFeatureUsage("track_favorite_toggled", mapOf(
                "track_id" to trackId,
                "new_status" to newFavoriteStatus.toString()
            ))
            
            true
        } catch (e: Exception) {
            AnalyticsManager.trackError("favorite_toggle_failed", e.message)
            false
        }
    }
    
    suspend fun setRating(trackId: String, rating: Float): Boolean = withContext(Dispatchers.IO) {
        try {
            trackDao.updateRating(trackId, rating)
            
            AnalyticsManager.trackFeatureUsage("track_rating_set", mapOf(
                "track_id" to trackId,
                "rating" to rating.toString()
            ))
            
            true
        } catch (e: Exception) {
            AnalyticsManager.trackError("rating_set_failed", e.message)
            false
        }
    }
    
    // Library Statistics
    
    suspend fun getLibraryStatistics(): MusicLibraryStats = withContext(Dispatchers.IO) {
        try {
            val trackCount = trackDao.getTrackCount()
            val albumCount = albumDao.getAlbumCount()
            val artistCount = artistDao.getArtistCount()
            val genreCount = genreDao.getGenreCount()
            val totalDuration = trackDao.getTotalDuration()
            val totalSize = trackDao.getTotalSize()
            val favoriteTrackCount = trackDao.getFavoriteTrackCount()
            val favoriteAlbumCount = albumDao.getFavoriteAlbumCount()
            val favoriteArtistCount = artistDao.getFavoriteArtistCount()
            val playedTrackCount = trackDao.getPlayedTrackCount()
            val averageRating = trackDao.getAverageRating() ?: 0f
            val mostPlayedTracks = trackDao.getRecentlyPlayedTracks(10)
            val recentlyAddedTracks = trackDao.getRecentlyAddedTracks(10)
            val topArtists = artistDao.getTopArtists(10)
            val topGenres = genreDao.getTopGenres(10)
            
            MusicLibraryStats(
                totalTracks = trackCount,
                totalAlbums = albumCount,
                totalArtists = artistCount,
                totalGenres = genreCount,
                totalDuration = totalDuration,
                totalSize = totalSize,
                favoriteTracks = favoriteTrackCount,
                favoriteAlbums = favoriteAlbumCount,
                favoriteArtists = favoriteArtistCount,
                playedTracks = playedTrackCount,
                averageRating = averageRating,
                mostPlayedTracks = mostPlayedTracks,
                recentlyAddedTracks = recentlyAddedTracks,
                topArtists = topArtists,
                topGenres = topGenres
            )
        } catch (e: Exception) {
            AnalyticsManager.trackError("music_statistics_failed", e.message)
            MusicLibraryStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0f, emptyList(), emptyList(), emptyList(), emptyList())
        }
    }
    
    // Library Scanning and Refresh
    
    suspend fun scanForNewMusic(): Int = withContext(Dispatchers.IO) {
        try {
            val currentTracks = trackDao.getAllTracks().map { it.filePath }.toSet()
            val newTracks = scanMusicFromMediaStore()
            val actuallyNewTracks = newTracks.filterNot { currentTracks.contains(it.filePath) }
            
            if (actuallyNewTracks.isNotEmpty()) {
                trackDao.insertTracks(actuallyNewTracks)
                refreshDerivedData(actuallyNewTracks)
            }
            
            AnalyticsManager.trackFeatureUsage("music_scan_completed", mapOf(
                "new_tracks_found" to actuallyNewTracks.size.toString()
            ))
            
            actuallyNewTracks.size
        } catch (e: Exception) {
            AnalyticsManager.trackError("music_scan_failed", e.message)
            0
        }
    }
    
    suspend fun refreshLibrary(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Clear existing data
            trackDao.clearAllTracks()
            albumDao.clearAllAlbums()
            artistDao.clearAllArtists()
            genreDao.clearAllGenres()
            
            // Rescan and populate
            val tracks = scanMusicFromMediaStore()
            if (tracks.isNotEmpty()) {
                trackDao.insertTracks(tracks)
                refreshDerivedData(tracks)
            }
            
            AnalyticsManager.trackFeatureUsage("music_library_refreshed", mapOf(
                "tracks_found" to tracks.size.toString()
            ))
            true
        } catch (e: Exception) {
            AnalyticsManager.trackError("music_library_refresh_failed", e.message)
            false
        }
    }
    
    // Helper method to refresh derived data (albums, artists, genres)
    private suspend fun refreshDerivedData(tracks: List<MusicTrack>) {
        try {
            val albums = generateAlbumsFromTracks()
            val artists = generateArtistsFromTracks()
            val genres = generateGenresFromTracks()
            
            if (albums.isNotEmpty()) albumDao.insertAlbums(albums)
            if (artists.isNotEmpty()) artistDao.insertArtists(artists)
            if (genres.isNotEmpty()) genreDao.insertGenres(genres)
        } catch (e: Exception) {
            AnalyticsManager.trackError("derived_data_refresh_failed", e.message)
        }
    }
    
    // Helper method to calculate playlist duration
    private suspend fun calculatePlaylistDuration(playlistId: String): Long {
        return try {
            val tracks = getPlaylistTracks(playlistId)
            tracks.sumOf { it.duration }
        } catch (e: Exception) {
            0L
        }
    }
    
    // Helper Methods
    
    private suspend fun scanMusicFromMediaStore(): List<MusicTrack> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<MusicTrack>()
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ARTIST,
            MediaStore.Audio.Media.COMPOSER,
            MediaStore.Audio.Media.GENRE,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumArtistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)
                val composerColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER)
                val genreColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE)
                val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val albumArtist = cursor.getString(albumArtistColumn)
                    val composer = cursor.getString(composerColumn)
                    val genre = cursor.getString(genreColumn)
                    val year = cursor.getInt(yearColumn).takeIf { it > 0 }
                    val trackNumber = cursor.getInt(trackColumn).takeIf { it > 0 }
                    val duration = cursor.getLong(durationColumn)
                    val filePath = cursor.getString(dataColumn) ?: continue
                    val size = cursor.getLong(sizeColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn) * 1000
                    val dateModified = cursor.getLong(dateModifiedColumn) * 1000
                    
                    // Extract additional metadata
                    val (bitrate, sampleRate, channels, format) = extractAudioMetadata(filePath)
                    val albumArtPath = extractAndSaveAlbumArt(filePath)
                    
                    val track = MusicTrack(
                        id = id.toString(),
                        title = title,
                        artist = artist,
                        album = album,
                        albumArtist = albumArtist,
                        composer = composer,
                        genre = genre,
                        year = year,
                        trackNumber = trackNumber,
                        duration = duration,
                        filePath = filePath,
                        fileSize = size,
                        bitrate = bitrate,
                        sampleRate = sampleRate,
                        channels = channels,
                        format = format,
                        albumArtPath = albumArtPath,
                        dateAdded = dateAdded,
                        dateModified = dateModified
                    )
                    
                    tracks.add(track)
                }
            }
        } catch (e: Exception) {
            AnalyticsManager.trackError("media_store_scan_failed", e.message)
        }
        
        tracks
    }
    
    private fun extractAudioMetadata(filePath: String): AudioMetadata {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()
            val sampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)?.let {
                when {
                    it.contains("mp3") -> null // MP3 sample rate not directly available
                    it.contains("flac") -> 44100 // Default for FLAC
                    it.contains("wav") -> 44100 // Default for WAV
                    else -> null
                }
            }
            val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            val format = when {
                mimeType?.contains("mp3") == true -> "MP3"
                mimeType?.contains("flac") == true -> "FLAC"
                mimeType?.contains("wav") == true -> "WAV"
                mimeType?.contains("ogg") == true -> "OGG"
                mimeType?.contains("m4a") == true -> "M4A"
                else -> "Unknown"
            }
            
            retriever.release()
            
            AudioMetadata(bitrate, sampleRate, 2, format) // Default to stereo
        } catch (e: Exception) {
            AudioMetadata(null, null, null, null)
        }
    }
    
    private data class AudioMetadata(
        val bitrate: Int?,
        val sampleRate: Int?,
        val channels: Int?,
        val format: String?
    )
    
    private suspend fun generateAlbumsFromTracks(): List<MusicAlbum> = withContext(Dispatchers.IO) {
        val tracks = getAllTracks()
        val albums = tracks.groupBy { it.album }.map { (albumName, albumTracks) ->
            val firstTrack = albumTracks.first()
            MusicAlbum(
                id = albumName,
                name = albumName,
                artist = albumTracks.map { it.artist }.distinct().joinToString(", "),
                albumArtist = firstTrack.albumArtist,
                year = albumTracks.mapNotNull { it.year }.maxOrNull(),
                genre = albumTracks.mapNotNull { it.genre }.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key,
                trackCount = albumTracks.size,
                totalDuration = albumTracks.sumOf { it.duration },
                albumArtPath = albumTracks.firstNotNullOfOrNull { it.albumArtPath },
                dateAdded = albumTracks.minOf { it.dateAdded },
                playCount = albumTracks.sumOf { it.playCount },
                lastPlayed = albumTracks.mapNotNull { it.lastPlayed }.maxOrNull(),
                rating = albumTracks.filter { it.rating > 0 }.map { it.rating }.average().toFloat()
            )
        }
        albums
    }
    
    private suspend fun generateArtistsFromTracks(): List<MusicArtist> = withContext(Dispatchers.IO) {
        val tracks = getAllTracks()
        val artists = tracks.groupBy { it.artist }.map { (artistName, artistTracks) ->
            val albums = artistTracks.map { it.album }.distinct()
            val genres = artistTracks.mapNotNull { it.genre }.distinct()
            
            MusicArtist(
                id = artistName,
                name = artistName,
                albumCount = albums.size,
                trackCount = artistTracks.size,
                totalDuration = artistTracks.sumOf { it.duration },
                genres = genres.joinToString(", "),
                dateAdded = artistTracks.minOf { it.dateAdded },
                playCount = artistTracks.sumOf { it.playCount },
                lastPlayed = artistTracks.mapNotNull { it.lastPlayed }.maxOrNull()
            )
        }
        artists
    }
    
    private suspend fun generateGenresFromTracks(): List<MusicGenre> = withContext(Dispatchers.IO) {
        val tracks = getAllTracks()
        val genres = tracks.mapNotNull { it.genre }.distinct().map { genreName ->
            val genreTracks = tracks.filter { it.genre == genreName }
            val genreArtists = genreTracks.map { it.artist }.distinct()
            val genreAlbums = genreTracks.map { it.album }.distinct()
            
            MusicGenre(
                id = genreName,
                name = genreName,
                trackCount = genreTracks.size,
                artistCount = genreArtists.size,
                albumCount = genreAlbums.size,
                totalDuration = genreTracks.sumOf { it.duration },
                dateAdded = genreTracks.minOf { it.dateAdded }
            )
        }
        genres
    }
}
