package com.example.anasdemoapplication.domain

sealed interface ScreenUiState {
    data object Loading : ScreenUiState

    data object Success : ScreenUiState

    data object Error : ScreenUiState
}