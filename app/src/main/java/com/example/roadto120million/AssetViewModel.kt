package com.example.roadto120million

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.launch

class AssetViewModel(private val repository: AssetRepository): ViewModel() {

    var nowAssets by mutableStateOf("")
    var monthlyReserve by mutableStateOf("")
    var annualRatePercent by mutableStateOf("")
    var resultText by mutableStateOf("0")
    var selectedRange by mutableStateOf(ChartRange.ALL)

    // 計算済みの全データを保持する
    private var fullProgression: List<Double> = emptyList()

    // UIが表示に使うモデル
    var chartEntryModel by mutableStateOf<ChartEntryModel?>(null)

    init {
        // アプリ起動時にDataStoreから値を読み込む
        viewModelScope.launch { repository.nowAssetsFlow.collect { nowAssets = it } }
        viewModelScope.launch { repository.monthlyReserveFlow.collect { monthlyReserve = it } }
        viewModelScope.launch { repository.annualRateFlow.collect { annualRatePercent = it } }
    }

    // ロジック:計算ボタンがタップされた時の処理
    fun onCalculate(startAge: Int) {
        val assets = nowAssets.toDoubleOrNull() ?: 0.0
        val reserve = monthlyReserve.toDoubleOrNull() ?: 0.0
        val rate = annualRatePercent.toDoubleOrNull() ?: 0.0

        fullProgression = calculateAssetProgression(startAge, assets, reserve, rate)

        viewModelScope.launch {
            repository.saveSettings(nowAssets, monthlyReserve, annualRatePercent)
        }

    }

    // 10年表示か全期間表示が切り替わった時の処理
    fun onRangeChanged(range: ChartRange) {
        selectedRange = range
        updateChartModel()
    }

    private fun updateChartModel() {
        if (fullProgression.isEmpty()) return

        val dataToShow = when (selectedRange) {
            ChartRange.ALL -> fullProgression
            ChartRange.TEN_YEARS -> fullProgression.take(11)
        }

        chartEntryModel = entryModelOf(
            dataToShow.mapIndexed { index, d ->
                entryOf(index.toFloat(), d.toFloat())
            }
        )
    }

}