package com.example.xtreamplayer.ui

import android.util.Base64
import android.view.ViewGroup
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.xtreamplayer.data.Category
import com.example.xtreamplayer.data.EpgListing
import com.example.xtreamplayer.data.LiveStream
import com.example.xtreamplayer.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val LiveBlue = Color(0xFF1677FF)
private val LiveBlueLight = Color(0xFF20B7FF)
private val LiveBackground = Color(0xFF02060B)
private val LivePanel = Color(0xE609121D)
private val LivePanelLight = Color(0xFF0C1724)
private val LiveBorder = Color(0xFF1A3047)
private val LiveMuted = Color(0xFF8E9BAB)
private val LiveGreen = Color(0xFF36E39A)

private const val FAVORITES_CATEGORY = "__favorites__"

@Composable
fun LiveTvScreen(
    categories: List<Category>,
    streams: List<LiveStream>,
    vm: AppViewModel,
    initialLive: LiveStream? = null,
    onLivePlay: (String, LiveStream) -> Unit,
    onBack: () -> Unit
) {
    var selectedCategoryId by remember {
        mutableStateOf(
            initialLive?.category_id
                ?: categories.firstOrNull()?.category_id
        )
    }

    var selectedLive by remember {
        mutableStateOf(initialLive)
    }

    var searchText by remember {
        mutableStateOf("")
    }

    var globalSearchText by remember {
        mutableStateOf("")
    }

    val favoriteIds = vm.favoriteLiveIds

    val categoryStreams = remember(
        streams,
        selectedCategoryId,
        favoriteIds
    ) {
        when (selectedCategoryId) {
            FAVORITES_CATEGORY -> {
                streams.filter {
                    val id = it.stream_id
                    id != null && id in favoriteIds
                }
            }

            null -> streams

            else -> {
                streams.filter {
                    it.category_id == selectedCategoryId
                }
            }
        }
    }

    val filteredStreams = remember(
        streams,
        categoryStreams,
        searchText,
        globalSearchText
    ) {
        when {
            globalSearchText.isNotBlank() -> {
                streams.filter {
                    it.name
                        .orEmpty()
                        .contains(
                            globalSearchText,
                            ignoreCase = true
                        )
                }
            }

            searchText.isNotBlank() -> {
                categoryStreams.filter {
                    it.name
                        .orEmpty()
                        .contains(
                            searchText,
                            ignoreCase = true
                        )
                }
            }

            else -> categoryStreams
        }
    }

    val selectedUrl = remember(
        selectedLive,
        vm.auth,
        vm.credentials
    ) {
        selectedLive
            ?.stream_id
            ?.let { id ->
                vm.streamUrl(
                    type = "live",
                    id = id
                )
            }
    }

    /*
     * Se arriviamo qui dopo il fullscreen con un canale
     * già selezionato, ricarichiamo anche il suo EPG.
     */
    LaunchedEffect(initialLive) {
        if (initialLive != null) {
            selectedLive = initialLive
            selectedCategoryId =
                initialLive.category_id

            initialLive.stream_id?.let { id ->
                vm.loadShortEpg(id)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LiveBackground)
    ) {
        LiveBackgroundDecoration()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 22.dp,
                    vertical = 16.dp
                )
        ) {

            LiveTopBar(
                globalSearchText = globalSearchText,
                onGlobalSearchChanged = {
                    globalSearchText = it
                    if (it.isNotBlank()) {
                        searchText = ""
                    }
                },
                onBack = {
                    vm.clearEpg()
                    onBack()
                }
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

                LiveCategoriesPanel(
                    modifier = Modifier
                        .width(190.dp)
                        .fillMaxHeight(),
                    categories = categories,
                    selectedCategoryId =
                        selectedCategoryId,
                    favoriteCount =
                        favoriteIds.size,
                    onCategorySelected = {
                        categoryId ->

                        selectedCategoryId =
                            categoryId

                        searchText = ""
                        globalSearchText = ""

                        if (
                            selectedLive?.category_id !=
                            categoryId &&
                            categoryId !=
                            FAVORITES_CATEGORY
                        ) {
                            selectedLive = null
                            vm.clearEpg()
                        }

                        if (
                            categoryId ==
                            FAVORITES_CATEGORY &&
                            selectedLive?.stream_id
                                !in favoriteIds
                        ) {
                            selectedLive = null
                            vm.clearEpg()
                        }
                    }
                )

                LiveChannelListPanel(
                    modifier = Modifier
                        .width(315.dp)
                        .fillMaxHeight(),
                    streams = filteredStreams,
                    totalCount =
                        if (globalSearchText.isNotBlank()) {
                            filteredStreams.size
                        } else {
                            categoryStreams.size
                        },
                    selectedLive = selectedLive,
                    searchText = searchText,
                    onSearchChanged = {
                        searchText = it
                    },
                    onChannelClick = { stream ->

                        val id =
                            stream.stream_id
                                ?: return@LiveChannelListPanel

                        val url =
                            vm.streamUrl(
                                type = "live",
                                id = id
                            )
                                ?: return@LiveChannelListPanel

                        if (
                            selectedLive?.stream_id ==
                            id
                        ) {
                            /*
                             * Secondo click:
                             * fullscreen come prima.
                             */
                            onLivePlay(
                                url,
                                stream
                            )
                        } else {
                            /*
                             * Primo click:
                             * preview + EPG vero.
                             */
                            selectedLive = stream
                            vm.loadShortEpg(id)
                        }
                    }
                )

                LivePreviewPanel(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    stream = selectedLive,
                    streamUrl = selectedUrl,
                    isFavorite =
                        selectedLive
                            ?.stream_id
                            ?.let {
                                vm.isFavoriteLive(it)
                            }
                            ?: false,
                    epgListings =
                        vm.selectedEpg,
                    loadingEpg =
                        vm.loadingEpg,
                    epgError =
                        vm.epgError,
                    onToggleFavorite = {
                        selectedLive
                            ?.stream_id
                            ?.let {
                                vm.toggleFavoriteLive(it)
                            }
                    }
                )
            }
        }
    }
}

