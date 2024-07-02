/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.wear.compose.material3

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.OnFocusChange
import androidx.wear.compose.foundation.ScrollInfoProvider
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.toScrollAwayInfoProvider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * [ScreenScaffold] is one of the Wear Material3 scaffold components.
 *
 * The scaffold components [AppScaffold] and [ScreenScaffold] lay out the structure of a screen and
 * coordinate transitions of the [ScrollIndicator] and [TimeText] components.
 *
 * [ScreenScaffold] displays the [ScrollIndicator] at the center-end of the screen by default and
 * coordinates showing/hiding [TimeText] and [ScrollIndicator] according to [scrollState].
 *
 * Example of using AppScaffold and ScreenScaffold:
 *
 * @sample androidx.wear.compose.material3.samples.ScaffoldSample
 * @param scrollState The scroll state for [ScalingLazyColumn], used to drive screen transitions
 *   such as [TimeText] scroll away and showing/hiding [ScrollIndicator].
 * @param modifier The modifier for the screen scaffold.
 * @param timeText Time text (both time and potentially status message) for this screen, if
 *   different to the time text at the [AppScaffold] level. When null, the time text from the
 *   [AppScaffold] is displayed for this screen.
 * @param scrollIndicator The [ScrollIndicator] to display on this screen, which is expected to be
 *   aligned to Center-End. It is recommended to use the Material3 [ScrollIndicator] which is
 *   provided by default. No scroll indicator is displayed if null is passed.
 * @param content The body content for this screen.
 */
@Composable
fun ScreenScaffold(
    scrollState: ScalingLazyListState,
    modifier: Modifier = Modifier,
    timeText: (@Composable () -> Unit)? = null,
    scrollIndicator: (@Composable BoxScope.() -> Unit)? = {
        ScrollIndicator(scrollState, modifier = Modifier.align(Alignment.CenterEnd))
    },
    content: @Composable BoxScope.() -> Unit,
) =
    ScreenScaffold(
        modifier,
        timeText,
        scrollState.toScrollAwayInfoProvider(),
        scrollIndicator,
        content
    )

/**
 * [ScreenScaffold] is one of the Wear Material3 scaffold components.
 *
 * The scaffold components [AppScaffold] and [ScreenScaffold] lay out the structure of a screen and
 * coordinate transitions of the [ScrollIndicator] and [TimeText] components.
 *
 * [ScreenScaffold] displays the [ScrollIndicator] at the center-end of the screen by default and
 * coordinates showing/hiding [TimeText] and [ScrollIndicator] according to [scrollState].
 *
 * Example of using AppScaffold and ScreenScaffold:
 *
 * @sample androidx.wear.compose.material3.samples.ScaffoldSample
 * @param scrollState The scroll state for LazyColumn, used to drive screen transitions such as
 *   [TimeText] scroll away and showing/hiding [ScrollIndicator].
 * @param modifier The modifier for the screen scaffold.
 * @param timeText Time text (both time and potentially status message) for this screen, if
 *   different to the time text at the [AppScaffold] level. When null, the time text from the
 *   [AppScaffold] is displayed for this screen.
 * @param scrollIndicator The [ScrollIndicator] to display on this screen, which is expected to be
 *   aligned to Center-End. It is recommended to use the Material3 [ScrollIndicator] which is
 *   provided by default. No scroll indicator is displayed if null is passed.
 * @param content The body content for this screen.
 */
@Composable
fun ScreenScaffold(
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    timeText: (@Composable () -> Unit)? = null,
    scrollIndicator: (@Composable BoxScope.() -> Unit)? = {
        ScrollIndicator(scrollState, modifier = Modifier.align(Alignment.CenterEnd))
    },
    content: @Composable BoxScope.() -> Unit,
) =
    ScreenScaffold(
        modifier,
        timeText,
        scrollState.toScrollAwayInfoProvider(),
        scrollIndicator,
        content
    )

