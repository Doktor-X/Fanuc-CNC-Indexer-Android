package com.cncindex.ui.`import`

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cncindex.CncIndexApp
import com.cncindex.data.repository.ImportResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ImportUiState {
    object Idle : ImportUiState()
    object Loading : ImportUiState()
    data class Success(val message: String) : ImportUiState()
    data class Error(val message: String) : ImportUiState()
}

class ImportViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as CncIndexApp).repository

    private val _state = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val state: StateFlow<ImportUiState> = _state.asStateFlow()

    fun importIndex(uri: Uri) {
        viewModelScope.launch {
            _state.value = ImportUiState.Loading
            when (val result = repository.importIndexFile(uri)) {
                is ImportResult.Success ->
                    _state.value = ImportUiState.Success(
                        "Uvezeno ${result.programCount} programa"
                    )
                is ImportResult.Error ->
                    _state.value = ImportUiState.Error(result.message)
            }
        }
    }

    fun importTools(uri: Uri) {
        viewModelScope.launch {
            _state.value = ImportUiState.Loading
            when (val result = repository.importToolsFile(uri)) {
                is ImportResult.Success ->
                    _state.value = ImportUiState.Success(
                        "Uvezeno ${result.toolCount} alata"
                    )
                is ImportResult.Error ->
                    _state.value = ImportUiState.Error(result.message)
            }
        }
    }

    fun resetState() {
        _state.value = ImportUiState.Idle
    }
}
