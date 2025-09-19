package com.bytecoder.vplay.frontend.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bytecoder.vplay.backend.managers.PlaybackQueueManager
import com.bytecoder.vplay.backend.managers.MusicLibraryScreenManager
import com.bytecoder.vplay.backend.managers.MusicTrack
import com.bytecoder.vplay.backend.managers.MusicAlbum
import com.bytecoder.vplay.backend.managers.MusicArtist
import com.bytecoder.vplay.backend.managers.MusicPlaylist
import com.bytecoder.vplay.backend.data.models.MediaItemModel
import com.bytecoder.vplay.TabMode

@Composable
fun EnhancedMusicScreen(
    queueManager: PlaybackQueueManager,
    navController: NavController,
    MusicLibraryScreenManager: MusicLibraryScreenManager = viewModel()
) {
    val context = LocalContext.current
    
    // Initialize the music library
    LaunchedEffect(Unit) {
        MusicLibraryScreenManager.initialize(context)
        queueManager.setMusicLibraryManager(context)
    }
    
    var selectedMode by remember { mutableStateOf(TabMode.LIBRARY) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    val sortOptions = listOf(
        "title" to "Title",
        "artist" to "Artist",
        "album" to "Album", 
        "duration" to "Duration",
        "date_added" to "Date Added"
    )
    
    // Collect state from ViewModel
    val tracks by MusicLibraryScreenManager.tracks.collectAsStateWithLifecycle()
    val albums by MusicLibraryScreenManager.albums.collectAsStateWithLifecycle()
    val artists by MusicLibraryScreenManager.artists.collectAsStateWithLifecycle()
    val playlists by MusicLibraryScreenManager.playlists.collectAsStateWithLifecycle()
    val isLoading by MusicLibraryScreenManager.isLoading.collectAsStateWithLifecycle()
    val error by MusicLibraryScreenManager.error.collectAsStateWithLifecycle()
    
    // Show error snackbar
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show snackbar or handle error
            MusicLibraryScreenManager.clearError()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with title and actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Music",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row {
                IconButton(
                    onClick = { showSearch = !showSearch }
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search Music",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Box {
                    IconButton(
                        onClick = { showSortMenu = true }
                    ) {
                        Icon(
                            Icons.Default.Sort,
                            contentDescription = "Sort Music",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        sortOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(label)
                                        if (MusicLibraryScreenManager.currentSortOption == value) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Selected",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    MusicLibraryScreenManager.updateSortOption(value)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
                if (selectedMode == TabMode.PLAYLISTS) {
                    IconButton(onClick = { showCreatePlaylistDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Create Playlist",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(
                    onClick = { MusicLibraryScreenManager.refreshLibrary() }
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh Music",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Search bar
        androidx.compose.animation.AnimatedVisibility(
            visible = showSearch,
            enter = androidx.compose.animation.slideInVertically() + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically() + androidx.compose.animation.fadeOut()
        ) {
            OutlinedTextField(
                value = MusicLibraryScreenManager.searchQuery,
                onValueChange = { MusicLibraryScreenManager.updateSearchQuery(it) },
                label = { Text("Search music...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (MusicLibraryScreenManager.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { MusicLibraryScreenManager.updateSearchQuery("") }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )
        }
        
        // Tab row for Library/Playlists
        TabRow(
            selectedTabIndex = selectedMode.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedMode == TabMode.LIBRARY,
                onClick = { selectedMode = TabMode.LIBRARY },
                text = { Text("Library") }
            )
            Tab(
                selected = selectedMode == TabMode.PLAYLISTS,
                onClick = { selectedMode = TabMode.PLAYLISTS },
                text = { Text("Playlists") }
            )
        }
        
        // Content based on selected mode
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (selectedMode) {
                TabMode.LIBRARY -> MusicLibraryContent(
                    tracks = tracks,
                    albums = albums,
                    artists = artists,
                    isLoading = isLoading,
                    onTrackClick = { track ->
                        val mediaItem = MediaItemModel(
                            id = track.id,
                            title = track.title,
                            subtitle = track.artist,
                            uri = track.filePath,
                            isVideo = false,
                            durationMs = track.duration,
                            thumbnailPath = track.albumArtPath
                        )
                        queueManager.setQueue(listOf(mediaItem), 0)
                        navController.navigate("audio_player")
                    },
                    onTrackFavorite = { trackId ->
                        MusicLibraryScreenManager.toggleFavorite(trackId)
                    },
                    navController = navController
                )
                TabMode.PLAYLISTS -> MusicPlaylistsContent(
                    playlists = playlists,
                    isLoading = isLoading,
                    onPlaylistClick = { playlist ->
                        navController.navigate("playlist_detail/${playlist.id}")
                    },
                    onDeletePlaylist = { playlistId ->
                        MusicLibraryScreenManager.deletePlaylist(playlistId)
                    }
                )
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
    
    // Create playlist dialog
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name, description ->
                MusicLibraryScreenManager.createPlaylist(name, description)
                showCreatePlaylistDialog = false
            }
        )
    }
}

@Composable
private fun MusicLibraryContent(
    tracks: List<MusicTrack>,
    albums: List<MusicAlbum>,
    artists: List<MusicArtist>,
    isLoading: Boolean,
    onTrackClick: (MusicTrack) -> Unit,
    onTrackFavorite: (String) -> Unit,
    navController: NavController
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Sub-tabs for Songs/Albums/Artists
        TabRow(
            selectedTabIndex = selectedSubTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Songs (${tracks.size})") }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Albums (${albums.size})") }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("Artists (${artists.size})") }
            )
        }
        
        when (selectedSubTab) {
            0 -> TracksContent(tracks, onTrackClick, onTrackFavorite)
            1 -> AlbumsContent(albums, navController)
            2 -> ArtistsContent(artists, navController)
        }
    }
}

@Composable
private fun TracksContent(
    tracks: List<MusicTrack>,
    onTrackClick: (MusicTrack) -> Unit,
    onTrackFavorite: (String) -> Unit
) {
    if (tracks.isEmpty()) {
        EmptyState(
            icon = Icons.Default.MusicNote,
            title = "No Songs Found",
            subtitle = "Your music library will appear here"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(tracks) { track ->
                TrackItem(
                    track = track,
                    onClick = { onTrackClick(track) },
                    onFavoriteClick = { onTrackFavorite(track.id) }
                )
            }
        }
    }
}

@Composable
private fun AlbumsContent(albums: List<MusicAlbum>, navController: NavController) {
    if (albums.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Album,
            title = "No Albums Found",
            subtitle = "Your music albums will appear here"
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(albums) { album ->
                AlbumItem(album = album, onClick = { 
                    navController.navigate("album_detail/${album.id}")
                })
            }
        }
    }
}

@Composable
private fun ArtistsContent(artists: List<MusicArtist>, navController: NavController) {
    if (artists.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Person,
            title = "No Artists Found",
            subtitle = "Your music artists will appear here"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(artists) { artist ->
                ArtistItem(artist = artist, onClick = { 
                    navController.navigate("artist_detail/${artist.name}")
                })
            }
        }
    }
}

