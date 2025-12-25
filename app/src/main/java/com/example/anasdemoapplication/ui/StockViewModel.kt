package com.example.anasdemoapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anasdemoapplication.data.mapper.toTotalHoldingsUiState
import com.example.anasdemoapplication.data.remote.RequestResult
import com.example.anasdemoapplication.domain.ScreenUiState
import com.example.anasdemoapplication.domain.StockRepository
import com.example.anasdemoapplication.domain.TotalHoldingsUiState
import com.example.anasdemoapplication.utils.ConnectivityReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockViewModel
@Inject
constructor(
    private val stockRepository: StockRepository,
    connectivityReceiver: ConnectivityReceiver,
) : ViewModel() {
    private val _totalHoldings = MutableStateFlow(TotalHoldingsUiState())
    val totalHoldings: StateFlow<TotalHoldingsUiState> = _totalHoldings.asStateFlow()

    val isConnected = connectivityReceiver.isConnected.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    init {
        getAllHoldings()
    }

    fun getAllHoldings() {
        _totalHoldings.update { it.copy(screenUiState = ScreenUiState.Loading) }
        viewModelScope.launch {
            try {
                when (val result = stockRepository.getHoldingList()) {
                    is RequestResult.Success -> {
                        _totalHoldings.update {
                            result.data.toTotalHoldingsUiState()
                        }
                    }

                    is RequestResult.Error -> {
                        _totalHoldings.update { it.copy(screenUiState = ScreenUiState.Error) }
                    }

                    else -> {
                        _totalHoldings.update { it.copy(screenUiState = ScreenUiState.Error) }
                    }
                }
            } catch (_: Exception) {
                _totalHoldings.update { it.copy(screenUiState = ScreenUiState.Error) }
            }
        }
    }
}