/**
 * [ScreenScaffold] is one of the Wear Material3 scaffold components.
 *
 * The scaffold components [AppScaffold] and [ScreenScaffold] lay out the structure of a screen and
 * coordinate transitions of the [ScrollIndicator] and [TimeText] components.
 *
 * [ScreenScaffold] displays the [ScrollIndicator] at the center-end of the screen by default and
 * coordinates showing/hiding [TimeText] and [ScrollIndicator] according to [scrollState].
 *
 * Example of using AppScaffold and ScreenScaffold:
 *
 * @sample androidx.wear.compose.material3.samples.ScaffoldSample
 * @param scrollState The scroll state for a Column, used to drive screen transitions such as
 *   [TimeText] scroll away and showing/hiding [ScrollIndicator].
 * @param modifier The modifier for the screen scaffold.
 * @param timeText Time text (both time and potentially status message) for this screen, if
 *   different to the time text at the [AppScaffold] level. When null, the time text from the
 *   [AppScaffold] is displayed for this screen.
 * @param scrollIndicator The [ScrollIndicator] to display on this screen, which is expected to be
 *   aligned to Center-End. It is recommended to use the Material3 [ScrollIndicator] which is
 *   provided by default. No scroll indicator is displayed if null is passed.
 * @param content The body content for this screen.
 */
@Composable
fun ScreenScaffold(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    timeText: (@Composable () -> Unit)? = null,
    scrollIndicator: (@Composable BoxScope.() -> Unit)? = {
        ScrollIndicator(scrollState, modifier = Modifier.align(Alignment.CenterEnd))
    },
    content: @Composable BoxScope.() -> Unit,
) =
    ScreenScaffold(
        modifier,
        timeText,
        scrollState.toScrollAwayInfoProvider(),
        scrollIndicator,
        content
    )

/**
 * [ScreenScaffold] is one of the Wear Material3 scaffold components.
 *
 * The scaffold components [AppScaffold] and [ScreenScaffold] lay out the structure of a screen and
 * coordinate transitions of the [ScrollIndicator] and [TimeText] components.
 *
 * [ScreenScaffold] displays the [ScrollIndicator] at the center-end of the screen by default and
 * coordinates showing/hiding [TimeText] and [ScrollIndicator] according to [scrollInfoProvider].
 *
 * Example of using AppScaffold and ScreenScaffold:
 *
 * @sample androidx.wear.compose.material3.samples.ScaffoldSample
 * @param modifier The modifier for the screen scaffold.
 * @param timeText Time text (both time and potentially status message) for this screen, if
 *   different to the time text at the [AppScaffold] level. When null, the time text from the
 *   [AppScaffold] is displayed for this screen.
 * @param scrollInfoProvider Provider for scroll information used to scroll away screen elements
 *   such as [TimeText] and coordinate showing/hiding the [ScrollIndicator].
 * @param scrollIndicator The [ScrollIndicator] to display on this screen, which is expected to be
 *   aligned to Center-End. It is recommended to use the Material3 [ScrollIndicator] which is
 *   provided by default. No scroll indicator is displayed if null is passed.
 * @param content The body content for this screen.
 */
@OptIn(ExperimentalWearFoundationApi::class)
@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    timeText: (@Composable () -> Unit)? = null,
    scrollInfoProvider: ScrollInfoProvider? = null,
    scrollIndicator: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val scaffoldState = LocalScaffoldState.current
    val key = remember { Any() }

    DisposableEffect(key) { onDispose { scaffoldState.removeScreen(key) } }

    OnFocusChange { focused ->
        if (focused) {
            scaffoldState.addScreen(key, timeText, scrollInfoProvider)
        } else {
            scaffoldState.removeScreen(key)
        }
    }

    scaffoldState.UpdateIdlingDetectorIfNeeded()

    Box(modifier = modifier.fillMaxSize()) {
        content()
        scrollInfoProvider?.let {
            AnimatedScrollIndicator(
                scrollInfoProvider = scrollInfoProvider,
                content = scrollIndicator,
                stage = { scaffoldState.screenStage.value }
            )
        } ?: scrollIndicator?.let { it() }
    }
}

@Composable
private fun AnimatedScrollIndicator(
    scrollInfoProvider: ScrollInfoProvider,
    stage: () -> ScreenStage,
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    // Skip if no scroll indicator provided
    content?.let { scrollIndicator ->
        val alphaValue = remember { mutableFloatStateOf(0f) }
        val animationSpec: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow)
        LaunchedEffect(scrollInfoProvider, scrollIndicator) {
            launch {
                snapshotFlow {
                        if (stage() != ScreenStage.Idle && scrollInfoProvider.isScrollable) 1f
                        else 0f
                    }
                    .distinctUntilChanged()
                    .collectLatest { targetValue ->
                        animate(
                            alphaValue.floatValue,
                            targetValue,
                            animationSpec = animationSpec
                        ) { value, _ ->
                            alphaValue.floatValue = value
                        }
                    }
            }
        }
        Box(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = alphaValue.floatValue }) {
            scrollIndicator()
        }
    }
}
