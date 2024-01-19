package com.unsika.waterfilterapp.data.remote

sealed class DataState<out R> private constructor() {
    data class Success<out T>(val data: T) : DataState<T>()
    data class Failure(val message: String) : DataState<Nothing>()
    object Loading : DataState<Nothing>()
}