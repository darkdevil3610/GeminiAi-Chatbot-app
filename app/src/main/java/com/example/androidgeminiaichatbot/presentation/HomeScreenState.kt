package com.example.androidgeminiaichatbot.presentation

sealed class HomeScreenState {
    object initial : HomeScreenState()
    object loading : HomeScreenState()
    data class Success(val successMessage: String) : HomeScreenState()
    data class Error(val errorMessage: String) : HomeScreenState()
}