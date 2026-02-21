package com.example.roadto120million

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Contextの拡張プロパティとしてDataStoreを定義
private val Context.dataStore by preferencesDataStore(name = "settings")
class AssetRepository(private val context: Context) {
    // 保存するためのキーを定義
    private companion object {
        // 現在の資産、毎月の積立額、年利
        val KEY_NOW_ASSETS = stringPreferencesKey("now_assets")
        val KEY_MONTHLY_RESERVE = stringPreferencesKey("monthly_reserve")
        val KEY_ANNUAL_RATE = stringPreferencesKey("annual_rate")
        val KEY_START_AGE = stringPreferencesKey("start_age")
    }

    // データを保存する関数
    suspend fun saveSettings(nowAssets: String, monthlyReserve: String, annualRate: String, startAge: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOW_ASSETS] = nowAssets
            preferences[KEY_MONTHLY_RESERVE] = monthlyReserve
            preferences[KEY_ANNUAL_RATE] = annualRate
            preferences[KEY_START_AGE] = startAge
        }
    }

    // データを読み出す
    val nowAssetsFlow: Flow<String> = context.dataStore.data.map { it[KEY_NOW_ASSETS] ?: "" }
    val monthlyReserveFlow: Flow<String> = context.dataStore.data.map { it[KEY_MONTHLY_RESERVE] ?: "" }
    val annualRateFlow: Flow<String> = context.dataStore.data.map { it[KEY_ANNUAL_RATE] ?: "" }
    val startAgeFlow: Flow<String> = context.dataStore.data.map { it[KEY_START_AGE] ?: "" }
}