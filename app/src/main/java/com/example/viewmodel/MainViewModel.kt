package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HistoryEntity
import com.example.data.UserPreferencesRepository
import com.example.model.CalculatorCategory
import com.example.model.CalculatorId
import com.example.model.CalculatorInfo
import com.example.model.CalculatorRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val historyDao = db.historyDao()
    private val userPrefs = UserPreferencesRepository(application)

    val favorites: StateFlow<Set<String>> = userPrefs.favorites
    val themeMode: StateFlow<String> = userPrefs.themeMode
    val currencySymbol: StateFlow<String> = userPrefs.currencySymbol
    val onboardingCompleted: StateFlow<Boolean> = userPrefs.onboardingCompleted

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<CalculatorCategory?>(null)
    val selectedCategoryFilter: StateFlow<CalculatorCategory?> = _selectedCategoryFilter.asStateFlow()

    private val _copyModalMessage = MutableStateFlow<String?>(null)
    val copyModalMessage: StateFlow<String?> = _copyModalMessage.asStateFlow()

    val historyList: StateFlow<List<HistoryEntity>> = historyDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentCalculators: StateFlow<List<CalculatorInfo>> = userPrefs.recentCalculators
        .combine(MutableStateFlow(Unit)) { recents, _ ->
            recents.mapNotNull { name ->
                try {
                    val id = CalculatorId.valueOf(name)
                    CalculatorRegistry.getById(id)
                } catch (e: Exception) {
                    null
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredCalculators: StateFlow<List<CalculatorInfo>> = combine(
        _searchQuery,
        _selectedCategoryFilter
    ) { query, catFilter ->
        val cleanQuery = query.trim().lowercase()
        CalculatorRegistry.allCalculators.filter { item ->
            val matchesCategory = catFilter == null || item.category == catFilter
            val matchesSearch = cleanQuery.isEmpty() ||
                    item.title.lowercase().contains(cleanQuery) ||
                    item.subtitle.lowercase().contains(cleanQuery) ||
                    item.keywords.any { it.contains(cleanQuery) }
            matchesCategory && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalculatorRegistry.allCalculators)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryFilterSelected(category: CalculatorCategory?) {
        _selectedCategoryFilter.value = if (_selectedCategoryFilter.value == category) null else category
    }

    fun toggleFavorite(calcId: CalculatorId) {
        userPrefs.toggleFavorite(calcId.name)
    }

    fun isFavorite(calcId: CalculatorId): Boolean {
        return favorites.value.contains(calcId.name)
    }

    fun onCalculatorOpened(calcId: CalculatorId) {
        userPrefs.recordCalculatorOpened(calcId.name)
    }

    fun saveHistory(calcId: CalculatorId, inputSummary: String, resultSummary: String) {
        val info = CalculatorRegistry.getById(calcId)
        // Privacy rule: Do not save sensitive health inputs unless explicitly chosen
        if (info.isHealth) return

        viewModelScope.launch {
            historyDao.insert(
                HistoryEntity(
                    calculatorId = calcId.name,
                    title = info.title,
                    inputSummary = inputSummary,
                    resultSummary = resultSummary,
                    categoryName = info.category.title
                )
            )
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            historyDao.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyDao.clearAll()
        }
    }

    fun setThemeMode(mode: String) {
        userPrefs.setThemeMode(mode)
    }

    fun setCurrencySymbol(symbol: String) {
        userPrefs.setCurrencySymbol(symbol)
    }

    fun completeOnboarding() {
        userPrefs.setOnboardingCompleted(true)
    }

    fun showCopyModal(msg: String) {
        _copyModalMessage.value = msg
    }

    fun dismissCopyModal() {
        _copyModalMessage.value = null
    }
}
