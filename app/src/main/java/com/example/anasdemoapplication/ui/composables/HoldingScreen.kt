package com.example.anasdemoapplication.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.anasdemoapplication.R
import com.example.anasdemoapplication.StockViewModel
import com.example.anasdemoapplication.model.RequestResult
import com.example.anasdemoapplication.model.TotalHoldingsUiState

@Composable
fun HoldingScreen(
    modifier: Modifier = Modifier,
    viewModel: StockViewModel = hiltViewModel()
) {
    val totalHoldings by viewModel.totalHoldings.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.getAllHoldings()
    }

    HoldingList(modifier = modifier, totalHoldings = totalHoldings)
}

@Composable
fun HoldingList(
    modifier: Modifier = Modifier,
    totalHoldings: RequestResult<TotalHoldingsUiState>?
) {
    if (totalHoldings != null) {
        when (totalHoldings) {
            is RequestResult.Loading -> {
                val lottieRes = remember { LottieCompositionSpec.RawRes(R.raw.loading) }
                val composition by rememberLottieComposition(lottieRes)
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        modifier = Modifier.size(64.dp),
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                    )
                }
            }

            is RequestResult.Success -> {
                HoldingListUi(modifier = modifier, totalHoldings = totalHoldings.data)
            }

            is RequestResult.Error -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = totalHoldings.exception.message.toString(), color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun HoldingListUi(modifier: Modifier = Modifier, totalHoldings: TotalHoldingsUiState) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 10.dp)
    ) {
        item {
            HorizontalDivider()
        }
        items(
            count = totalHoldings.holdings.size,
            key = { totalHoldings.holdings[it].name },
        ) { index ->
            val uiState = totalHoldings.holdings[index]
            HoldingItem(userHolding = uiState)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp))
        }
    }
}

@Composable
fun HoldingItem(modifier: Modifier = Modifier, userHolding: TotalHoldingsUiState.HoldingUiState) {
    Row(modifier = modifier.padding(horizontal = 8.dp)) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(text = userHolding.name.uppercase(), fontWeight = FontWeight.SemiBold)
            Text(text = "NET QTY: ${userHolding.quantity}")
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End,
        ) {
            Text(text = "LTP: ₹ ${userHolding.lastTradedPrice}")
            Text(
                text = "P&L: ₹ %.2f".format(userHolding.profitAndLoss),
                color = if (userHolding.profitAndLoss >= 0) {
                    Color.Green.copy(alpha = 0.3f)
                } else {
                    Color.Red.copy(alpha = 0.5f)
                }
            )
        }
    }
}