@Composable
private fun LiveTopBar(
    globalSearchText: String,
    onGlobalSearchChanged: (String) -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        var backFocused by remember { mutableStateOf(false) }
        val backScale by animateFloatAsState(
            targetValue = if (backFocused) 1.08f else 1f,
            label = "liveBackFocusScale"
        )

        Surface(
            modifier = Modifier
                .size(44.dp)
                .scale(backScale)
                .onFocusChanged { backFocused = it.isFocused }
                .focusable()
                .clickable(onClick = onBack),
            color = if (backFocused) Color(0xFF102942) else Color.Transparent,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                if (backFocused) 3.dp else 1.dp,
                if (backFocused) LiveBlueLight else Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "‹",
                    color = if (backFocused) LiveBlueLight else Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }

        Spacer(
            modifier = Modifier.width(4.dp)
        )

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text = "X",
                color = LiveBlue,
                fontSize = 28.sp,
                fontWeight =
                    FontWeight.Black
            )

            Text(
                text = "TREAM",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text = " PLAYER",
                color = LiveBlueLight,
                fontSize = 22.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(25.dp)
                .background(LiveBorder)
        )

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Text(
            text = "LIVE TV",
            color = LiveMuted,
            fontSize = 13.sp,
            fontWeight =
                FontWeight.SemiBold,
            letterSpacing = 1.5.sp
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        GlobalLiveSearchField(
            value = globalSearchText,
            onValueChange = onGlobalSearchChanged
        )

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Row(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(50)
                )
                .background(
                    Color(0x1F36E39A)
                )
                .padding(
                    horizontal = 13.dp,
                    vertical = 7.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(
                        LiveGreen,
                        CircleShape
                    )
            )

            Spacer(
                modifier = Modifier.width(7.dp)
            )

            Text(
                text = "VPN ATTIVA",
                color = LiveGreen,
                fontSize = 11.sp,
                fontWeight =
                    FontWeight.Bold,
                letterSpacing = 0.7.sp
            )
        }
    }
}

