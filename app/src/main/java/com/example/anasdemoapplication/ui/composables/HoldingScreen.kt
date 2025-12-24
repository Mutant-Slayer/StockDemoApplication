package com.example.anasdemoapplication.ui.composables

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.anasdemoapplication.R
import com.example.anasdemoapplication.data.remote.RequestResult
import com.example.anasdemoapplication.domain.TotalHoldingsUiState
import com.example.anasdemoapplication.ui.StockViewModel

@Composable
fun HoldingScreen(
    modifier: Modifier = Modifier,
    viewModel: StockViewModel = hiltViewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)
) {
    val totalHoldings by viewModel.totalHoldings.collectAsStateWithLifecycle()

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
    var isExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 10.dp)
    ) {
        HorizontalDivider()
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            totalHoldings.holdings.forEach { uiState ->
                HoldingItem(userHolding = uiState)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Gray)
        ) {
            // Clickable header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Profit & Loss",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "₹ %.2f (%.2f%%)".format(
                        totalHoldings.totalPnL,
                        totalHoldings.pnlPercentage
                    ),
                    color = if (totalHoldings.totalPnL >= 0) Color.Green.copy(alpha = 0.5f) else Color.Red.copy(
                        alpha = 0.5f
                    ),
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(
                visible = isExpanded
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PnLDetailRow(
                        label = "Current value*",
                        value = "₹ ${totalHoldings.currentValue}"
                    )
                    PnLDetailRow(
                        label = "Total investment*",
                        value = "₹ ${totalHoldings.totalInvestment}"
                    )
                    PnLDetailRow(
                        label = "Today's Profit & Loss*",
                        value = "₹ %.2f".format(totalHoldings.todaysPnL),
                        valueColor = if (totalHoldings.todaysPnL >= 0) Color.Green.copy(alpha = 0.5f) else Color.Red.copy(
                            alpha = 0.5f
                        )
                    )
                }
            }
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
                    Color.Green.copy(alpha = 0.5f)
                } else {
                    Color.Red.copy(alpha = 0.5f)
                }
            )
        }
    }
}

@Composable
fun PnLDetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = if (isHighlighted) 16.sp else 14.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isHighlighted) 16.sp else 14.sp
        )
    }
}