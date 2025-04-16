package com.example.tvserieswatcher

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TVSeries(
    val id: Int = 0,
    val name: String,
    val totalSeasons: Int,
    val episodesPerSeason: Int? = null,  // Optional for regular series
    val seasonEpisodeMap: Map<Int, Int>? = null,  // For series with different episodes per season
    val currentSeason: Int = 1,
    val currentEpisode: Int = 1,
    val isCompleted: Boolean = false
)

class SeriesViewModel : ViewModel() {
    private val _series = MutableStateFlow<List<TVSeries>>(emptyList())
    val series: StateFlow<List<TVSeries>> = _series.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private var nextId = 1

    fun addSeries(series: TVSeries) {
        val newSeries = series.copy(id = nextId++)
        _series.value += newSeries
    }

    fun addSeriesWithCustomSeasons(name: String, totalSeasons: Int, seasonsEpisodeMap: Map<Int, Int>) {
        val newSeries = TVSeries(
            id = nextId++,
            name = name,
            totalSeasons = totalSeasons,
            seasonEpisodeMap = seasonsEpisodeMap,
            episodesPerSeason = null  // Not used for custom seasons
        )
        _series.value += newSeries
    }

    fun incrementEpisode(series: TVSeries) {
        val seriesList = _series.value.toMutableList()
        val seriesIndex = seriesList.indexOfFirst { it.id == series.id }

        if (seriesIndex != -1) {
            val currentSeries = seriesList[seriesIndex]

            // Calculate next episode/season
            var newEpisode = currentSeries.currentEpisode + 1
            var newSeason = currentSeries.currentSeason
            var completed = false

            // Check if we need to move to the next season
            val episodesInCurrentSeason = if (currentSeries.seasonEpisodeMap != null) {
                currentSeries.seasonEpisodeMap[currentSeries.currentSeason] ?: 0
            } else {
                currentSeries.episodesPerSeason ?: 0
            }

            if (newEpisode > episodesInCurrentSeason) {
                newEpisode = 1
                newSeason++

                // Check if we've completed all seasons
                if (newSeason > currentSeries.totalSeasons) {
                    newSeason = currentSeries.totalSeasons
                    newEpisode = episodesInCurrentSeason
                    completed = true
                }
            }

            // Update the series
            seriesList[seriesIndex] = currentSeries.copy(
                currentSeason = newSeason,
                currentEpisode = newEpisode,
                isCompleted = completed
            )

            _series.value = seriesList
        }
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }
}