@Composable
private fun GlobalLiveSearchField(
    value: String,
    onValueChange: (String) -> Unit
) {
    var containerFocused by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }

    val textFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val scale by animateFloatAsState(
        targetValue = if (containerFocused || editing) 1.03f else 1f,
        label = "globalLiveSearchFocusScale"
    )

    LaunchedEffect(editing) {
        if (editing) {
            textFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Surface(
        modifier = Modifier
            .width(230.dp)
            .height(38.dp)
            .scale(scale)
            .onFocusChanged {
                containerFocused = it.isFocused
                if (!it.hasFocus) {
                    editing = false
                }
            }
            .onKeyEvent { event ->
                if (
                    !editing &&
                    event.type == KeyEventType.KeyUp &&
                    (event.key == Key.Enter ||
                     event.key == Key.NumPadEnter ||
                     event.key == Key.DirectionCenter)
                ) {
                    editing = true
                    true
                } else {
                    false
                }
            }
            .focusable(),
        color = if (containerFocused || editing) Color(0xFF102942) else Color(0xFF07101A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            if (containerFocused || editing) 3.dp else 1.dp,
            if (containerFocused || editing) LiveBlueLight else LiveBorder.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⌕",
                color = if (containerFocused || editing || value.isNotBlank()) LiveBlueLight else LiveMuted,
                fontSize = 20.sp
            )

            Spacer(Modifier.width(8.dp))

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(textFocusRequester)
                    .onFocusChanged {
                        if (!it.isFocused && editing) {
                            editing = false
                        }
                    },
                enabled = editing,
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 12.sp
                ),
                cursorBrush = SolidColor(LiveBlue),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = "Cerca in tutti i canali...",
                                color = if (containerFocused || editing) {
                                    Color.White.copy(alpha = 0.78f)
                                } else {
                                    LiveMuted.copy(alpha = 0.7f)
                                },
                                fontSize = 11.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (value.isNotEmpty()) {
                Text(
                    text = "×",
                    modifier = Modifier
                        .clickable { onValueChange("") }
                        .padding(4.dp),
                    color = if (containerFocused || editing) LiveBlueLight else LiveMuted,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun LiveCategoriesPanel(
    modifier: Modifier,
    categories: List<Category>,
    selectedCategoryId: String?,
    favoriteCount: Int,
    onCategorySelected: (String?) -> Unit
) {
    Surface(
        modifier = modifier,
        color = LivePanel,
        shape =
            RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            LiveBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {

            Text(
                text = "CATEGORIE",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight =
                    FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            CategoryRow(
                title = "Preferiti",
                selected =
                    selectedCategoryId ==
                    FAVORITES_CATEGORY,
                count = favoriteCount,
                onClick = {
                    onCategorySelected(
                        FAVORITES_CATEGORY
                    )
                }
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(LiveBorder)
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),
                verticalArrangement =
                    Arrangement.spacedBy(5.dp)
            ) {
                items(
                    items = categories,
                    key = {
                        it.category_id
                            ?: it.category_name
                            ?: it.hashCode()
                                .toString()
                    }
                ) { category ->

                    val id =
                        category.category_id

                    CategoryRow(
                        title =
                            category
                                .category_name
                                ?: "Categoria",
                        selected =
                            selectedCategoryId ==
                            id,
                        count = null,
                        onClick = {
                            onCategorySelected(id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    title: String,
    selected: Boolean,
    count: Int?,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.035f else 1f,
        label = "categoryFocusScale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(onClick = onClick),
        color = when {
            isFocused -> Color(0xFF102942)
            selected -> Color(0x261677FF)
            else -> Color.Transparent
        },
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            if (isFocused) 3.dp else if (selected) 1.dp else 0.dp,
            when {
                isFocused -> LiveBlueLight
                selected -> LiveBlue.copy(alpha = 0.7f)
                else -> Color.Transparent
            }
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isFocused) 7.dp else 6.dp)
                    .background(
                        if (isFocused || selected) LiveBlueLight else LiveMuted.copy(alpha = 0.55f),
                        CircleShape
                    )
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = if (isFocused || selected) Color.White else LiveMuted,
                fontSize = 12.sp,
                fontWeight = if (isFocused || selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (count != null) {
                Text(
                    text = count.toString(),
                    color = if (isFocused || selected) LiveBlueLight else LiveMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun LiveChannelListPanel(
    modifier: Modifier,
    streams: List<LiveStream>,
    totalCount: Int,
    selectedLive: LiveStream?,
    searchText: String,
    onSearchChanged: (String) -> Unit,
    onChannelClick: (LiveStream) -> Unit
) {
    Surface(
        modifier = modifier,
        color = LivePanel,
        shape =
            RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            LiveBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text = "CANALI",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight =
                        FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )

                Text(
                    text =
                        totalCount.toString(),
                    color = LiveMuted,
                    fontSize = 10.sp
                )
            }

            Spacer(
                modifier =
                    Modifier.height(11.dp)
            )

            LiveSearchField(
                value = searchText,
                onValueChange =
                    onSearchChanged
            )

            Spacer(
                modifier =
                    Modifier.height(11.dp)
            )

            if (streams.isEmpty()) {
                Box(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            if (
                                searchText
                                    .isNotBlank()
                            ) {
                                "Nessun canale trovato"
                            } else {
                                "Nessun canale disponibile"
                            },
                        color = LiveMuted,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier =
                        Modifier.fillMaxSize(),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            6.dp
                        )
                ) {
                    items(
                        items = streams,
                        key = {
                            it.stream_id
                                ?: it.hashCode()
                        }
                    ) { stream ->

                        ChannelRow(
                            stream = stream,
                            selected =
                                selectedLive
                                    ?.stream_id ==
                                stream.stream_id,
                            onClick = {
                                onChannelClick(
                                    stream
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveSearchField(
    value: String,
    onValueChange: (String) -> Unit
) {
    var containerFocused by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }

    val textFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val scale by animateFloatAsState(
        targetValue = if (containerFocused || editing) 1.025f else 1f,
        label = "liveSearchFocusScale"
    )

    LaunchedEffect(editing) {
        if (editing) {
            textFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .scale(scale)
            .onFocusChanged {
                containerFocused = it.isFocused
                if (!it.hasFocus) {
                    editing = false
                }
            }
            .onKeyEvent { event ->
                if (
                    !editing &&
                    event.type == KeyEventType.KeyUp &&
                    (event.key == Key.Enter ||
                     event.key == Key.NumPadEnter ||
                     event.key == Key.DirectionCenter)
                ) {
                    editing = true
                    true
                } else {
                    false
                }
            }
            .focusable(),
        color = if (containerFocused || editing) Color(0xFF102942) else Color(0xFF07101A),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            if (containerFocused || editing) 3.dp else 1.dp,
            if (containerFocused || editing) LiveBlueLight else LiveBorder.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⌕",
                color = if (containerFocused || editing) LiveBlueLight else LiveMuted,
                fontSize = 20.sp
            )

            Spacer(Modifier.width(8.dp))

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(textFocusRequester)
                    .onFocusChanged {
                        if (!it.isFocused && editing) {
                            editing = false
                        }
                    },
                enabled = editing,
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 12.sp
                ),
                cursorBrush = SolidColor(LiveBlue),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = "Cerca canale...",
                                color = if (containerFocused || editing) {
                                    Color.White.copy(alpha = 0.78f)
                                } else {
                                    LiveMuted.copy(alpha = 0.7f)
                                },
                                fontSize = 12.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (value.isNotEmpty()) {
                Text(
                    text = "×",
                    modifier = Modifier
                        .clickable { onValueChange("") }
                        .padding(4.dp),
                    color = if (containerFocused || editing) LiveBlueLight else LiveMuted,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ChannelRow(
    stream: LiveStream,
    selected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.025f else 1f,
        label = "channelFocusScale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(onClick = onClick),
        color = when {
            isFocused -> Color(0xFF102942)
            selected -> Color(0x261677FF)
            else -> LivePanelLight.copy(alpha = 0.55f)
        },
        shape = RoundedCornerShape(11.dp),
        border = BorderStroke(
            when {
                isFocused -> 3.dp
                selected -> 1.2.dp
                else -> 0.7.dp
            },
            when {
                isFocused -> LiveBlueLight
                selected -> LiveBlue
                else -> LiveBorder.copy(alpha = 0.6f)
            }
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                color = if (isFocused) Color(0xFF153454) else Color(0xFF101C29),
                shape = RoundedCornerShape(8.dp),
                border = if (isFocused) BorderStroke(1.dp, LiveBlue.copy(alpha = 0.7f)) else null
            ) {
                if (!stream.stream_icon.isNullOrBlank()) {
                    AsyncImage(
                        model = stream.stream_icon,
                        contentDescription = stream.name,
                        modifier = Modifier.fillMaxSize().padding(4.dp)
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stream.name?.take(1)?.uppercase() ?: "TV",
                            color = LiveBlueLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stream.name ?: "Canale",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = if (isFocused || selected) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = when {
                        selected -> "IN RIPRODUZIONE"
                        isFocused -> "PREMI OK"
                        else -> "LIVE"
                    },
                    color = if (isFocused || selected) LiveBlueLight else LiveMuted,
                    fontSize = 9.sp,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = 0.6.sp
                )
            }
            if (selected || isFocused) {
                Box(
                    modifier = Modifier
                        .size(if (isFocused) 8.dp else 7.dp)
                        .background(LiveBlueLight, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun LivePreviewPanel(
    modifier: Modifier,
    stream: LiveStream?,
    streamUrl: String?,
    isFavorite: Boolean,
    epgListings: List<EpgListing>,
    loadingEpg: Boolean,
    epgError: String?,
    onToggleFavorite: () -> Unit
) {
    Surface(
        modifier = modifier,
        color = LivePanel,
        shape =
            RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            LiveBorder
        )
    ) {
        if (
            stream == null ||
            streamUrl == null
        ) {
            EmptyLivePreview()
        } else {

            val currentProgram =
                findCurrentProgram(
                    epgListings
                )

            val nextProgram =
                findNextProgram(
                    epgListings,
                    currentProgram
                )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {

                LivePreviewPlayer(
                    url = streamUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Surface(
                        modifier =
                            Modifier.size(48.dp),
                        color =
                            Color(0xFF101C29),
                        shape =
                            RoundedCornerShape(
                                11.dp
                            )
                    ) {
                        if (
                            !stream.stream_icon
                                .isNullOrBlank()
                        ) {
                            AsyncImage(
                                model =
                                    stream.stream_icon,
                                contentDescription =
                                    stream.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(5.dp)
                            )
                        } else {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxSize(),
                                contentAlignment =
                                    Alignment.Center
                            ) {
                                Text(
                                    text = "TV",
                                    color =
                                        LiveBlueLight,
                                    fontSize = 13.sp,
                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.width(12.dp)
                    )

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {
                        Text(
                            text =
                                stream.name
                                    ?: "Canale Live",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight =
                                FontWeight.Bold,
                            maxLines = 1,
                            overflow =
                                TextOverflow.Ellipsis
                        )

                        Spacer(
                            modifier =
                                Modifier.height(3.dp)
                        )

                        Text(
                            text =
                                "Trasmissione in diretta",
                            color = LiveMuted,
                            fontSize = 10.sp
                        )
                    }

                    FavoriteButton(
                        isFavorite =
                            isFavorite,
                        onClick =
                            onToggleFavorite
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                EpgPanel(
                    loading = loadingEpg,
                    error = epgError,
                    current =
                        currentProgram,
                    next =
                        nextProgram
                )
            }
        }
    }
}

@Composable
private fun EpgPanel(
    loading: Boolean,
    error: String?,
    current: EpgListing?,
    next: EpgListing?
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color =
            Color(0xFF07101A),
        shape =
            RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            LiveBorder.copy(
                alpha = 0.65f
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 10.dp
            )
        ) {

            if (loading) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = LiveBlueLight
                    )

                    Spacer(
                        modifier =
                            Modifier.width(9.dp)
                    )

                    Text(
                        text =
                            "Caricamento EPG...",
                        color = LiveMuted,
                        fontSize = 11.sp
                    )
                }

                return@Column
            }

            if (
                error != null &&
                current == null
            ) {
                Text(
                    text = "EPG",
                    color = LiveBlueLight,
                    fontSize = 9.sp,
                    fontWeight =
                        FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    text =
                        "EPG non disponibile",
                    color = LiveMuted,
                    fontSize = 11.sp
                )

                return@Column
            }

            if (current == null) {
                Text(
                    text = "EPG",
                    color = LiveBlueLight,
                    fontSize = 9.sp,
                    fontWeight =
                        FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    text =
                        "Nessuna informazione disponibile per questo canale",
                    color = LiveMuted,
                    fontSize = 11.sp
                )

                return@Column
            }

            Text(
                text = "ORA IN ONDA",
                color = LiveBlueLight,
                fontSize = 9.sp,
                fontWeight =
                    FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text =
                    buildProgramLine(current),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight =
                    FontWeight.SemiBold,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )

            val description =
                decodeEpgText(
                    current.description
                )

            if (description.isNotBlank()) {
                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    text = description,
                    color = LiveMuted,
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            if (next != null) {

                Spacer(
                    modifier =
                        Modifier.height(9.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            LiveBorder.copy(
                                alpha = 0.6f
                            )
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text = "A SEGUIRE",
                    color = LiveMuted,
                    fontSize = 9.sp,
                    fontWeight =
                        FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(3.dp)
                )

                Text(
                    text =
                        buildProgramLine(next),
                    color =
                        Color.White.copy(
                            alpha = 0.9f
                        ),
                    fontSize = 11.sp,
                    fontWeight =
                        FontWeight.Medium,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun findCurrentProgram(
    listings: List<EpgListing>
): EpgListing? {

    if (listings.isEmpty()) {
        return null
    }

    val now =
        System.currentTimeMillis() / 1000L

    /*
     * Prima proviamo con i timestamp, che sono
     * il dato più affidabile.
     */
    val byTimestamp =
        listings.firstOrNull { item ->

            val start =
                item.start_timestamp
                    ?.toLongOrNull()

            val end =
                item.stop_timestamp
                    ?.toLongOrNull()

            start != null &&
            end != null &&
            now >= start &&
            now < end
        }

    if (byTimestamp != null) {
        return byTimestamp
    }

    /*
     * Alcuni server indicano esplicitamente
     * il programma corrente.
     */
    val byNowPlaying =
        listings.firstOrNull {
            it.now_playing == 1
        }

    if (byNowPlaying != null) {
        return byNowPlaying
    }

    /*
     * Fallback: il primo elemento restituito
     * dal short EPG.
     */
    return listings.firstOrNull()
}

private fun findNextProgram(
    listings: List<EpgListing>,
    current: EpgListing?
): EpgListing? {

    if (
        listings.isEmpty() ||
        current == null
    ) {
        return null
    }

    val index =
        listings.indexOf(current)

    if (
        index >= 0 &&
        index + 1 < listings.size
    ) {
        return listings[index + 1]
    }

    val currentEnd =
        current.stop_timestamp
            ?.toLongOrNull()
            ?: return null

    return listings
        .filter { item ->
            item.start_timestamp
                ?.toLongOrNull()
                ?.let {
                    it >= currentEnd
                }
                ?: false
        }
        .minByOrNull {
            it.start_timestamp
                ?.toLongOrNull()
                ?: Long.MAX_VALUE
        }
}

private fun buildProgramLine(
    item: EpgListing
): String {

    val title =
        decodeEpgText(item.title)
            .ifBlank {
                "Programma"
            }

    val start =
        formatEpgTime(
            item.start_timestamp,
            item.start
        )

    val end =
        formatEpgTime(
            item.stop_timestamp,
            item.end
        )

    return if (
        start.isNotBlank() &&
        end.isNotBlank()
    ) {
        "$start – $end  •  $title"
    } else {
        title
    }
}

private fun formatEpgTime(
    timestamp: String?,
    fallbackDate: String?
): String {

    timestamp
        ?.toLongOrNull()
        ?.let { seconds ->

            return try {
                SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(
                    Date(
                        seconds * 1000L
                    )
                )
            } catch (
                _: Exception
            ) {
                ""
            }
        }

    if (
        fallbackDate.isNullOrBlank()
    ) {
        return ""
    }

    val formats =
        listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm"
        )

    formats.forEach { pattern ->

        try {
            val parser =
                SimpleDateFormat(
                    pattern,
                    Locale.US
                )

            val date =
                parser.parse(
                    fallbackDate
                )

            if (date != null) {
                return SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(date)
            }

        } catch (
            _: Exception
        ) {
        }
    }

    return ""
}

/*
 * Molti server Xtream restituiscono titolo e descrizione
 * EPG codificati in Base64. Altri restituiscono testo normale.
 *
 * Questa funzione supporta entrambi senza mostrare stringhe
 * incomprensibili all'utente.
 */
private fun decodeEpgText(
    value: String?
): String {

    if (value.isNullOrBlank()) {
        return ""
    }

    val clean =
        value.trim()

    return try {

        val decoded =
            String(
                Base64.decode(
                    clean,
                    Base64.DEFAULT
                ),
                Charsets.UTF_8
            ).trim()

        /*
         * Evitiamo di interpretare accidentalmente
         * del normale testo come Base64.
         */
        if (
            decoded.isNotBlank() &&
            decoded.none {
                it == '\uFFFD'
            }
        ) {
            decoded
        } else {
            clean
        }

    } catch (
        _: Exception
    ) {
        clean
    }
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        label = "favoriteFocusScale"
    )

    Surface(
        modifier = Modifier
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(onClick = onClick),
        color = when {
            isFocused -> Color(0xFF102942)
            isFavorite -> Color(0x261677FF)
            else -> Color.Transparent
        },
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            if (isFocused) 3.dp else 1.dp,
            when {
                isFocused -> LiveBlueLight
                isFavorite -> LiveBlue
                else -> LiveBorder
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isFavorite) "★" else "☆",
                color = if (isFocused || isFavorite) LiveBlueLight else Color.White,
                fontSize = 15.sp
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = if (isFavorite) "RIMUOVI DAI PREFERITI" else "AGGIUNGI AI PREFERITI",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptyLivePreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Surface(
                modifier =
                    Modifier.size(72.dp),
                color =
                    Color(0x201677FF),
                shape = CircleShape,
                border = BorderStroke(
                    1.dp,
                    LiveBlue.copy(
                        alpha = 0.5f
                    )
                )
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        color =
                            LiveBlueLight,
                        fontSize = 27.sp
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )

            Text(
                text =
                    "Seleziona un canale",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(7.dp)
            )

            Text(
                text =
                    "Il canale verrà riprodotto qui.\nPremilo una seconda volta per lo schermo intero.",
                color = LiveMuted,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun LivePreviewPlayer(
    url: String,
    modifier: Modifier = Modifier
) {
    val context =
        androidx.compose.ui.platform
            .LocalContext.current

    val player = remember(url) {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                setMediaItem(
                    MediaItem.fromUri(url)
                )

                prepare()
                playWhenReady = true
            }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    AndroidView(
        modifier = modifier
            .clip(
                RoundedCornerShape(14.dp)
            )
            .background(Color.Black),
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player

                useController = true

                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams
                            .MATCH_PARENT,
                        ViewGroup.LayoutParams
                            .MATCH_PARENT
                    )
            }
        },
        update = {
            it.player = player
        }
    )
}

@Composable
private fun LiveBackgroundDecoration() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0x241677FF),
                        Color.Transparent
                    ),
                    radius = 850f
                )
            )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x66000000)
                    )
                )
            )
    )
}
