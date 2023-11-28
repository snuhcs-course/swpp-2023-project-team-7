package com.example.readability.ui.screens.viewer

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readability.R
import com.example.readability.data.ai.Quiz
import com.example.readability.data.ai.QuizLoadState
import com.example.readability.ui.animation.DURATION_EMPHASIZED_DECELERATE
import com.example.readability.ui.animation.DURATION_STANDARD
import com.example.readability.ui.animation.EASING_EMPHASIZED_DECELERATE
import com.example.readability.ui.animation.EASING_STANDARD
import com.example.readability.ui.components.RoundedRectButton
import com.example.readability.ui.components.RoundedRectFilledTonalButton
import com.example.readability.ui.theme.Gabarito
import com.example.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
@Preview
fun QuizPreview() {
    ReadabilityTheme {
        QuizView(
            quizList = listOf(
                Quiz(
                    "What initially bores Alice by the riverbank?",
                    "Alice is tired of sitting by the riverbank " +
                        "and being bored by her sister's book " +
                        "with no pictures or conversations.",
                ),
            ),
            quizSize = 5,
            quizLoadState = QuizLoadState.LOADED,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuizView(
    quizList: List<Quiz>,
    quizSize: Int,
    quizLoadState: QuizLoadState,
    onBack: () -> Unit = {},
    onNavigateReport: (Int) -> Unit = {},
) {
    val pagerScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0) { quizList.size }
    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .navigationBarsPadding()
            .systemBarsPadding(),
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            QuizProgress(
                modifier = Modifier.fillMaxWidth(),
                progress = (pagerState.currentPage + 1) / quizSize.toFloat(),
                onBack = onBack,
                onNavigateReport = {
                    onNavigateReport(pagerState.currentPage)
                },
            )
            HorizontalPager(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                state = pagerState,
                userScrollEnabled = false,
            ) {
                if (it < quizList.size) {
                    QuizCard(
                        modifier = Modifier.padding(24.dp),
                        index = it,
                        quiz = quizList[it],
                        quizLoaded = quizLoadState == QuizLoadState.LOADED || it < quizList.size - 1,
                        onNavigateReport = {
                            onNavigateReport(it)
                        },
                    )
                }
            }
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                RoundedRectFilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        pagerScope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage - 1,
                                animationSpec = tween(
                                    DURATION_STANDARD,
                                    0,
                                    EASING_STANDARD,
                                ),
                            )
                        }
                    },
                    enabled = pagerState.currentPage > 0,
                ) {
                    Text(text = "Previous")
                }
                RoundedRectButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (pagerState.currentPage == quizSize - 1) {
                            onBack()
                        } else {
                            pagerScope.launch {
                                pagerState.animateScrollToPage(
                                    pagerState.currentPage + 1,
                                    animationSpec = tween(
                                        DURATION_STANDARD,
                                        0,
                                        EASING_STANDARD,
                                    ),
                                )
                            }
                        }
                    },
                    enabled = pagerState.currentPage < quizList.size - 1 || pagerState.currentPage == quizSize - 1,
                ) {
                    Text(text = if (pagerState.currentPage == quizSize - 1) "Finish" else "Next")
                }
            }
        }
    }
}

