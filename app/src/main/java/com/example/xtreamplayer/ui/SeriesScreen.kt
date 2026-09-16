package com.example.xtreamplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.xtreamplayer.data.*
import com.example.xtreamplayer.viewmodel.AppViewModel

private val SeriesAccent = Color(0xFFCAEA00)

@Composable
fun SeriesContentScreen(
    title: String,
    categories: List<Category>,
    series: List<SeriesStream>,
    vm: AppViewModel,
    onPlay: (String) -> Unit,
    onBack: () -> Unit
) {

    var selectedCategoryId by remember {
        mutableStateOf<String?>(null)
    }

    var selectedSeries by remember {
        mutableStateOf<SeriesStream?>(null)
    }

    val filteredSeries = if (selectedCategoryId == null) {
        series
    } else {
        series.filter {
            it.category_id == selectedCategoryId
        }
    }

    if (selectedSeries == null) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090909))
                .padding(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = onBack
                ) {
                    Text("← INDIETRO")
                }

                Spacer(
                    modifier = Modifier.width(18.dp)
                )

                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            Row(
                modifier = Modifier.fillMaxSize()
            ) {

                SeriesCategorySidebar(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = {
                        selectedCategoryId = it
                    }
                )

                Spacer(
                    modifier = Modifier.width(22.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {

                    if (filteredSeries.isEmpty()) {

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                "Nessuna serie disponibile.",
                                color = MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                            )
                        }

                    } else {

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(
                                minSize = 170.dp
                            ),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement =
                                Arrangement.spacedBy(20.dp),
                            horizontalArrangement =
                                Arrangement.spacedBy(18.dp),
                            contentPadding =
                                PaddingValues(bottom = 30.dp)
                        ) {

                            items(filteredSeries) { show ->

                                SeriesPosterCard(
                                    series = show,
                                    onClick = {

                                        show.series_id?.let { id ->

                                            vm.loadSeriesInfo(id)
                                            selectedSeries = show
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

    } else {

        SeriesDetailsScreen(
            series = selectedSeries!!,
            info = vm.selectedSeriesInfo,
            loading = vm.loadingSeriesInfo,
            vm = vm,
            onPlay = onPlay,
            onBack = {
                vm.clearSeriesInfo()
                selectedSeries = null
            }
        )
    }
}

@Composable
private fun SeriesCategorySidebar(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit
) {

    Column(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
    ) {

        SeriesSidebarItem(
            name = "TUTTI",
            selected = selectedCategoryId == null,
            onClick = {
                onCategorySelected(null)
            }
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        categories.forEach { category ->

            val id = category.category_id ?: return@forEach

            SeriesSidebarItem(
                name = category.category_name ?: "Categoria",
                selected = selectedCategoryId == id,
                onClick = {
                    onCategorySelected(id)
                }
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )
        }
    }
}

@Composable
private fun SeriesSidebarItem(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(
                RoundedCornerShape(10.dp)
            )
            .background(
                if (selected) {
                    SeriesAccent
                } else {
                    Color(0xFF151515)
                }
            )
            .clickable {
                onClick()
            }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {

        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = if (selected) {
                FontWeight.ExtraBold
            } else {
                FontWeight.Medium
            },
            color = if (selected) {
                Color.Black
            } else {
                Color.White
            },
            maxLines = 1
        )
    }
}

@Composable
private fun SeriesPosterCard(
    series: SeriesStream,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151515)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {

        Column {

            if (!series.cover.isNullOrBlank()) {

                AsyncImage(
                    model = series.cover,
                    contentDescription = series.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(245.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp
                            )
                        ),
                    contentScale = ContentScale.Crop
                )

            } else {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(245.dp)
                        .background(Color(0xFF202020)),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        "NESSUNA IMMAGINE",
                        color = MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier.padding(14.dp)
            ) {

                Text(
                    text = series.name ?: "Serie",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                if (!series.rating.isNullOrBlank()) {

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = "★ ${series.rating}",
                        fontSize = 13.sp,
                        color = SeriesAccent
                    )
                }
            }
        }
    }
}
