package com.bytecoder.vplay.database

import androidx.room.*
import androidx.lifecycle.LiveData
import com.bytecoder.vplay.ui.music.*

@Dao
interface MusicTrackDao {
    @Query("SELECT * FROM music_tracks ORDER BY title ASC")
    suspend fun getAllTracks(): List<MusicTrack>

    @Query("SELECT * FROM music_tracks WHERE id = :id")
    suspend fun getTrack(id: String): MusicTrack?

    @Query("SELECT * FROM music_tracks WHERE artist = :artist ORDER BY album ASC, trackNumber ASC")
    suspend fun getTracksByArtist(artist: String): List<MusicTrack>

    @Query("SELECT * FROM music_tracks WHERE album = :album ORDER BY trackNumber ASC")
    suspend fun getTracksByAlbum(album: String): List<MusicTrack>

    @Query("SELECT * FROM music_tracks WHERE genre = :genre ORDER BY artist ASC, album ASC, trackNumber ASC")
    suspend fun getTracksByGenre(genre: String): List<MusicTrack>

    @Query("SELECT * FROM music_tracks WHERE isFavorite = 1 ORDER BY dateModified DESC")
    suspend fun getFavoriteTracks(): List<MusicTrack>

    @Query("SELECT * FROM music_tracks WHERE lastPlayed IS NOT NULL ORDER BY lastPlayed DESC LIMIT :limit")
    suspend fun getRecentlyPlayedTracks(limit: Int): List<MusicTrack>

    @Query("SELECT * FROM music_tracks ORDER BY dateAdded DESC LIMIT :limit")
    suspend fun getRecentlyAddedTracks(limit: Int): List<MusicTrack>

    @Query("SELECT * FROM music_tracks WHERE playCount = 0 ORDER BY dateAdded DESC LIMIT :limit")
    suspend fun getNeverPlayedTracks(limit: Int): List<MusicTrack>

    @Query("SELECT * FROM music_tracks WHERE rating >= :minRating ORDER BY rating DESC LIMIT :limit")
    suspend fun getTopRatedTracks(minRating: Float, limit: Int): List<MusicTrack>

