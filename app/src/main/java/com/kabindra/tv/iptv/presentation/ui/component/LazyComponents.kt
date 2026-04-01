package com.kabindra.tv.iptv.presentation.ui.component

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import network.chaintech.sdpcomposemultiplatform.sdp
import kotlin.math.roundToInt

enum class BaseLazyLayout { List, Grid }
enum class BaseLazyOrientation { Vertical, Horizontal }
enum class BaseLazyPlatform { Android, AndroidTv }

@Immutable
data class TvLazyConfig(
    val enabled: Boolean = true,
    val requestInitialFocus: Boolean = true,
    val restoreFocus: Boolean = true,
    val initialFocusedIndex: Int = 0,
    val autoScrollOnFocus: Boolean = true,
    val focusedItemOffsetFraction: Float = 0.08f,
)

@Stable
class BaseLazyState internal constructor(
    val listState: LazyListState,
    val gridState: LazyGridState,
    private val lastFocusedIndexState: androidx.compose.runtime.MutableIntState,
    private val lastEndReachedCountState: androidx.compose.runtime.MutableIntState,
    private val hasHandledFocusRequestState: androidx.compose.runtime.MutableState<Boolean>,
) {
    var lastFocusedIndex: Int
        get() = lastFocusedIndexState.intValue
        internal set(value) {
            lastFocusedIndexState.intValue = value
        }

    internal var lastEndReachedCount: Int
        get() = lastEndReachedCountState.intValue
        set(value) {
            lastEndReachedCountState.intValue = value
        }

    internal var hasHandledFocusRequest: Boolean
        get() = hasHandledFocusRequestState.value
        set(value) {
            hasHandledFocusRequestState.value = value
        }
}

