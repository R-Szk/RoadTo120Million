package com.example.roadto120million.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.roadto120million.AssetViewModel
import com.example.roadto120million.CalculationInputField

@Composable
fun SettingsView(
    viewModel: AssetViewModel,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("設定", style = MaterialTheme.typography.headlineMedium)

        // 年齢設定の入力欄
        CalculationInputField(
            label = "現在の年齢",
            value = viewModel.startAgeString,
            unit = "歳",
            onValueChange = { viewModel.startAgeString = it }
        )

        Button(onClick = onBack) {
            Text("保存して戻る")
        }
    }
}