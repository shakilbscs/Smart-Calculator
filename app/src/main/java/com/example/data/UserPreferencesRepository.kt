package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CalculatorId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("smart_calc_prefs", Context.MODE_PRIVATE)

    private val _favorites = MutableStateFlow<Set<String>>(loadFavorites())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _recentCalculators = MutableStateFlow<List<String>>(loadRecents())
    val recentCalculators: StateFlow<List<String>> = _recentCalculators.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _currencySymbol = MutableStateFlow(prefs.getString("currency_symbol", "$") ?: "$")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_done", false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private fun loadFavorites(): Set<String> {
        val raw = prefs.getStringSet("favorites", null)
        return raw ?: setOf(CalculatorId.PERCENTAGE.name, CalculatorId.DISCOUNT.name, CalculatorId.AGE.name)
    }

    private fun loadRecents(): List<String> {
        val raw = prefs.getString("recent_list", null) ?: return listOf(
            CalculatorId.DISCOUNT.name,
            CalculatorId.BMI.name,
            CalculatorId.AGE.name,
            CalculatorId.FUEL_COST.name
        )
        return raw.split(",").filter { it.isNotBlank() }
    }

    fun toggleFavorite(calcId: String) {
        val current = _favorites.value.toMutableSet()
        if (current.contains(calcId)) {
            current.remove(calcId)
        } else {
            current.add(calcId)
        }
        prefs.edit().putStringSet("favorites", current).apply()
        _favorites.value = current
    }

    fun recordCalculatorOpened(calcId: String) {
        val current = _recentCalculators.value.toMutableList()
        current.remove(calcId)
        current.add(0, calcId)
        val trimmed = current.take(6)
        prefs.edit().putString("recent_list", trimmed.joinToString(",")).apply()
        _recentCalculators.value = trimmed
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    fun setCurrencySymbol(symbol: String) {
        prefs.edit().putString("currency_symbol", symbol).apply()
        _currencySymbol.value = symbol
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_done", completed).apply()
        _onboardingCompleted.value = completed
    }
}
