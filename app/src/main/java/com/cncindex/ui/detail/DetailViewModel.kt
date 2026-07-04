package com.cncindex.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cncindex.CncIndexApp
import com.cncindex.data.local.entity.CncProgram
import com.cncindex.data.local.entity.Tool
import com.cncindex.data.repository.CncRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val program: CncProgram? = null,
    val tools: List<Tool> = emptyList(),
    val isLoading: Boolean = true
)

class DetailViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as CncIndexApp).repository
    private val gson = Gson()

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun load(program: CncProgram) {
        viewModelScope.launch {
            val type = object : TypeToken<List<Int>>() {}.type
            val toolIds: List<Int> = gson.fromJson(program.toolsJson, type) ?: emptyList()
            val tools = if (toolIds.isNotEmpty()) {
                repository.getToolsForProgram(program.toolsJson)
            } else emptyList()

            _uiState.value = DetailUiState(
                program = program,
                tools = tools,
                isLoading = false
            )
        }
    }
}