@Composable
fun rememberBaseLazyState(
    initialFirstVisibleItemIndex: Int = 0,
    initialFirstVisibleItemScrollOffset: Int = 0,
): BaseLazyState {
    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState(
            firstVisibleItemIndex = initialFirstVisibleItemIndex,
            firstVisibleItemScrollOffset = initialFirstVisibleItemScrollOffset
        )
    }
    val gridState = rememberSaveable(saver = LazyGridState.Saver) {
        LazyGridState(
            firstVisibleItemIndex = initialFirstVisibleItemIndex,
            firstVisibleItemScrollOffset = initialFirstVisibleItemScrollOffset
        )
    }
    val lastFocusedIndexState = rememberSaveable { mutableIntStateOf(-1) }
    val lastEndReachedCountState = rememberSaveable { mutableIntStateOf(-1) }
    val hasHandledFocusRequestState = rememberSaveable { mutableStateOf(false) }

    return remember(
        listState,
        gridState,
        lastFocusedIndexState,
        lastEndReachedCountState,
        hasHandledFocusRequestState
    ) {
        BaseLazyState(
            listState = listState,
            gridState = gridState,
            lastFocusedIndexState = lastFocusedIndexState,
            lastEndReachedCountState = lastEndReachedCountState,
            hasHandledFocusRequestState = hasHandledFocusRequestState
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T : Any> BaseLazy(
    items: List<T>,
    modifier: Modifier = Modifier.fillMaxSize(),
    state: BaseLazyState = rememberBaseLazyState(),
    layout: BaseLazyLayout = BaseLazyLayout.List,
    orientation: BaseLazyOrientation = BaseLazyOrientation.Vertical,
    platform: BaseLazyPlatform = BaseLazyPlatform.AndroidTv,
    tvConfig: TvLazyConfig = TvLazyConfig(),
    contentPadding: PaddingValues = PaddingValues(5.sdp),
    arrangement: Arrangement.HorizontalOrVertical = Arrangement.spacedBy(7.sdp),
    userScrollEnabled: Boolean = true,
    spanCount: Int = 2,
    isLoading: Boolean = false,
    endReachedThreshold: Int = 5,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null,
    onEndReached: (() -> Unit)? = null,
    key: ((index: Int, item: T) -> Any)? = null,
    contentType: ((index: Int, item: T) -> Any?)? = null,
    loadingContent: @Composable () -> Unit = { BaseLazyLoadingContent() },
    emptyContent: @Composable () -> Unit = { BaseLazyEmptyContent() },
    errorContent: @Composable (message: String, onRetry: (() -> Unit)?) -> Unit = { message, retry ->
        BaseLazyErrorContent(message = message, onRetry = retry)
    },
    onScrollStateChanged: ((Boolean) -> Unit)? = null,
    itemContent: @Composable (index: Int, item: T, itemModifier: Modifier) -> Unit,
) {
    if (items.isEmpty()) {
        state.lastEndReachedCount = -1
        state.lastFocusedIndex = -1
        state.hasHandledFocusRequest = false

        when {
            isLoading -> loadingContent()
            !errorMessage.isNullOrBlank() -> errorContent(errorMessage, onRetry)
            else -> emptyContent()
        }
        return
    }

    val tvSupport = rememberTvLazySupport(
        state = state,
        itemCount = items.size,
        layout = layout,
        platform = platform,
        tvConfig = tvConfig
    )

    ObserveScrollState(
        state = state,
        layout = layout,
        onScrollStateChanged = onScrollStateChanged
    )

    ObserveEndReached(
        state = state,
        layout = layout,
        isLoading = isLoading,
        threshold = endReachedThreshold,
        onEndReached = onEndReached
    )

    val itemKey: ((Int) -> Any)? = key?.let { keyProvider ->
        { index: Int -> keyProvider(index, items[index]) }
    }
    val itemContentType: (Int) -> Any? = { index: Int ->
        contentType?.invoke(index, items[index])
    }
    val containerModifier = modifier.then(tvSupport?.containerModifier ?: Modifier)

    if (layout == BaseLazyLayout.List && orientation == BaseLazyOrientation.Vertical) {
        LazyColumn(
            state = state.listState,
            modifier = containerModifier,
            contentPadding = contentPadding,
            verticalArrangement = arrangement,
            userScrollEnabled = userScrollEnabled
        ) {
            items(
                count = items.size,
                key = itemKey,
                contentType = itemContentType
            ) { index ->
                val itemModifier = rememberBaseLazyItemModifier(
                    index = index,
                    state = state,
                    layout = layout,
                    tvSupport = tvSupport,
                    tvConfig = tvConfig
                )
                itemContent(index, items[index], itemModifier)
            }

            if (isLoading) {
                item(contentType = "static_loading_footer") {
                    loadingContent()
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                item(contentType = "static_error_footer") {
                    errorContent(errorMessage, onRetry)
                }
            }
        }
        return
    }

    if (layout == BaseLazyLayout.List && orientation == BaseLazyOrientation.Horizontal) {
        LazyRow(
            state = state.listState,
            modifier = containerModifier,
            contentPadding = contentPadding,
            horizontalArrangement = arrangement,
            userScrollEnabled = userScrollEnabled
        ) {
            items(
                count = items.size,
                key = itemKey,
                contentType = itemContentType
            ) { index ->
                val itemModifier = rememberBaseLazyItemModifier(
                    index = index,
                    state = state,
                    layout = layout,
                    tvSupport = tvSupport,
                    tvConfig = tvConfig
                )
                itemContent(index, items[index], itemModifier)
            }

            if (isLoading) {
                item(contentType = "static_loading_footer") {
                    loadingContent()
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                item(contentType = "static_error_footer") {
                    errorContent(errorMessage, onRetry)
                }
            }
        }
        return
    }

    if (layout == BaseLazyLayout.Grid && orientation == BaseLazyOrientation.Vertical) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(spanCount),
            state = state.gridState,
            modifier = containerModifier,
            contentPadding = contentPadding,
            verticalArrangement = arrangement,
            horizontalArrangement = arrangement,
            userScrollEnabled = userScrollEnabled
        ) {
            items(
                count = items.size,
                key = itemKey,
                contentType = itemContentType
            ) { index ->
                val itemModifier = rememberBaseLazyItemModifier(
                    index = index,
                    state = state,
                    layout = layout,
                    tvSupport = tvSupport,
                    tvConfig = tvConfig
                )
                itemContent(index, items[index], itemModifier)
            }

            if (isLoading) {
                item(span = { GridItemSpan(maxLineSpan) }, contentType = "static_loading_footer") {
                    loadingContent()
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                item(span = { GridItemSpan(maxLineSpan) }, contentType = "static_error_footer") {
                    errorContent(errorMessage, onRetry)
                }
            }
        }
        return
    }

    LazyHorizontalGrid(
        rows = GridCells.Fixed(spanCount),
        state = state.gridState,
        modifier = containerModifier,
        contentPadding = contentPadding,
        verticalArrangement = arrangement,
        horizontalArrangement = arrangement,
        userScrollEnabled = userScrollEnabled
    ) {
        items(
            count = items.size,
            key = itemKey,
            contentType = itemContentType
        ) { index ->
            val itemModifier = rememberBaseLazyItemModifier(
                index = index,
                state = state,
                layout = layout,
                tvSupport = tvSupport,
                tvConfig = tvConfig
            )
            itemContent(index, items[index], itemModifier)
        }

        if (isLoading) {
            item(span = { GridItemSpan(maxLineSpan) }, contentType = "static_loading_footer") {
                loadingContent()
            }
        }

        if (!errorMessage.isNullOrBlank()) {
            item(span = { GridItemSpan(maxLineSpan) }, contentType = "static_error_footer") {
                errorContent(errorMessage, onRetry)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T : Any> BaseLazy(
    pagingItems: LazyPagingItems<T>,
    modifier: Modifier = Modifier.fillMaxSize(),
    state: BaseLazyState = rememberBaseLazyState(),
    layout: BaseLazyLayout = BaseLazyLayout.List,
    orientation: BaseLazyOrientation = BaseLazyOrientation.Vertical,
    platform: BaseLazyPlatform = BaseLazyPlatform.AndroidTv,
    tvConfig: TvLazyConfig = TvLazyConfig(),
    contentPadding: PaddingValues = PaddingValues(5.sdp),
    arrangement: Arrangement.HorizontalOrVertical = Arrangement.spacedBy(7.sdp),
    userScrollEnabled: Boolean = true,
    spanCount: Int = 2,
    loadingContent: @Composable () -> Unit = { BaseLazyLoadingContent() },
    emptyContent: @Composable () -> Unit = { BaseLazyEmptyContent() },
    errorContent: @Composable (message: String, onRetry: (() -> Unit)?) -> Unit = { message, retry ->
        BaseLazyErrorContent(message = message, onRetry = retry)
    },
    placeholderContent: @Composable (index: Int) -> Unit = { BaseLazyLoadingContent() },
    onScrollStateChanged: ((Boolean) -> Unit)? = null,
    key: ((item: T) -> Any)? = null,
    contentType: ((item: T) -> Any?)? = null,
    itemContent: @Composable (index: Int, item: T, itemModifier: Modifier) -> Unit,
) {
    val refreshState = pagingItems.loadState.refresh
    val appendState = pagingItems.loadState.append
    val itemCount = pagingItems.itemCount

    if (itemCount == 0) {
        state.lastEndReachedCount = -1
        state.lastFocusedIndex = -1
        state.hasHandledFocusRequest = false

        when (refreshState) {
            is LoadState.Loading -> loadingContent()
            is LoadState.Error -> errorContent(
                refreshState.error.message ?: "Unable to load items.",
                pagingItems::retry
            )

            is LoadState.NotLoading -> emptyContent()
        }
        return
    }

    val tvSupport = rememberTvLazySupport(
        state = state,
        itemCount = itemCount,
        layout = layout,
        platform = platform,
        tvConfig = tvConfig
    )

    ObserveScrollState(
        state = state,
        layout = layout,
        onScrollStateChanged = onScrollStateChanged
    )

    val itemKey = pagingItems.itemKey(key)
    val itemContentType = pagingItems.itemContentType(contentType)
    val containerModifier = modifier.then(tvSupport?.containerModifier ?: Modifier)

    if (layout == BaseLazyLayout.List && orientation == BaseLazyOrientation.Vertical) {
        LazyColumn(
            state = state.listState,
            modifier = containerModifier,
            contentPadding = contentPadding,
            verticalArrangement = arrangement,
            userScrollEnabled = userScrollEnabled
        ) {
            items(
                count = itemCount,
                key = itemKey,
                contentType = itemContentType
            ) { index ->
                val itemModifier = rememberBaseLazyItemModifier(
                    index = index,
                    state = state,
                    layout = layout,
                    tvSupport = tvSupport,
                    tvConfig = tvConfig
                )
                val item = pagingItems[index]
                if (item != null) {
                    itemContent(index, item, itemModifier)
                } else {
                    placeholderContent(index)
                }
            }

            AppendLoadStateContent(
                appendState = appendState,
                retry = pagingItems::retry,
                loadingContent = loadingContent,
                errorContent = errorContent
            )
        }
        return
    }

    if (layout == BaseLazyLayout.List && orientation == BaseLazyOrientation.Horizontal) {
        LazyRow(
            state = state.listState,
            modifier = containerModifier,
            contentPadding = contentPadding,
            horizontalArrangement = arrangement,
            userScrollEnabled = userScrollEnabled
        ) {
            items(
                count = itemCount,
                key = itemKey,
                contentType = itemContentType
            ) { index ->
                val itemModifier = rememberBaseLazyItemModifier(
                    index = index,
                    state = state,
                    layout = layout,
                    tvSupport = tvSupport,
                    tvConfig = tvConfig
                )
                val item = pagingItems[index]
                if (item != null) {
                    itemContent(index, item, itemModifier)
                } else {
                    placeholderContent(index)
                }
            }

            AppendLoadStateContent(
                appendState = appendState,
                retry = pagingItems::retry,
                loadingContent = loadingContent,
                errorContent = errorContent
            )
        }
        return
    }

    if (layout == BaseLazyLayout.Grid && orientation == BaseLazyOrientation.Vertical) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(spanCount),
            state = state.gridState,
            modifier = containerModifier,
            contentPadding = contentPadding,
            verticalArrangement = arrangement,
            horizontalArrangement = arrangement,
            userScrollEnabled = userScrollEnabled
        ) {
            items(
                count = itemCount,
                key = itemKey,
                contentType = itemContentType
            ) { index ->
                val itemModifier = rememberBaseLazyItemModifier(
                    index = index,
                    state = state,
                    layout = layout,
                    tvSupport = tvSupport,
                    tvConfig = tvConfig
                )
                val item = pagingItems[index]
                if (item != null) {
                    itemContent(index, item, itemModifier)
                } else {
                    placeholderContent(index)
                }
            }

            AppendGridLoadStateContent(
                appendState = appendState,
                retry = pagingItems::retry,
                loadingContent = loadingContent,
                errorContent = errorContent
            )
        }
        return
    }

    LazyHorizontalGrid(
        rows = GridCells.Fixed(spanCount),
        state = state.gridState,
        modifier = containerModifier,
        contentPadding = contentPadding,
        verticalArrangement = arrangement,
        horizontalArrangement = arrangement,
        userScrollEnabled = userScrollEnabled
    ) {
        items(
            count = itemCount,
            key = itemKey,
            contentType = itemContentType
        ) { index ->
            val itemModifier = rememberBaseLazyItemModifier(
                index = index,
                state = state,
                layout = layout,
                tvSupport = tvSupport,
                tvConfig = tvConfig
            )
            val item = pagingItems[index]
            if (item != null) {
                itemContent(index, item, itemModifier)
            } else {
                placeholderContent(index)
            }
        }

        AppendGridLoadStateContent(
            appendState = appendState,
            retry = pagingItems::retry,
            loadingContent = loadingContent,
            errorContent = errorContent
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.AppendLoadStateContent(
    appendState: LoadState,
    retry: () -> Unit,
    loadingContent: @Composable () -> Unit,
    errorContent: @Composable (message: String, onRetry: (() -> Unit)?) -> Unit,
) {
    when (appendState) {
        is LoadState.Loading -> item(contentType = "paging_append_loading") {
            loadingContent()
        }

        is LoadState.Error -> item(contentType = "paging_append_error") {
            errorContent(
                appendState.error.message ?: "Unable to load more items.",
                retry
            )
        }

        is LoadState.NotLoading -> Unit
    }
}

private fun androidx.compose.foundation.lazy.grid.LazyGridScope.AppendGridLoadStateContent(
    appendState: LoadState,
    retry: () -> Unit,
    loadingContent: @Composable () -> Unit,
    errorContent: @Composable (message: String, onRetry: (() -> Unit)?) -> Unit,
) {
    when (appendState) {
        is LoadState.Loading -> item(
            span = { GridItemSpan(maxLineSpan) },
            contentType = "paging_append_loading"
        ) {
            loadingContent()
        }

        is LoadState.Error -> item(
            span = { GridItemSpan(maxLineSpan) },
            contentType = "paging_append_error"
        ) {
            errorContent(
                appendState.error.message ?: "Unable to load more items.",
                retry
            )
        }

        is LoadState.NotLoading -> Unit
    }
}

@Composable
private fun BaseLazyLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.sdp),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator(
            modifier = Modifier.align(Alignment.Center),
            isCircular = true,
            useExpressive = true
        )
    }
}

@Composable
private fun BaseLazyEmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        TextComponent(
            text = "No items available.",
            type = TextType.Body,
            size = TextSize.Medium,
        )
    }
}

@Composable
private fun BaseLazyErrorContent(
    message: String,
    onRetry: (() -> Unit)?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.sdp, vertical = 10.sdp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {
            TextComponent(
                text = message,
                type = TextType.Body,
                size = TextSize.Medium,
                maxLines = 3,
            )

            onRetry?.let { retry ->
                ButtonComponent(
                    text = "Retry",
                    onClick = retry
                )
            }
        }
    }
}

@Composable
private fun ObserveScrollState(
    state: BaseLazyState,
    layout: BaseLazyLayout,
    onScrollStateChanged: ((Boolean) -> Unit)?,
) {
    if (onScrollStateChanged == null) return

    LaunchedEffect(state, layout, onScrollStateChanged) {
        snapshotFlow {
            when (layout) {
                BaseLazyLayout.List -> {
                    state.listState.firstVisibleItemIndex > 0 ||
                            state.listState.firstVisibleItemScrollOffset > 0
                }

                BaseLazyLayout.Grid -> {
                    state.gridState.firstVisibleItemIndex > 0 ||
                            state.gridState.firstVisibleItemScrollOffset > 0
                }
            }
        }
            .distinctUntilChanged()
            .collect { onScrollStateChanged(it) }
    }
}

@Composable
private fun ObserveEndReached(
    state: BaseLazyState,
    layout: BaseLazyLayout,
    isLoading: Boolean,
    threshold: Int,
    onEndReached: (() -> Unit)?,
) {
    if (onEndReached == null) return

    LaunchedEffect(state, layout, isLoading, threshold, onEndReached) {
        snapshotFlow {
            val totalItemsCount = when (layout) {
                BaseLazyLayout.List -> state.listState.layoutInfo.totalItemsCount
                BaseLazyLayout.Grid -> state.gridState.layoutInfo.totalItemsCount
            }
            val lastVisibleItemIndex = when (layout) {
                BaseLazyLayout.List -> state.listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                BaseLazyLayout.Grid -> state.gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            } ?: -1

            Triple(
                totalItemsCount,
                lastVisibleItemIndex,
                totalItemsCount > 0 &&
                        lastVisibleItemIndex >= (totalItemsCount - threshold) &&
                        !isLoading
            )
        }
            .distinctUntilChanged()
            .filter { it.third }
            .collect { (totalItemsCount, _, _) ->
                if (state.lastEndReachedCount != totalItemsCount) {
                    state.lastEndReachedCount = totalItemsCount
                    onEndReached()
                }
            }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun rememberTvLazySupport(
    state: BaseLazyState,
    itemCount: Int,
    layout: BaseLazyLayout,
    platform: BaseLazyPlatform,
    tvConfig: TvLazyConfig,
): TvLazySupport? {
    if (platform != BaseLazyPlatform.AndroidTv || !tvConfig.enabled || itemCount <= 0) {
        return null
    }

    if (state.lastFocusedIndex !in 0 until itemCount) {
        state.lastFocusedIndex = -1
    }

    val fallbackRequester = remember(tvConfig.initialFocusedIndex) { FocusRequester() }
    val focusRequesters = remember { mutableStateMapOf<Int, FocusRequester>() }

    LaunchedEffect(itemCount, layout, tvConfig, state.hasHandledFocusRequest) {
        if (!tvConfig.requestInitialFocus || state.hasHandledFocusRequest || itemCount == 0) {
            return@LaunchedEffect
        }

        val targetIndex = when {
            tvConfig.restoreFocus && state.lastFocusedIndex in 0 until itemCount -> state.lastFocusedIndex
            else -> tvConfig.initialFocusedIndex.coerceIn(0, itemCount - 1)
        }

        scrollItemIntoPosition(
            state = state,
            layout = layout,
            index = targetIndex,
            focusedItemOffsetFraction = tvConfig.focusedItemOffsetFraction,
            animate = false
        )

        repeat(12) {
            focusRequesters[targetIndex]?.let { requester ->
                requester.requestFocus()
                state.hasHandledFocusRequest = true
                return@LaunchedEffect
            }
            kotlinx.coroutines.yield()
        }

        state.hasHandledFocusRequest = true
    }

    return remember(fallbackRequester, focusRequesters) {
        TvLazySupport(
            containerModifier = Modifier
                .focusRestorer(fallbackRequester)
                .focusGroup(),
            fallbackRequester = fallbackRequester,
            focusRequesters = focusRequesters
        )
    }
}

@Composable
private fun rememberBaseLazyItemModifier(
    index: Int,
    state: BaseLazyState,
    layout: BaseLazyLayout,
    tvSupport: TvLazySupport?,
    tvConfig: TvLazyConfig,
): Modifier {
    if (tvSupport == null) return Modifier

    val coroutineScope = rememberCoroutineScope()
    val focusRequester =
        remember(index, tvSupport.fallbackRequester, tvConfig.initialFocusedIndex) {
            if (index == tvConfig.initialFocusedIndex) {
                tvSupport.fallbackRequester
            } else {
                FocusRequester()
            }
        }

    DisposableEffect(index, focusRequester, tvSupport.focusRequesters) {
        tvSupport.focusRequesters[index] = focusRequester
        onDispose {
            tvSupport.focusRequesters.remove(index)
        }
    }

    return Modifier
        .focusRequester(focusRequester)
        .onFocusChanged { focusState ->
            if (focusState.isFocused) {
                state.lastFocusedIndex = index
                if (tvConfig.autoScrollOnFocus) {
                    coroutineScope.launch {
                        scrollItemIntoPosition(
                            state = state,
                            layout = layout,
                            index = index,
                            focusedItemOffsetFraction = tvConfig.focusedItemOffsetFraction,
                            animate = true
                        )
                    }
                }
            }
        }
}

private suspend fun scrollItemIntoPosition(
    state: BaseLazyState,
    layout: BaseLazyLayout,
    index: Int,
    focusedItemOffsetFraction: Float,
    animate: Boolean,
) {
    val scrollOffset = when (layout) {
        BaseLazyLayout.List -> {
            val viewportSize =
                state.listState.layoutInfo.viewportEndOffset - state.listState.layoutInfo.viewportStartOffset
            (viewportSize * focusedItemOffsetFraction.coerceIn(0f, 1f)).roundToInt()
        }

        BaseLazyLayout.Grid -> {
            val viewportSize =
                state.gridState.layoutInfo.viewportEndOffset - state.gridState.layoutInfo.viewportStartOffset
            (viewportSize * focusedItemOffsetFraction.coerceIn(0f, 1f)).roundToInt()
        }
    }

    when (layout) {
        BaseLazyLayout.List -> {
            if (animate) {
                state.listState.animateScrollToItem(index = index, scrollOffset = scrollOffset)
            } else {
                state.listState.scrollToItem(index = index, scrollOffset = scrollOffset)
            }
        }

        BaseLazyLayout.Grid -> {
            if (animate) {
                state.gridState.animateScrollToItem(index = index, scrollOffset = scrollOffset)
            } else {
                state.gridState.scrollToItem(index = index, scrollOffset = scrollOffset)
            }
        }
    }
}

private data class TvLazySupport(
    val containerModifier: Modifier,
    val fallbackRequester: FocusRequester,
    val focusRequesters: MutableMap<Int, FocusRequester>,
)
