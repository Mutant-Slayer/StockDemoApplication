package com.example.anasdemoapplication.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.anasdemoapplication.model.TotalHolding

@Composable
fun HoldingScreen(
    modifier: Modifier = Modifier,
    viewModel: StockViewModel = hiltViewModel()
) {
    val totalHoldings by viewModel.totalHolding.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.getAllHoldings()
    }

    HoldingList(modifier = modifier, totalHoldings = totalHoldings)
}

@Composable
fun HoldingList(modifier: Modifier = Modifier, totalHoldings: RequestResult<TotalHolding>?) {
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

            is RequestResult.Success -> {}

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