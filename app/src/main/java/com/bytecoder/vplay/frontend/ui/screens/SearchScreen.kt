package com.bytecoder.vplay.frontend.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bytecoder.vplay.backend.data.models.MediaItemModel
import com.bytecoder.vplay.frontend.viewmodels.SearchViewModel
import com.bytecoder.vplay.frontend.viewmodels.PlaybackQueueViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    queueViewModel: PlaybackQueueViewModel,
    searchViewModel: SearchViewModel = viewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by searchViewModel.searchResults.collectAsState()
    val isSearching by searchViewModel.isSearching.collectAsState()
    val recentSearches by searchViewModel.recentSearches.collectAsState()
    
    // Auto-focus search field
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                if (it.isNotBlank()) {
                    searchViewModel.search(it)
                } else {
                    searchViewModel.clearResults()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = { Text("Search music and videos...") },
            leadingIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { 
                        searchQuery = ""
                        searchViewModel.clearResults()
                    }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (searchQuery.isNotBlank()) {
                        searchViewModel.search(searchQuery)
                        searchViewModel.addToRecentSearches(searchQuery)
                    }
                    keyboardController?.hide()
                }
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when {
            isSearching -> {
                // Loading indicator
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            searchResults.isNotEmpty() -> {
                // Search results
                LazyColumn {
                    items(searchResults) { item ->
                        SearchResultItem(
                            item = item,
                            onClick = {
                                // Add to queue and play
                                queueViewModel.addToQueue(item)
                                queueViewModel.setCurrentIndex(queueViewModel.queue.value?.size?.minus(1) ?: 0)
                                navController.navigate("audio_player")
                            },
                            onAddToQueue = {
                                queueViewModel.addToQueue(item)
                            }
                        )
                    }
                }
            }
            
            searchQuery.isBlank() && recentSearches.isNotEmpty() -> {
                // Recent searches
                Text(
                    text = "Recent Searches",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                LazyColumn {
                    items(recentSearches) { search ->
                        ListItem(
                            headlineContent = { Text(search) },
                            leadingContent = {
                                Icon(Icons.Filled.History, contentDescription = null)
                            },
                            trailingContent = {
                                IconButton(onClick = { searchViewModel.removeFromRecentSearches(search) }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchQuery = search
                                    searchViewModel.search(search)
                                }
                        )
                    }
                }
            }
            
            searchQuery.isNotBlank() && searchResults.isEmpty() && !isSearching -> {
                // No results
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No results found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Try a different search term",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            else -> {
                // Search suggestions
                Column {
                    Text(
                        text = "Search for music and videos",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    val suggestions = listOf("Popular songs", "Recent albums", "Artists", "Playlists")
                    suggestions.forEach { suggestion ->
                        ListItem(
                            headlineContent = { Text(suggestion) },
                            leadingContent = {
                                Icon(Icons.Filled.TrendingUp, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchQuery = suggestion
                                    searchViewModel.search(suggestion)
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    item: MediaItemModel,
    onClick: () -> Unit,
    onAddToQueue: () -> Unit
) {
    ListItem(
        headlineContent = { 
            Text(
                text = item.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            ) 
        },
        supportingContent = {
            Text(
                text = item.subtitle ?: "Unknown",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Icon(
                imageVector = if (item.isVideo) Icons.Filled.VideoLibrary else Icons.Filled.MusicNote,
                contentDescription = if (item.isVideo) "Video" else "Audio"
            )
        },
        trailingContent = {
            Row {
                IconButton(onClick = onAddToQueue) {
                    Icon(Icons.Filled.Add, contentDescription = "Add to queue")
                }
                IconButton(onClick = onClick) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    )
}