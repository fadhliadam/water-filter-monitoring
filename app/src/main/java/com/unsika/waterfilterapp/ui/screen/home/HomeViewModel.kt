package com.unsika.waterfilterapp.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unsika.waterfilterapp.data.History
import com.unsika.waterfilterapp.data.RepositoryImpl
import com.unsika.waterfilterapp.data.Water
import com.unsika.waterfilterapp.data.remote.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repositoryImpl: RepositoryImpl
) : ViewModel() {
    private val _responseWater = MutableStateFlow<DataState<Water?>>(DataState.Loading)
    val responseWater: StateFlow<DataState<Water?>> = _responseWater

    private val _responseHistory = MutableStateFlow<DataState<List<History?>>>(DataState.Loading)
    val responseHistory: StateFlow<DataState<List<History?>>> = _responseHistory

    private val _responseDelete = MutableStateFlow<DataState<String>>(DataState.Loading)
    val responseDelete: StateFlow<DataState<String>> = _responseDelete

    init {
        fetchDataFromFirebase()
        fetchHistory()
        deleteHistory()
    }

    private fun fetchDataFromFirebase() {
        viewModelScope.launch {
            _responseWater.value = DataState.Loading
            repositoryImpl.fetchDataFromFirebase().collect {
                _responseWater.value = it
            }
        }
    }

    private fun fetchHistory() {
        viewModelScope.launch {
            _responseHistory.value = DataState.Loading
            repositoryImpl.fetchHistory().collect {
                _responseHistory.value = it
            }
        }
    }

    private fun deleteHistory() {
        viewModelScope.launch {
            _responseDelete.value = DataState.Loading
            repositoryImpl.deleteHistory().collect {
                _responseDelete.value = it
            }
        }
    }
}