package org.ethereumphone.onboarding

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingRoute(
    onSkipOnboarding: () -> Unit,
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {

    val isSyncing by onboardingViewModel.syncState.collectAsState()

    OnboardingScreen(
        isSyncing = isSyncing,
        onStartXmtp = onboardingViewModel::generateXMTP,
        onStartSync = onboardingViewModel::startFirstSync,
        onSkipOnboarding = {
            onboardingViewModel.hideOnboarding()
            onSkipOnboarding()
        }

    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    isSyncing: Boolean = false,
    onStartXmtp: () -> Unit,
    onStartSync: () -> Unit,
    onSkipOnboarding: () -> Unit,
) {

    val coroutineScope = rememberCoroutineScope()
    val pageContent = OnboardingPageContent.entries

    val pagerState = rememberPagerState(
        pageCount = { OnboardingPageContent.entries.size},
    )

    var runOnce by remember { mutableStateOf(false) }


    LaunchedEffect(runOnce) {
        Log.d("launched ", "side effect")
        onStartSync()
        delay(1000)
        pagerState.animateScrollToPage(pagerState.currentPage + 1)
    }




    HorizontalPager(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color.Black),
        state = pagerState,
        userScrollEnabled = false
    ) {
        PagerContent(
            pageContent[pagerState.currentPage]
        ) {
            when(pagerState.currentPage) {
                0 -> {
                    Button(
                        colors =  ButtonDefaults.buttonColors(containerColor = Color(0xFF8C7DF7)),
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                delay(200)
                                onStartXmtp()
                            } }
                    ) {
                        Text(
                            text = "Enable XMTP",
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Skip",
                        color = Color.White,
                        modifier = Modifier.clickable { onSkipOnboarding() }
                    )
                }
                1 -> {
                    DegenLoadingCircle()
                    runOnce = true
                }

                2 -> {
                    Button(
                        colors =  ButtonDefaults.buttonColors(containerColor = Color(0xFF8C7DF7)),
                        onClick = {  }
                    ) {
                        Text(
                            text = "Enable XMTP",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun PagerContent(
    pagerContent: OnboardingPageContent,
    extraContent: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Text(
            text = pagerContent.title,
            style = TextStyle(
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.sp,
            ),
            modifier = Modifier.padding(vertical = 5.dp)

        )

        Text(
            text = pagerContent.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White,
                textAlign = TextAlign.Center
            )
        )
        Spacer(Modifier.height(10.dp))

        extraContent()
    }
}

@Composable
private fun DegenLoadingCircle() {

    var activatedBox by remember { mutableIntStateOf(0) }

    val alignments = listOf(
        Alignment.CenterStart,
        Alignment.TopStart,
        Alignment.TopCenter,
        Alignment.TopEnd,
        Alignment.CenterEnd,
        Alignment.BottomEnd,
        Alignment.BottomCenter,
        Alignment.BottomStart
    )

    Box(Modifier.size(60.dp)) {
        Box(
            Modifier
                .align(Alignment.Center)
                .size(15.dp)
                .background(Color.DarkGray))
        alignments.forEachIndexed { index, alignment ->

            Box(
                Modifier
                    .align(alignment)
                    .size(15.dp)
                    .background(if (activatedBox == index) Color.White else Color.DarkGray)
            )
        }
    }

    LaunchedEffect(null) {
        while (true) {
            delay(100L)
            activatedBox = (activatedBox + 1) % alignments.size
        }
    }
}

enum class OnboardingPageContent(
    val title: String,
    val description: String
) {
    WELCOME(
        "Chat with XMTP",
        "XMTP is a secure messaging protocol that allows you to communicate via eth addresses"
    ),
    SYNC("Settings things up", "Please, sign the next two prompts"),
    FINISH("All set up :)", "You can change XMTP behaviours via the messenger's options"),
    ERROR("Something went wrong","Retry now or via the option menu")
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun previewOnboarding() {
    Column(Modifier.background(Color.Black)) {
        OnboardingScreen(false, {}, {}, {})
    }
}
