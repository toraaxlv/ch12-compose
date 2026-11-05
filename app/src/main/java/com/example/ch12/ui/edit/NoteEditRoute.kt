package com.example.ch12.ui.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// noteId tidak dioper lagi: hiltViewModel() membacanya dari SavedStateHandle
@Composable
fun NoteEditRoute(
    onNavigateBack: () -> Unit,
    viewModel: NoteEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NoteEditScreen(
        isNewNote = viewModel.isNewNote,
        uiState = uiState,
        onTitleChange = viewModel::onTitleChange,
        onContentChange = viewModel::onContentChange,
        onTagsChange = viewModel::onTagsChange,
        onSave = viewModel::save,
        onNavigateBack = onNavigateBack
    )
}
