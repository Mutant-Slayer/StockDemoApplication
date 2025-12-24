package com.example.anasdemoapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anasdemoapplication.data.StockRepository
import com.example.anasdemoapplication.model.RequestResult
import com.example.anasdemoapplication.model.TotalHoldingsUiState
import com.example.anasdemoapplication.model.toTotalHoldingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockViewModel
@Inject
constructor(
    private val stockRepository: StockRepository
) : ViewModel() {
    private val _totalHoldings =
        MutableStateFlow<RequestResult<TotalHoldingsUiState>>(RequestResult.Loading)
    val totalHoldings: StateFlow<RequestResult<TotalHoldingsUiState>> = _totalHoldings.asStateFlow()

    fun getAllHoldings() {
        _totalHoldings.update { RequestResult.Loading }
        viewModelScope.launch {
            when (val result = stockRepository.getHoldingList()) {
                is RequestResult.Success -> {
                    _totalHoldings.update {
                        RequestResult.Success(result.data.toTotalHoldingsUiState())
                    }
                }

                is RequestResult.Error -> {
                    _totalHoldings.update { RequestResult.Error(result.exception) }
                }

                else -> {}
            }
        }
    }
}