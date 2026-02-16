package com.example.roadto120million

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roadto120million.ui.theme.RoadTo120MillionTheme
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter

enum class ChartRange {
    ALL, TEN_YEARS
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoadTo120MillionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AssetCalculationView(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CalculationInputField(
    label: String,
    value: String,
    unit: String = "",
    onValueChange: (String) -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(2f),
            suffix = { Text(unit)},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            singleLine = true
        )
    }
}

fun calculateAssetProgression(
    startAge: Int,
    nowAssets: Double,
    monthlyReserve: Double,
    annualRatePercent: Double
): List<Double> {
    val targetAge = 55
    val assetList = mutableListOf<Double>()

    val monthlyRate = annualRatePercent / 100 / 12
    var currentAsset = nowAssets

    // 現在の年齢から目標年齢まで1年ずつループ
    for (age in startAge..targetAge) {
        // その年の年初の資産額を記録
        assetList.add(currentAsset)

        // 1年分（12ヶ月）の複利計算を回す
        repeat(12) {
            currentAsset = (currentAsset + monthlyReserve) * (1 + monthlyRate)
        }
    }
    return assetList
}

@Composable
fun AssetCalculationView(
    modifier: Modifier = Modifier,
    viewModel: AssetViewModel = viewModel()
) {
    val startAge = 28

    val commonCornerSize = 12.dp    // Cardパーツやボタンなどのコーナー角を共通にする

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "シミュレーション設定",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card (
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(commonCornerSize)
        ) {
            Column(modifier = modifier.padding(16.dp)) {
                CalculationInputField("現在の資産", viewModel.nowAssets, unit = "円", onValueChange = {viewModel.nowAssets = it})
                CalculationInputField("毎月の積立額", viewModel.monthlyReserve, unit = "円", onValueChange = {viewModel.monthlyReserve = it})
                CalculationInputField("年利", viewModel.annualRatePercent, unit = "%", onValueChange = {viewModel.annualRatePercent = it})
            }
        }

        Button(
            onClick = { viewModel.onCalculate(startAge)},
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 12.dp
            ),
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(commonCornerSize)
        ) {
            Text("計算する")
        }

        Row(modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = { viewModel.onRangeChanged(ChartRange.ALL) },
                enabled = viewModel.selectedRange != ChartRange.ALL,
                shape = RoundedCornerShape(commonCornerSize)
            ) {
                Text("全期間")
            }

            Spacer(modifier = modifier.width(8.dp))

            Button(
                onClick = { viewModel.onRangeChanged(ChartRange.TEN_YEARS) },
                enabled = viewModel.selectedRange != ChartRange.TEN_YEARS,
                shape = RoundedCornerShape(commonCornerSize)
            ) {
                Text("10年間")
            }
        }

        val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom>{ x, _ ->
            "${(x + startAge).toInt()}歳"
        }
        val startAxisValueFormatter = AxisValueFormatter<AxisPosition.Vertical.Start>{ y, _ ->
            "%,.0f百万".format(y / 1_000_000)
        }

        val model = viewModel.chartEntryModel
        if (model != null) {
            Chart(
                chart = lineChart(),
                model = model,
                startAxis = rememberStartAxis(valueFormatter = startAxisValueFormatter),
                bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter),

                modifier = modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(top = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RoadTo120MillionTheme {
        AssetCalculationView(viewModel = viewModel())
    }
}