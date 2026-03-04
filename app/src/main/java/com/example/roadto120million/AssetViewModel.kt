package com.example.roadto120million

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.launch

class AssetViewModel(private val repository: AssetRepository): ViewModel() {

    var nowAssets by mutableStateOf("")
    var monthlyReserve by mutableStateOf("")
    var annualRatePercent by mutableStateOf("")
    var resultText by mutableStateOf("0")
    var startAgeString by mutableStateOf("20")
    var selectedRange by mutableStateOf(ChartRange.ALL)

    // 各項目のエラーメッセージを保持する
    var nowAssetsError by mutableStateOf<String?>(null)
    var monthlyReserveError by mutableStateOf<String?>(null)
    var annualRatePercentError by mutableStateOf<String?>(null)
    var startAgeError by mutableStateOf<String?>(null)

    // 計算済みの全データを保持する
    private var fullProgression: List<Double> = emptyList()

    // UIが表示に使うモデル
    var chartEntryModel by mutableStateOf<ChartEntryModel?>(null)

    init {
        // アプリ起動時にDataStoreから値を読み込む
        viewModelScope.launch { repository.nowAssetsFlow.collect { nowAssets = it } }
        viewModelScope.launch { repository.monthlyReserveFlow.collect { monthlyReserve = it } }
        viewModelScope.launch { repository.annualRateFlow.collect { annualRatePercent = it } }
        viewModelScope.launch { repository.startAgeFlow.collect { startAgeString = it } }
    }

    // ロジック:計算ボタンがタップされた時の処理
    fun onCalculate() {
        if (!validateInputs()) return

        val assets = nowAssets.toDouble()
        val reserve = monthlyReserve.toDouble()
        val rate = annualRatePercent.toDouble()
        val startAge = startAgeString.toIntOrNull() ?: 20

        fullProgression = calculateAssetProgression(startAge, assets, reserve, rate)

        updateChartModel()

        viewModelScope.launch {
            repository.saveSettings(nowAssets, monthlyReserve, annualRatePercent, startAgeString)
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

    fun validateInputs(): Boolean {
        var isValid = true

        // 現在の資産のチェック
        if (nowAssets.toDoubleOrNull() == null || nowAssets.isBlank()) {
            nowAssetsError = "有効な数値を入力してください"
            isValid = false
        } else {
            nowAssetsError = null
        }

        // 積立額のチェック
        if (monthlyReserve.toDoubleOrNull() == null || monthlyReserve.isBlank()) {
            monthlyReserveError = "有効な数値を入力してください"
            isValid = false
        } else {
            monthlyReserveError = null
        }

        // 年利のチェック
        if (annualRatePercent.toDoubleOrNull() == null || annualRatePercent.isBlank()) {
            annualRatePercentError = "有効な数値を入力してください"
            isValid = false
        } else {
            annualRatePercentError = null
        }

        return isValid
    }

}