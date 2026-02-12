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
import com.example.roadto120million.ui.theme.RoadTo120MillionTheme
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf

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
                        name = "Android",
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
fun AssetCalculationView(name: String, modifier: Modifier = Modifier) {
    var nowAssets by rememberSaveable { mutableStateOf("") }
    var monthlyReserve by rememberSaveable { mutableStateOf("") }
    var annualRatePercent by rememberSaveable { mutableStateOf("") }
    var resultText by rememberSaveable { mutableStateOf("0") }
    val startAge = 28

    var selectedRange by remember { mutableStateOf(ChartRange.ALL) }
    var fullProgression by remember { mutableStateOf<List<Double>>(emptyList()) }

    val commonCornerSize = 12.dp    // Cardパーツやボタンなどのコーナー角を共通にする

    Column(
        modifier = Modifier
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
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(commonCornerSize)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                CalculationInputField("現在の資産", nowAssets, unit = "円", onValueChange = {nowAssets = it})
                CalculationInputField("毎月の積立額", monthlyReserve, unit = "円", onValueChange = {monthlyReserve = it})
                CalculationInputField("年利", annualRatePercent, unit = "%", onValueChange = {annualRatePercent = it})
            }
        }

        Button(
            onClick = {
                val assets = nowAssets.toDoubleOrNull() ?: 0.0
                val reserve = monthlyReserve.toDoubleOrNull() ?: 0.0
                val rate = annualRatePercent.toDoubleOrNull() ?: 0.0

                val progression = calculateAssetProgression(startAge, assets, reserve, rate)

                fullProgression = progression
            },
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 12.dp
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(commonCornerSize)
        ) {
            Text("計算する")
        }

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = { selectedRange = ChartRange.ALL },
                enabled = selectedRange != ChartRange.ALL,
                shape = RoundedCornerShape(commonCornerSize)
            ) {
                Text("全期間")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { selectedRange = ChartRange.TEN_YEARS },
                enabled = selectedRange != ChartRange.TEN_YEARS,
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

        val displayEntries: ChartEntryModel? = if (fullProgression.isNotEmpty()) {
            val dataToShow = when (selectedRange) {
                ChartRange.ALL -> fullProgression
                ChartRange.TEN_YEARS -> fullProgression.take(11)    // 0年目~10年目
            }

            entryModelOf(
                dataToShow.mapIndexed { index, d ->
                    entryOf(index.toFloat(), d.toFloat())
                }
            )
        } else null

        if (displayEntries != null) {
            Chart(
                chart = lineChart(),
                model = displayEntries,
                startAxis = rememberStartAxis(valueFormatter = startAxisValueFormatter),
                bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter),

                modifier = Modifier
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
        AssetCalculationView("Android")
    }
}