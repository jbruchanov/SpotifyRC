package com.scurab.android.spotifyrc.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scurab.android.spotify.api.SpotifyApi
import com.scurab.android.spotify.api.model.Item
import com.scurab.android.spotify.api.model.Search
import com.scurab.android.spotifyrc.lifecycle.LiveQueue
import com.scurab.android.spotifyrc.lifecycle.MutableLiveQueue
import com.scurab.android.spotifyrc.spotify.SpotifyBtClient
import com.scurab.android.spotifyrc.uistate.SearchUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class SearchViewModel @ViewModelInject constructor(
    private val spotifyApi: SpotifyApi,
    private val spotifyBtClient: SpotifyBtClient
) : ViewModel() {

    private val _uiState = MutableLiveData<SearchUiState>()
    val uiState: LiveData<SearchUiState> = _uiState

    private val _search = MutableLiveData<Search>()
    val search: LiveData<Search> = _search

    private val _error = MutableLiveQueue<String>()
    val error: LiveQueue<String> = _error

    fun search(query: String, type: String, market: String? = null) {
        _uiState.value = SearchUiState(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = spotifyApi.search(query, type, market)
                _search.postValue(result)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Null error message")
                e.printStackTrace()
            } finally {
                _uiState.postValue(SearchUiState(false))
            }
        }
    }

    fun onItemClicked(it: Item) {
        viewModelScope.launch {
            try {
                spotifyBtClient.play(it.uri)
            } catch (e: Exception) {
                _error.emit(e.message ?: "Null error message")
            }
        }
    }
}