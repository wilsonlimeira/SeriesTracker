package com.example.tvserieswatcher

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Extension property for DataStore
val Application.dataStore: DataStore<Preferences> by preferencesDataStore(name = "series_datastore")

@Serializable
data class TVSeries(
    val id: Int = 0,
    val name: String,
    val totalSeasons: Int,
    val episodesPerSeason: Int? = null,
    val seasonEpisodeMap: Map<Int, Int>? = null,
    val currentSeason: Int = 1,
    val currentEpisode: Int = 1,
    val isCompleted: Boolean = false
)

class SeriesViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.dataStore
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    private val SERIES_KEY = stringPreferencesKey("series_data")
    private val THEME_KEY = booleanPreferencesKey("is_dark_theme")

    private val _series = MutableStateFlow<List<TVSeries>>(emptyList())
    val series: StateFlow<List<TVSeries>> = _series.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private var nextId = 1

    init {
        viewModelScope.launch {
            dataStore.data
                .map { preferences ->
                    // Load series
                    val seriesJson = preferences[SERIES_KEY] ?: ""
                    if (seriesJson.isNotEmpty()) {
                        val loadedSeries = json.decodeFromString<List<TVSeries>>(seriesJson)
                        _series.value = loadedSeries
                        nextId = (loadedSeries.maxOfOrNull { it.id } ?: 0) + 1
                    }

                    // Load theme
                    _isDarkTheme.value = preferences[THEME_KEY] ?: false
                }
                .first() // Get initial values
        }
    }

    private fun saveSeries() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                val seriesJson = json.encodeToString(_series.value)
                preferences[SERIES_KEY] = seriesJson
            }
        }
    }

    private fun saveThemePreference(isDark: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[THEME_KEY] = isDark
            }
        }
    }

    fun addSeries(series: TVSeries) {
        val newSeries = series.copy(id = nextId++)
        _series.value += newSeries
        saveSeries()
    }

    fun addSeriesWithCustomSeasons(name: String, totalSeasons: Int, seasonsEpisodeMap: Map<Int, Int>) {
        val newSeries = TVSeries(
            id = nextId++,
            name = name,
            totalSeasons = totalSeasons,
            seasonEpisodeMap = seasonsEpisodeMap,
            episodesPerSeason = null
        )
        _series.value += newSeries
        saveSeries()
    }

    fun incrementEpisode(series: TVSeries) {
        val seriesList = _series.value.toMutableList()
        val seriesIndex = seriesList.indexOfFirst { it.id == series.id }

        if (seriesIndex != -1) {
            val currentSeries = seriesList[seriesIndex]

            var newEpisode = currentSeries.currentEpisode + 1
            var newSeason = currentSeries.currentSeason
            var completed = false

            val episodesInCurrentSeason = if (currentSeries.seasonEpisodeMap != null) {
                currentSeries.seasonEpisodeMap[currentSeries.currentSeason] ?: 0
            } else {
                currentSeries.episodesPerSeason ?: 0
            }

            if (newEpisode > episodesInCurrentSeason) {
                newEpisode = 1
                newSeason++

                if (newSeason > currentSeries.totalSeasons) {
                    newSeason = currentSeries.totalSeasons
                    newEpisode = episodesInCurrentSeason
                    completed = true
                }
            }

            seriesList[seriesIndex] = currentSeries.copy(
                currentSeason = newSeason,
                currentEpisode = newEpisode,
                isCompleted = completed
            )

            _series.value = seriesList
            saveSeries()
        }
    }

    fun decrementEpisode(series: TVSeries) {
        val seriesList = _series.value.toMutableList()
        val seriesIndex = seriesList.indexOfFirst { it.id == series.id }

        if (seriesIndex != -1) {
            val currentSeries = seriesList[seriesIndex]

            // Calculate previous episode/season
            var newEpisode = currentSeries.currentEpisode - 1
            var newSeason = currentSeries.currentSeason

            // Check if we need to move to the previous season
            if (newEpisode < 1) {
                newSeason--

                // If we go below season 1, stay at episode 1 of season 1
                if (newSeason < 1) {
                    newSeason = 1
                    newEpisode = 1
                } else {
                    // Get episodes in the previous season
                    newEpisode = if (currentSeries.seasonEpisodeMap != null) {
                        currentSeries.seasonEpisodeMap[newSeason] ?: 0
                    } else {
                        currentSeries.episodesPerSeason ?: 0
                    }
                }
            }

            // Update the series
            seriesList[seriesIndex] = currentSeries.copy(
                currentSeason = newSeason,
                currentEpisode = newEpisode,
                isCompleted = false  // If we're decrementing, we're no longer completed
            )

            _series.value = seriesList
            saveSeries()
        }
    }

    fun deleteSeries(series: TVSeries) {
        _series.value = _series.value.filter { it.id != series.id }
        saveSeries()
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
        saveThemePreference(_isDarkTheme.value)
    }
}
