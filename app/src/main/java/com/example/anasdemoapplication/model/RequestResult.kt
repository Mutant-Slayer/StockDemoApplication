package com.example.anasdemoapplication.model

sealed class RequestResult<out R> {
    data class Success<out T>(val data: T) : RequestResult<T>()

    data class Error(val exception: Exception) : RequestResult<Nothing>()

    data object Loading : RequestResult<Nothing>()
}