@Composable
private fun MusicPlaylistsContent(
    playlists: List<MusicPlaylist>,
    isLoading: Boolean,
    onPlaylistClick: (MusicPlaylist) -> Unit,
    onDeletePlaylist: (String) -> Unit
) {
    if (playlists.isEmpty() && !isLoading) {
        EmptyState(
            icon = Icons.AutoMirrored.Filled.PlaylistPlay,
            title = "No Music Playlists",
            subtitle = "Create playlists to organize your music"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(playlists) { playlist ->
                PlaylistItem(
                    playlist = playlist,
                    onClick = { onPlaylistClick(playlist) },
                    onDelete = { onDeletePlaylist(playlist.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackItem(
    track: MusicTrack,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track number or music icon
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (track.trackNumber != null && track.trackNumber > 0) {
                    Text(
                        text = track.trackNumber.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Track info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${track.album}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Duration
            Text(
                text = formatDuration(track.duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Favorite button
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (track.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (track.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumItem(
    album: MusicAlbum,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Album,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .weight(1f),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = album.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = album.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArtistItem(
    artist: MusicArtist,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${artist.albumCount} albums • ${artist.trackCount} songs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistItem(
    playlist: MusicPlaylist,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.PlaylistPlay,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${playlist.trackCount} songs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete playlist",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Playlist") },
            text = { Text("Are you sure you want to delete \"${playlist.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Playlist") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, description) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}