    @Query("SELECT * FROM music_tracks WHERE (title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%' OR genre LIKE '%' || :query || '%')")
    suspend fun searchTracks(query: String): List<MusicTrack>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: MusicTrack)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<MusicTrack>)

    @Update
    suspend fun updateTrack(track: MusicTrack)

    @Delete
    suspend fun deleteTrack(track: MusicTrack)

    @Query("UPDATE music_tracks SET playCount = playCount + 1, lastPlayed = :timestamp WHERE id = :id")
    suspend fun incrementPlayCount(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE music_tracks SET resumePosition = :position WHERE id = :id")
    suspend fun updateResumePosition(id: String, position: Long)

    @Query("UPDATE music_tracks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("UPDATE music_tracks SET rating = :rating WHERE id = :id")
    suspend fun updateRating(id: String, rating: Float)

    @Query("DELETE FROM music_tracks")
    suspend fun clearAllTracks()

    @Query("SELECT COUNT(*) FROM music_tracks")
    suspend fun getTrackCount(): Int

    @Query("SELECT COUNT(*) FROM music_tracks WHERE isFavorite = 1")
    suspend fun getFavoriteTrackCount(): Int

    @Query("SELECT COUNT(*) FROM music_tracks WHERE playCount > 0")
    suspend fun getPlayedTrackCount(): Int

    @Query("SELECT SUM(duration) FROM music_tracks")
    suspend fun getTotalDuration(): Long

    @Query("SELECT SUM(fileSize) FROM music_tracks")
    suspend fun getTotalSize(): Long

    @Query("SELECT AVG(rating) FROM music_tracks WHERE rating > 0")
    suspend fun getAverageRating(): Float?
}

@Dao
interface MusicAlbumDao {
    @Query("SELECT * FROM music_albums ORDER BY name ASC")
    suspend fun getAllAlbums(): List<MusicAlbum>

    @Query("SELECT * FROM music_albums WHERE id = :id")
    suspend fun getAlbum(id: String): MusicAlbum?

    @Query("SELECT * FROM music_albums WHERE artist = :artist ORDER BY year DESC, name ASC")
    suspend fun getAlbumsByArtist(artist: String): List<MusicAlbum>

    @Query("SELECT * FROM music_albums WHERE genre = :genre ORDER BY artist ASC, name ASC")
    suspend fun getAlbumsByGenre(genre: String): List<MusicAlbum>

    @Query("SELECT * FROM music_albums WHERE year = :year ORDER BY artist ASC, name ASC")
    suspend fun getAlbumsByYear(year: Int): List<MusicAlbum>

    @Query("SELECT * FROM music_albums WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    suspend fun getFavoriteAlbums(): List<MusicAlbum>

    @Query("SELECT * FROM music_albums ORDER BY dateAdded DESC LIMIT :limit")
    suspend fun getRecentlyAddedAlbums(limit: Int): List<MusicAlbum>

    @Query("SELECT * FROM music_albums WHERE (name LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%')")
    suspend fun searchAlbums(query: String): List<MusicAlbum>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: MusicAlbum)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<MusicAlbum>)

    @Update
    suspend fun updateAlbum(album: MusicAlbum)

    @Delete
    suspend fun deleteAlbum(album: MusicAlbum)

    @Query("UPDATE music_albums SET playCount = playCount + 1, lastPlayed = :timestamp WHERE id = :id")
    suspend fun incrementPlayCount(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE music_albums SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("UPDATE music_albums SET rating = :rating WHERE id = :id")
    suspend fun updateRating(id: String, rating: Float)

    @Query("DELETE FROM music_albums")
    suspend fun clearAllAlbums()

    @Query("SELECT COUNT(*) FROM music_albums")
    suspend fun getAlbumCount(): Int

    @Query("SELECT COUNT(*) FROM music_albums WHERE isFavorite = 1")
    suspend fun getFavoriteAlbumCount(): Int
}

@Dao
interface MusicArtistDao {
    @Query("SELECT * FROM music_artists ORDER BY name ASC")
    suspend fun getAllArtists(): List<MusicArtist>

    @Query("SELECT * FROM music_artists WHERE id = :id")
    suspend fun getArtist(id: String): MusicArtist?

    @Query("SELECT * FROM music_artists WHERE genres LIKE '%' || :genre || '%' ORDER BY name ASC")
    suspend fun getArtistsByGenre(genre: String): List<MusicArtist>

    @Query("SELECT * FROM music_artists WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    suspend fun getFavoriteArtists(): List<MusicArtist>

    @Query("SELECT * FROM music_artists ORDER BY playCount DESC LIMIT :limit")
    suspend fun getTopArtists(limit: Int): List<MusicArtist>

    @Query("SELECT * FROM music_artists ORDER BY dateAdded DESC LIMIT :limit")
    suspend fun getRecentlyAddedArtists(limit: Int): List<MusicArtist>

    @Query("SELECT * FROM music_artists WHERE name LIKE '%' || :query || '%'")
    suspend fun searchArtists(query: String): List<MusicArtist>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: MusicArtist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<MusicArtist>)

    @Update
    suspend fun updateArtist(artist: MusicArtist)

    @Delete
    suspend fun deleteArtist(artist: MusicArtist)

    @Query("UPDATE music_artists SET playCount = playCount + 1, lastPlayed = :timestamp WHERE id = :id")
    suspend fun incrementPlayCount(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE music_artists SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("DELETE FROM music_artists")
    suspend fun clearAllArtists()

    @Query("SELECT COUNT(*) FROM music_artists")
    suspend fun getArtistCount(): Int

    @Query("SELECT COUNT(*) FROM music_artists WHERE isFavorite = 1")
    suspend fun getFavoriteArtistCount(): Int
}

@Dao
interface MusicGenreDao {
    @Query("SELECT * FROM music_genres ORDER BY name ASC")
    suspend fun getAllGenres(): List<MusicGenre>

    @Query("SELECT * FROM music_genres WHERE id = :id")
    suspend fun getGenre(id: String): MusicGenre?

    @Query("SELECT * FROM music_genres ORDER BY trackCount DESC LIMIT :limit")
    suspend fun getTopGenres(limit: Int): List<MusicGenre>

    @Query("SELECT * FROM music_genres WHERE name LIKE '%' || :query || '%'")
    suspend fun searchGenres(query: String): List<MusicGenre>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenre(genre: MusicGenre)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenres(genres: List<MusicGenre>)

    @Update
    suspend fun updateGenre(genre: MusicGenre)

    @Delete
    suspend fun deleteGenre(genre: MusicGenre)

    @Query("DELETE FROM music_genres")
    suspend fun clearAllGenres()

    @Query("SELECT COUNT(*) FROM music_genres")
    suspend fun getGenreCount(): Int
}

@Dao
interface MusicPlaylistDao {
    @Query("SELECT * FROM music_playlists ORDER BY dateModified DESC")
    suspend fun getAllPlaylists(): List<MusicPlaylist>

    @Query("SELECT * FROM music_playlists WHERE id = :id")
    suspend fun getPlaylist(id: String): MusicPlaylist?

    @Query("SELECT * FROM music_playlists WHERE isSmartPlaylist = 0 ORDER BY name ASC")
    suspend fun getUserPlaylists(): List<MusicPlaylist>

    @Query("SELECT * FROM music_playlists WHERE isSmartPlaylist = 1 ORDER BY name ASC")
    suspend fun getSmartPlaylists(): List<MusicPlaylist>

    @Query("SELECT * FROM music_playlists WHERE isFavorite = 1 ORDER BY dateModified DESC")
    suspend fun getFavoritePlaylists(): List<MusicPlaylist>

    @Query("SELECT * FROM music_playlists WHERE name LIKE '%' || :query || '%'")
    suspend fun searchPlaylists(query: String): List<MusicPlaylist>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: MusicPlaylist): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<MusicPlaylist>)

    @Update
    suspend fun updatePlaylist(playlist: MusicPlaylist)

    @Delete
    suspend fun deletePlaylist(playlist: MusicPlaylist)

    @Query("UPDATE music_playlists SET playCount = playCount + 1, lastPlayed = :timestamp WHERE id = :id")
    suspend fun incrementPlayCount(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE music_playlists SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("UPDATE music_playlists SET trackCount = :count, totalDuration = :duration, dateModified = :timestamp WHERE id = :id")
    suspend fun updatePlaylistStats(id: String, count: Int, duration: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM music_playlists")
    suspend fun clearAllPlaylists()

    @Query("SELECT COUNT(*) FROM music_playlists")
    suspend fun getPlaylistCount(): Int
}

@Dao
interface MusicPlaylistTrackDao {
    @Query("SELECT * FROM music_playlist_tracks WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun getPlaylistTracks(playlistId: String): List<MusicPlaylistTrack>

    @Query("SELECT * FROM music_playlist_tracks WHERE trackId = :trackId")
    suspend fun getPlaylistsForTrack(trackId: String): List<MusicPlaylistTrack>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrack(playlistTrack: MusicPlaylistTrack)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTracks(playlistTracks: List<MusicPlaylistTrack>)

    @Delete
    suspend fun deletePlaylistTrack(playlistTrack: MusicPlaylistTrack)

    @Query("DELETE FROM music_playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String)

    @Query("DELETE FROM music_playlist_tracks WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: String)

    @Query("UPDATE music_playlist_tracks SET position = :position WHERE id = :id")
    suspend fun updateTrackPosition(id: String, position: Int)

    @Query("SELECT COUNT(*) FROM music_playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getPlaylistTrackCount(playlistId: String): Int

    @Query("SELECT MAX(position) FROM music_playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getMaxPosition(playlistId: String): Int?
}