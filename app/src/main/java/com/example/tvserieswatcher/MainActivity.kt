package com.example.tvserieswatcher

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tvserieswatcher.ui.theme.SeriesTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: SeriesViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState(initial = false)

            SeriesTrackerTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TVSeriesTrackerApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun TVSeriesTrackerApp(viewModel: SeriesViewModel) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState(initial = false)
    val series by viewModel.series.collectAsState(initial = emptyList())

    var seriesName by remember { mutableStateOf("") }
    var seasons by remember { mutableStateOf("") }
    var episodesPerSeason by remember { mutableStateOf("") }
    var isManualMode by remember { mutableStateOf(false) }
    var customSeasons by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var episodesInCurrentSeason by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TV Series Tracker",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { viewModel.toggleTheme() }) {
                Icon(
                    painter = painterResource(
                        id = if (isDarkTheme) R.drawable.baseline_dark_mode_24_white
                        else R.drawable.baseline_dark_mode_24
                    ),

                    contentDescription = "Toggle theme"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add new series form
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Add New Series",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = seriesName,
                    onValueChange = { seriesName = it },
                    label = { Text("Series Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = seasons,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d+$"))) {
                            seasons = it
                        }
                    },
                    label = { Text("Number of Seasons") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = episodesPerSeason,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d+$"))) {
                            episodesPerSeason = it
                        }
                    },
                    label = { Text("Episodes per Season") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isManualMode,
                        onCheckedChange = {
                            isManualMode = it
                            if (!it) {
                                customSeasons = emptyMap()
                            }
                        }
                    )
                    Text("Manually add episodes per season")
                }

                if (isManualMode && seasons.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        // Display added seasons
                        customSeasons.forEach { (season, episodes) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Season $season: $episodes episodes")
                                IconButton(onClick = {
                                    customSeasons = customSeasons - season
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove season")
                                }
                            }
                        }

                        // Add new season if not all seasons are added
                        if (customSeasons.size < (seasons.toIntOrNull() ?: 0)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Season ${customSeasons.size + 1}: ", fontWeight = FontWeight.Medium)

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedTextField(
                                    value = episodesInCurrentSeason,
                                    onValueChange = {
                                        if (it.isEmpty() || it.matches(Regex("^\\d+$"))) {
                                            episodesInCurrentSeason = it
                                        }
                                    },
                                    label = { Text("Episodes") },
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = {
                                        if (episodesInCurrentSeason.isNotEmpty()) {
                                            val currentSeasonNumber = customSeasons.size + 1
                                            customSeasons = customSeasons + (currentSeasonNumber to episodesInCurrentSeason.toInt())
                                            episodesInCurrentSeason = ""
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add season")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (seriesName.isNotBlank() && seasons.isNotBlank()) {
                            if (isManualMode) {
                                // For manual mode, use the custom seasons map
                                if (customSeasons.isNotEmpty()) {
                                    viewModel.addSeriesWithCustomSeasons(
                                        name = seriesName,
                                        totalSeasons = seasons.toInt(),
                                        seasonsEpisodeMap = customSeasons
                                    )
                                }
                            } else {
                                // For non-manual mode, use the same episodes per season
                                if (episodesPerSeason.isNotBlank()) {
                                    viewModel.addSeries(
                                        TVSeries(
                                            name = seriesName,
                                            totalSeasons = seasons.toInt(),
                                            episodesPerSeason = episodesPerSeason.toInt()
                                        )
                                    )
                                }
                            }

                            // Clear all fields
                            seriesName = ""
                            seasons = ""
                            episodesPerSeason = ""
                            isManualMode = false
                            customSeasons = emptyMap()
                            episodesInCurrentSeason = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Add Series")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your Series",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Series list
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(series) { tvSeries ->
                SeriesItem(
                    series = tvSeries,
                    onIncrementEpisode = { viewModel.incrementEpisode(tvSeries) },
                    onDecrementEpisode = { viewModel.decrementEpisode(tvSeries) },
                    onDeleteSeries = { viewModel.deleteSeries(tvSeries) }
                )
            }
        }
    }
}

@Composable
fun SeriesItem(
    series: TVSeries,
    onIncrementEpisode: () -> Unit,
    onDecrementEpisode: () -> Unit,
    onDeleteSeries: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = series.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Season ${series.currentSeason}, Episode ${series.currentEpisode}",
                    fontSize = 14.sp
                )

                val episodeCount = if (series.seasonEpisodeMap != null) {
                    series.seasonEpisodeMap.values.sum()
                } else {
                    series.totalSeasons * (series.episodesPerSeason ?: 0)
                }

                Text(
                    text = "Total: ${series.totalSeasons} seasons, $episodeCount episodes",
                    fontSize = 12.sp
                )

                if (series.isCompleted) {
                    Text(
                        text = "Completed!",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrement button
                IconButton(onClick = onDecrementEpisode) {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.baseline_remove_circle_24
                        ),
                        contentDescription = "Previous episode",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                // Increment button
                IconButton(onClick = onIncrementEpisode) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Next episode",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Delete button - shows only when series is completed
                if (series.isCompleted) {
                    IconButton(onClick = onDeleteSeries) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete series",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "TV Series Tracker App Preview")
@Composable
fun TVSeriesTrackerAppPreview() {
    val mockViewModel: SeriesViewModel = SeriesViewModel(application = Application()).apply {
        addSeries(TVSeries(8, "Game of Thrones", 8, 10))
        addSeries(TVSeries(5, "Breaking Bad", 5, 16))
        addSeries(TVSeries(9, "The Office", 9, 24))
    }
    val isDarkTheme by mockViewModel.isDarkTheme.collectAsState(initial = false)

    SeriesTrackerTheme(darkTheme = isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            TVSeriesTrackerApp(mockViewModel)
        }
    }
}