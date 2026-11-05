package com.example.ch12.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ch12.data.model.UserProfile

@Composable
fun NoteListRoute(
    userEmail: String?,
    profile: UserProfile,
    onOpenProfile: () -> Unit,
    onAddNote: () -> Unit,
    onEditNote: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NoteListScreen(
        uiState = uiState,
        userEmail = userEmail,
        profile = profile,
        onOpenProfile = onOpenProfile,
        onQueryChange = viewModel::onQueryChange,
        onTagSelected = viewModel::onTagSelected,
        onAddNote = onAddNote,
        onEditNote = onEditNote,
        onDeleteNote = viewModel::deleteNote,
        onTogglePin = viewModel::togglePin,
        onUndoDelete = viewModel::undoDelete,
        onUndoDismissed = viewModel::onUndoDismissed,
        onLogout = onLogout
    )
}
