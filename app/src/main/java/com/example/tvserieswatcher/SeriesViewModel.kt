package com.example.tvserieswatcher

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TVSeries(
    val id: Int = 0,
    val name: String,
    val totalSeasons: Int,
    val episodesPerSeason: Int,
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
        _series.value = _series.value + newSeries
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
            if (newEpisode > currentSeries.episodesPerSeason) {
                newEpisode = 1
                newSeason++

                // Check if we've completed all seasons
                if (newSeason > currentSeries.totalSeasons) {
                    newSeason = currentSeries.totalSeasons
                    newEpisode = currentSeries.episodesPerSeason
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