package com.example.ch12.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ch12.data.model.Note
import com.example.ch12.data.model.UserProfile
import com.example.ch12.ui.profile.ProfilePhoto

// Screen murni presentasional: hanya menerima state dan callback.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    uiState: NoteListUiState,
    userEmail: String?,
    profile: UserProfile,
    onOpenProfile: () -> Unit,
    onQueryChange: (String) -> Unit,
    onTagSelected: (String?) -> Unit,
    onAddNote: () -> Unit,
    onEditNote: (String) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onTogglePin: (Note) -> Unit,
    onUndoDelete: () -> Unit,
    onUndoDismissed: () -> Unit,
    onLogout: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnUndo by rememberUpdatedState(onUndoDelete)
    val currentOnDismissed by rememberUpdatedState(onUndoDismissed)

    // Setiap ada catatan yang baru dihapus, tampilkan Snackbar.
    // "Batalkan" -> dokumen ditulis kembali ke Firestore.
    LaunchedEffect(uiState.recentlyDeleted) {
        if (uiState.recentlyDeleted == null) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "Catatan dihapus",
            actionLabel = "Batalkan",
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) currentOnUndo() else currentOnDismissed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("CatatanKu Cloud")
                        userEmail?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Chapter 12: ketuk avatar -> layar foto profil
                    IconButton(onClick = onOpenProfile) {
                        ProfilePhoto(model = profile.photoUrl, version = profile.photoUpdatedAt, size = 32.dp)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Keluar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNote) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah catatan")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            NoteSearchField(
                query = uiState.query,
                onQueryChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter chip berdasarkan tag
            if (uiState.availableTags.isNotEmpty()) {
                TagFilterRow(
                    tags = uiState.availableTags,
                    selectedTag = uiState.selectedTag,
                    onTagSelected = onTagSelected
                )
            }

            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                uiState.errorMessage != null -> CenteredMessage(uiState.errorMessage)

                uiState.notes.isEmpty() -> CenteredMessage(emptyMessage(uiState))

                else -> {
                    // Waktu "sekarang" dihitung ulang setiap daftar berubah
                    val now = remember(uiState.notes) { System.currentTimeMillis() }
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.notes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                now = now,
                                onClick = { onEditNote(note.id) },
                                onDelete = { onDeleteNote(note) },
                                onTogglePin = { onTogglePin(note) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Pesan kosong dibedakan: belum ada catatan sama sekali vs hasil filter kosong
private fun emptyMessage(uiState: NoteListUiState): String = when {
    !uiState.isFiltering -> "Belum ada catatan. Ketuk + untuk memulai."
    uiState.query.isNotBlank() ->
        "Tidak ada catatan yang cocok dengan \"${uiState.query}\""
    else -> "Tidak ada catatan dengan tag #${uiState.selectedTag}"
}

@Composable
private fun CenteredMessage(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp)
        )
    }
}

// TextField hanya melaporkan teks; penyaringan ada di ViewModel.
@Composable
private fun NoteSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Cari judul atau isi catatan...") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Hapus pencarian")
                }
            }
        },
        singleLine = true
    )
}

@Composable
private fun TagFilterRow(
    tags: List<String>,
    selectedTag: String?,
    onTagSelected: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedTag == null,
                onClick = { onTagSelected(null) },
                label = { Text("Semua") }
            )
        }
        items(tags, key = { it }) { tag ->
            FilterChip(
                selected = tag == selectedTag,
                onClick = { onTagSelected(tag) },
                label = { Text("#$tag") }
            )
        }
    }
}