@Composable
fun QuizProgress(
    modifier: Modifier = Modifier,
    progress: Float,
    onBack: () -> Unit = {},
    onNavigateReport: (Int) -> Unit = {},
    onRegenerate: () -> Unit = {},
) {
    val animatedProgress =
        animateFloatAsState(targetValue = progress, label = "ViewerScreen.QuizView.Progress")
    Row(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onBack() }) {
            Icon(
                painter = painterResource(id = R.drawable.x),
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        LinearProgressIndicator(
            modifier = Modifier.clip(RoundedCornerShape(2.dp)),
            progress = { animatedProgress.value },
        )
        IconButton(onClick = { onRegenerate() }) {
            Icon(
                painter = painterResource(id = R.drawable.dots_three),
                contentDescription = "More",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

enum class BlindAnchors {
    Start,
    End,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuizCard(
    modifier: Modifier = Modifier,
    index: Int,
    quiz: Quiz,
    quizLoaded: Boolean,
    onNavigateReport: () -> Unit = {},
) {
    // blind handle size
    val blindMinHeight = with(LocalDensity.current) { 48.dp.toPx() }

    // for card height animation
    var cardHeight by remember { mutableIntStateOf(0) }
    var animationDurationMultiplier by remember { mutableIntStateOf(1) }
    val animatedCardHeight = animateFloatAsState(
        targetValue = cardHeight.toFloat(),
        label = "ViewerScreen.QuizView.AnimatedCardHeight",
        animationSpec = tween(
            DURATION_EMPHASIZED_DECELERATE * animationDurationMultiplier,
            0,
            EASING_EMPHASIZED_DECELERATE,
        ),
    )

    var lastAnswerCardHeight by remember { mutableIntStateOf(0) }
    var isQuestionCardHeightChanged by remember { mutableStateOf(true) }

    val infiniteTransition =
        rememberInfiniteTransition(label = "ViewerScreen.QuizView.ThreeDotsAnimation")
    val dotCount by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1000, easing = { it })),
        label = "ViewerScreen.QuizView.ThreeDotsAnimation",
    )

    val density = LocalDensity.current
    val blindAnimationScope = rememberCoroutineScope()
    val blindAnchors = DraggableAnchors {
        BlindAnchors.Start at 0f
        BlindAnchors.End at lastAnswerCardHeight - blindMinHeight
    }

    val blindDraggableState = remember {
        AnchoredDraggableState(
            initialValue = BlindAnchors.Start,
            anchors = blindAnchors,
            positionalThreshold = { lastAnswerCardHeight / 2f },
            velocityThreshold = { with(density) { 125.dp.toPx() } },
            animationSpec = tween(
                durationMillis = DURATION_STANDARD,
                easing = EASING_STANDARD,
            ),
        )
    }

    val opened = blindDraggableState.offset > lastAnswerCardHeight / 2

    SideEffect {
        blindDraggableState.updateAnchors(blindAnchors)
    }

    ElevatedCard(modifier = modifier.anchoredDraggable(blindDraggableState, Orientation.Vertical)) {
        Layout(content = {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Quiz ${index + 1}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = Gabarito,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { onNavigateReport() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.siren),
                            contentDescription = "Report",
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
                Text(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                        .fillMaxWidth(),
                    text = quiz.question,
                    style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                    textAlign = TextAlign.Center,
                )
            }
            // Answer
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "Answer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = Gabarito,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                Text(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    text = quiz.answer,
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(48.dp))
            }
            // Blind
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 16.dp)
                    .alpha(0.4f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                    IconButton(onClick = {
                        blindAnimationScope.launch {
                            blindDraggableState.animateTo(if (opened) BlindAnchors.Start else BlindAnchors.End)
                        }
                    }) {
                        Icon(
                            modifier = Modifier.rotate(if (opened) 180f else 0f),
                            painter = painterResource(id = R.drawable.caret_down),
                            contentDescription = if (opened) "Hide" else "Show",
                        )
                    }
                }
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (quizLoaded) {
                        "Swipe down to see answer"
                    } else {
                        "Generating answer${
                            ".".repeat(
                                dotCount.toInt() + 1,
                            )
                        }"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }) { measurables, constraints ->
            val questionCardPlaceable = measurables[0].measure(constraints)
            val answerCardPlaceable = measurables[1].measure(constraints)

            val animatedHeight = animatedCardHeight.value.roundToInt()
            if (isQuestionCardHeightChanged && lastAnswerCardHeight != 0 &&
                lastAnswerCardHeight != answerCardPlaceable.height
            ) {
                isQuestionCardHeightChanged = false
            }
            lastAnswerCardHeight = answerCardPlaceable.height

            val swipeCardPlaceable = measurables[2].measure(
                constraints.copy(
                    minWidth = 0,
                    minHeight = 0,
                    maxHeight = maxOf(
                        0,
                        if (isQuestionCardHeightChanged) {
                            answerCardPlaceable.height
                        } else {
                            animatedHeight - questionCardPlaceable.height
                        },
                    ),
                    maxWidth = answerCardPlaceable.width,
                ),
            )

            val height = questionCardPlaceable.height + answerCardPlaceable.height
            val width = maxOf(questionCardPlaceable.width, answerCardPlaceable.width)

            if (cardHeight == 0) {
                animationDurationMultiplier = 0
            } else if (cardHeight == animatedHeight) {
                animationDurationMultiplier = 1
            }
            cardHeight = height

            val swipeCardMaxY = animatedHeight - blindMinHeight
            val swipeCardMinY = if (isQuestionCardHeightChanged) {
                animatedHeight - answerCardPlaceable.height
            } else {
                questionCardPlaceable.height
            }

            var progress = (blindDraggableState.offset / blindDraggableState.anchors.maxAnchor())
            if (progress.isNaN()) {
                progress = 0f
            }
            val blindHeight =
                ((swipeCardMaxY - swipeCardMinY) * progress + swipeCardMinY).roundToInt()

            layout(width, animatedHeight) {
                questionCardPlaceable.placeRelative(0, 0)
                answerCardPlaceable.placeRelative(0, questionCardPlaceable.height)
                swipeCardPlaceable.placeRelative(
                    0,
                    blindHeight,
                )
            }
        }
    }
}
